package src;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * ServerProcess
 *
 * The ServerProcess class handles all actions related to an individual client. Each ServerProcess corresponds to
 * an individual user and ends once that user is logged off.
 *
 * Past homework assignments
 *
 * @author Natalie Wu, Benjamin Davenport, Rishi Banerjee, Zach George
 * @version Due 05/03/2021
 *
 */

public class ServerProcess extends Thread {

    private final Socket clientSocket;
    private OutputStream outputStream;
    private String userID;
    private Account currentAccount;
    private Database database;
    private PrintWriter writer;

    public ServerProcess(Socket clientSocket, Database database) {
        this.clientSocket = clientSocket;
        this.database = database;
    }

    // Runs the needed commands to handle client requests
    public void run() {
        try {
            clientProcess();
        } catch (IOException | AccountNotExistException | UsernameAlreadyExistsException | ConversationNotFoundException
                | MessageNotFoundException e) {
            e.printStackTrace();
        }
    }

    // clientProcess allows the user to log in or create an account before starting the messagingProcess method.
    private void clientProcess() throws IOException, AccountNotExistException, UsernameAlreadyExistsException,
            ConversationNotFoundException, MessageNotFoundException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        Boolean loggedIn = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter print = new PrintWriter(clientSocket.getOutputStream());
        ArrayList<Account> activeUsersList = ServerBackground.getActiveUsers();

        do {
            String commandString = (reader.readLine());
            String[] tokenLogin = commandString.split(" ");
            String cmdLogin = tokenLogin[0];
            switch (cmdLogin) {
                case ("createAccount"):
                    String newUsername = tokenLogin[1];
                    String newPassword = tokenLogin[2];
                    //TODO - fix if user already exists
                    Account newAccount = new Account(newUsername, newPassword, database, true);
                    currentAccount = newAccount;
                    activeUsersList.add(newAccount);
                    loggedIn = true;
                    print.write("true");
                    print.println();
                    print.flush();
                    break;
                case ("loginAccount"):
                    String existingUsername = tokenLogin[1];
                    String existingPassword = tokenLogin[2];
                    Account exitingAccount = checkUserLogin(existingUsername, existingPassword);
                    currentAccount = exitingAccount;
                    activeUsersList.add(exitingAccount);
                    loggedIn = true;
                    print.write("true");
                    print.println();
                    print.flush();
                    break;
            }
        }while (!loggedIn);



        if (loggedIn) {
            messagingProcess(activeUsersList);
        }

        // Closing the clientSocket and writer will occur only once messagingProcess has ended.
        clientSocket.close();
        writer.close();

    }

    // messagingProcess handles all client processes after logging in. This includes sending messages, creating or
    // leaving conversations, editing and deleting messages, editing each user's account, and logging off.
    public void messagingProcess(ArrayList<Account> activeUsersList) throws IOException, AccountNotExistException,
            ConversationNotFoundException, MessageNotFoundException {

        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        boolean exit = false;

        // clientData database is a mini-Database that only contains the data associated with that user.
        Database clientData = new Database(false);
        for (int i = 0; i < database.getConversations().size(); i++) {
            if (database.getConversations().get(i).getParticipants().contains(this.currentAccount))
                clientData.addToDatabase(database.getConversations().get(i));
        }

        // outputObjectStream sends the clientData Database to the client for their use.
        ObjectOutputStream outputObjectStream = new ObjectOutputStream(this.outputStream);
        outputObjectStream.flush();
        outputObjectStream.writeObject(clientData);
        outputObjectStream.flush();

        // do-while loop will repeat until user logs off. This loop contains all needed commands and actions for
        // interacting with the client.
        do {
            BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            String commandString = (bfr.readLine());
            System.out.println(commandString);
            String[] token = commandString.split(" ");
            String cmd = token[0];
            ArrayList<ServerProcess> serverProcessList = Server.getServerList();
            switch (cmd) {

                // case allows the user to change their password.
                case ("editPassword"):
                    String editPassword = token[1];
                    currentAccount.changePassword(editPassword);
                    break;

                // case allows user to create a new conversation.
                case ("createConvo"):
                    send("succeeded",this.clientSocket);
                    String participantsString = token[1];
                    String[] participantsUsernameList = participantsString.split(",");
                    // create conversation
                    ArrayList<Account> newConvoAccountList = new ArrayList<>();
                    for (int i = 0; i < participantsUsernameList.length; i++) {
                        newConvoAccountList.add(database.getAccountByUsername(participantsUsernameList[i]));
                    }
                    String placeHolder = commandString.substring(commandString.indexOf(" ") + 1);
                    String initialMsg = placeHolder.substring(placeHolder.indexOf(" ") + 1);
                    Conversation newConvo = new Conversation(null, newConvoAccountList,
                            true, database);
                    Message newMessage = new Message(this.currentAccount.getUsername(), initialMsg, database);
                    newConvo.addMessage(newMessage);
                  //  System.out.println(database.getConversationById(0).getMessages().get(0).getContent());
                    int newConvoID = newConvo.getConversationId();
                    // send intialMsg to users in newConvo
                    //ArrayList<ServerProcess> serverProcessList = Server.getServerList();
                    // for loops sends the new conversation and initial message to all online users included within the
                    // conversation.
                    for (int i = 0; i < newConvoAccountList.size(); i++) {
                        for (int j = 0; j < activeUsersList.size(); j++) {
                            if (newConvoAccountList.get(i).getUsername().equals(activeUsersList.get(j).getUsername())) {
                                for (int k = 0; k < serverProcessList.size(); k++) {
                                    if (serverProcessList.get(k).getCurrentAccount().getUsername().equals(
                                            newConvoAccountList.get(i).getUsername())) {
                                        send(String.format("addConvo %d %s", newConvoID, participantsString),
                                                serverProcessList.get(k).getClientSocket());
                                        send(("addMsg " + newConvoID + " " + newMessage.getId() + " " + this.currentAccount.getUsername() + " "
                                                        + newMessage.getTimestamp() + " " + initialMsg),
                                                serverProcessList.get(k).getClientSocket());
                                    }
                                }

                            }
                        }
                    }
                    System.out.println("SERVER - Received createConvo for [" + participantsString + "] with initialMsg [" + initialMsg
                            + "] from [" + currentAccount.getUsername() + "]");
                    break;

                // case allows for users to leave a conversation that they are currently a part of.
                case ("leaveConvo"):
                    try {
                        int leaveConversationID = Integer.parseInt(token[1]);
                        Conversation conversation = database.getConversationById(leaveConversationID);
                        // for loops send all online users within the conversation information that the specific user
                        // left the conversation.
                        for (int i = 0; i < conversation.getParticipants().size(); i++) {
                            for (int j = 0; j < activeUsersList.size(); j++) {
                                if (conversation.getParticipants().get(i).getUsername().equals(activeUsersList.get(j).getUsername())) {
                                    for (int k = 0; k < serverProcessList.size(); k++) {
                                        if (serverProcessList.get(k).getCurrentAccount().getUsername().equals(
                                                conversation.getParticipants().get(i).getUsername())) {
                                            send("removeUser " + conversation.getConversationId() + " "
                                                    + this.currentAccount.getUsername(),
                                                    serverProcessList.get(k).getClientSocket());
                                        }
                                    }

                                }
                            }
                        }
                        conversation.removeParticipant(this.currentAccount.getUsername());

                        break;
                    } catch (ConversationNotFoundException e) {
                        e.printStackTrace();
                    }

                // case allows for a user to create and send a new message.
                case ("createMsg"):
                    //Changed "sendMsg" to "createMsg"
                    int convoID = Integer.parseInt(token[1]);
                    String placeholder = commandString.substring(commandString.indexOf(" ") + 1);
                    String newMessageContent = placeholder.substring(placeholder.indexOf(" ") + 1);
                    Message tempMessage = new Message(this.currentAccount.getUsername(), newMessageContent, database);
                    database.getConversationById(convoID).addMessage(tempMessage);
                    // for loops notify all online users within the conversation that a new message was sent.
                    for (int i = 0; i < database.getConversationById(convoID).getParticipants().size(); i++) {
                        for (int j = 0; j < activeUsersList.size(); j++) {
                            if (database.getConversationById(convoID).getParticipants().get(i).getUsername().equals
                                    (activeUsersList.get(j).getUsername())) {
                                for (int k = 0; k < serverProcessList.size(); k++) {
                                    if (serverProcessList.get(k).getCurrentAccount().getUsername().equals(database
                                            .getConversationById(convoID).getParticipants().get(i).getUsername())) {
                                        send("addMsg " + convoID + " " + tempMessage.getId() + " " +
                                                this.currentAccount.getUsername() + " " + tempMessage.getTimestamp() + " " +
                                                newMessageContent, serverProcessList.get(k).getClientSocket());
                                        }
                                    }
                                }
                            }
                        }
                    break;

                // case allows a user to edit a message that they sent
                case ("editMsg"):
                    //SERVER HAS TO VALIDATE THAT USER HAS THE AUTHORITY (CLIENT ALREADY DOES THIS BUT WE MAY NEED SERVER TO DO IT)
                    int convoIdentifier = Integer.parseInt(token[1]);
                    int initialID = Integer.parseInt(token[2]);
                    String placeH = commandString.substring(commandString.indexOf(" ") + 1);
                    String placeHo = placeH.substring(placeH.indexOf(" ") + 1);
                    String newMessageEdit = placeHo.substring(placeHo.indexOf(" ") + 1);
                    Message messageToEdit = database.getMessageById(initialID);
                    messageToEdit.editMessage(newMessageEdit);
                    for (int i = 0; i < database.getConversationById(convoIdentifier).getParticipants().size(); i++) {
                        for (int j = 0; j < activeUsersList.size(); j++) {
                            if (database.getConversationById(convoIdentifier).getParticipants().get(i).getUsername()
                                    .equals(activeUsersList.get(j).getUsername())) {
                                for (int k = 0; k < serverProcessList.size(); k++) {
                                    if (serverProcessList.get(k).getCurrentAccount().getUsername().equals(database
                                            .getConversationById(convoIdentifier).getParticipants().get(i)
                                            .getUsername()))
                                        send("editMsg " + convoIdentifier + " " + initialID + " " + this.userID
                                                        + " " + messageToEdit.getTimestamp() + " " + newMessageEdit,
                                                serverProcessList.get(k).getClientSocket());

                                }
                            }
                        }
                    }
                    break;

                // case allows a user to delete a message that they sent
                case ("deleteMsg"):
                    int convoIdent = Integer.parseInt(token[1]);
                    int messageIdentifier = Integer.parseInt(token[2]);
                    database.getMessageById(messageIdentifier).deleteMessage();
                    for (int i = 0; i < database.getConversationById(convoIdent).getParticipants().size(); i++) {
                        for (int j = 0; j < activeUsersList.size(); j++) {
                            if (database.getConversationById(convoIdent).getParticipants().get(i).getUsername()
                                    .equals(activeUsersList.get(j).getUsername())) {
                                for (int k = 0; k < serverProcessList.size(); k++) {
                                    if (serverProcessList.get(k).getCurrentAccount().getUsername().equals(database
                                            .getConversationById(convoIdent).getParticipants().get(i).getUsername()))
                                        send("removeMsg " + convoIdent + " " + messageIdentifier,
                                                serverProcessList.get(k).getClientSocket());
                                }
                            }
                        }
                    }
                    break;

                // case allows a user to logout.
                case ("logoutAccount"):
                    ServerBackground.activeUsers.remove(this.currentAccount);
                    pw.write("logoutTrue");
                    pw.println();
                    pw.flush();
                    exit = true;
                    break;

                // case checks that the client received the client database.
                case ("Database Received Successfully"):
                    System.out.println("Database Sent SuccessFully");
                    pw.write("");
                    pw.println();
                    pw.flush();
                    break;
            }
        }while(!exit);
    }

    // checkUserLogin makes sure that a user's username and password are correct
    public Account checkUserLogin(String username, String password) throws AccountNotExistException {

        ArrayList<Account> userList = database.getAccounts();
        ArrayList<String> databaseUsernames = new ArrayList<>();
        ArrayList<String> databasePasswords = new ArrayList<>();

        for (int i = 0; i < database.getAccounts().size(); i++) {
            databaseUsernames.add(database.getAccounts().get(i).getUsername());
            databasePasswords.add(database.getAccounts().get(i).getPassword());
        }

        if (databaseUsernames.contains(username) && databasePasswords.contains(password) //Final login
                && (databaseUsernames.indexOf(username) == databasePasswords.indexOf(password))) {
            return (database.getAccountByUsername(username));
        } else {
            throw new AccountNotExistException();
        }

    }

    // send allows for the sending of a message String to a specific client.
    public void send(String message, Socket clientSocket) throws IOException {
        OutputStream outputStreamSend = clientSocket.getOutputStream();
        writer = new PrintWriter(outputStreamSend);
        writer.write(message);
        writer.println();
        writer.flush();
    }

    // getClientSocket returns the client socket of a specific user.
    public Socket getClientSocket() {
        return clientSocket;
    }

    // getCurrentAccount returns the active account within a specific ServerProcess.
    public Account getCurrentAccount() {
        return currentAccount;
    }
}