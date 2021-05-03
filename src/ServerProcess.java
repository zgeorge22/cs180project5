package src;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ServerProcess extends Thread {

    private final Socket clientSocket;
    private OutputStream outputStream;
    private String userID;
    private Account currentAccount;
    private Database database;
    private PrintWriter writer;

    private Scanner in;
    private PrintWriter out;

    public ServerProcess(Socket clientSocket, Database database) {
        this.clientSocket = clientSocket;
        this.database = database;
    }

    public void run() {
        try {
            clientProcess();
            clientSocket.close();
            System.out.println("SERVER - Client Disconnected: " + clientSocket.getPort());
        } catch (IOException | UsernameAlreadyExistsException | ConversationNotFoundException
                | MessageNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void clientProcess() throws IOException, UsernameAlreadyExistsException, ConversationNotFoundException,
            MessageNotFoundException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        writer = new PrintWriter(outputStream);

        Boolean loggedIn = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<Account> activeUsersList = ServerBackground.getActiveUsers();

        do {
            String commandString = (reader.readLine());
            String[] tokenLogin = commandString.split(" ");
            String cmdLogin = tokenLogin[0];
            switch (cmdLogin) {
                case ("createAccount"):
                    String newUsername = tokenLogin[1];
                    String newPassword = tokenLogin[2];
                    // TODO - fix if user already exists
                    Account newAccount = new Account(newUsername, newPassword, database, true);
                    currentAccount = newAccount;
                    activeUsersList.add(newAccount);
                    loggedIn = true;

                    sendClient("createAccountSuccessful " + newUsername);
                    break;
                case ("loginAccount"):
                    String existingUsername = tokenLogin[1];
                    String existingPassword = tokenLogin[2];

                    try {
                        currentAccount = checkUserLogin(existingUsername, existingPassword);
                        activeUsersList.add(currentAccount);

                        loggedIn = true;

                        sendClient("loginAccountSuccessful " + existingUsername);
                    } catch (AccountNotExistException e) {

                    }
                    break;
                default:
                    System.out.println("ERROR - Unknown command: " + cmdLogin);
                    break;
            }
        } while (!loggedIn);

        if (loggedIn) {
            messagingProcess(activeUsersList);
        }

        writer.close();
    }

    public void messagingProcess(ArrayList<Account> activeUsersList)
            throws IOException, UsernameAlreadyExistsException, ConversationNotFoundException {

        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        boolean exit = false;

        sendClient("prepareForDataDump");

        Database clientData = new Database(false);
        for (int i = 0; i < database.getConversations().size(); i++) {
            if (database.getConversations().get(i).getParticipants().contains(this.currentAccount)) {
                clientData.addToDatabase(database.getConversations().get(i));
                for (int j = 0; j < database.getConversations().get(i).getMessages().size(); j++) {

                }
            }
        }

        ObjectOutputStream outputObjectStream = new ObjectOutputStream(this.outputStream);
        outputObjectStream.flush();
        outputObjectStream.writeObject(clientData);
        outputObjectStream.flush();

        do {
            BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            String commandString = (bfr.readLine());

            System.out.println("SERVER (" + clientSocket.getPort() + ") - " + commandString);

            String[] token = commandString.split(" ");
            String cmd = token[0];
            ArrayList<ServerProcess> serverProcessList = Server.getServerList();
            switch (cmd) {

                case ("editPassword"):
                    String editPassword = token[1];
                    currentAccount.changePassword(editPassword);
                    break;
                case ("createConvo"):
                    // sendClient("succeeded");
                    String participantsString = token[1];
                    String[] participantsUsernameList = participantsString.split(",");
                    // create conversation
                    ArrayList<Account> newConvoAccountList = new ArrayList<>();
                    for (int i = 0; i < participantsUsernameList.length; i++) {
                        try {
                            newConvoAccountList.add(database.getAccountByUsername(participantsUsernameList[i]));
                        } catch (AccountNotExistException e) {
                            e.printStackTrace();
                        }
                    }
                    String placeHolder = commandString.substring(commandString.indexOf(" ") + 1);
                    String initialMsg = placeHolder.substring(placeHolder.indexOf(" ") + 1);
                    String name = newConvoAccountList.size() > 2 ? "GC" : "DM";
                    Conversation newConvo = new Conversation(name, newConvoAccountList, true, database);
                    Message newMessage = new Message(this.currentAccount.getUsername(), initialMsg, database);
                    newConvo.addMessage(newMessage);
                    int newConvoID = newConvo.getConversationId();
                    for (int i = 0; i < newConvoAccountList.size(); i++) {
                        for (int j = 0; j < activeUsersList.size(); j++) {
                            if (newConvoAccountList.get(i).getUsername().equals(activeUsersList.get(j).getUsername())) {
                                for (int k = 0; k < serverProcessList.size(); k++) {
                                    if (serverProcessList.get(k).getCurrentAccount().getUsername()
                                            .equals(newConvoAccountList.get(i).getUsername())) {
                                        sendOtherClient(String.format("addConvo %d %s", newConvoID, participantsString),
                                                serverProcessList.get(k).getClientSocket());
                                        sendOtherClient(
                                                ("addMsg " + newConvoID + " " + newMessage.getId() + " "
                                                        + this.currentAccount.getUsername() + " "
                                                        + newMessage.getTimestamp() + " " + initialMsg),
                                                serverProcessList.get(k).getClientSocket());
                                    }
                                }

                            }
                        }
                    }
                    System.out.println("SERVER - Received createConvo for [" + participantsString
                            + "] with initialMsg [" + initialMsg + "] from [" + currentAccount.getUsername() + "]");
                    break;
                case ("leaveConvo"):
                    try {
                        int leaveConversationID = Integer.parseInt(token[1]);
                        Conversation conversation = database.getConversationById(leaveConversationID);
                        for (int i = 0; i < conversation.getParticipants().size(); i++) {
                            for (int j = 0; j < activeUsersList.size(); j++) {
                                if (conversation.getParticipants().get(i).getUsername()
                                        .equals(activeUsersList.get(j).getUsername())) {
                                    for (int k = 0; k < serverProcessList.size(); k++) {
                                        if (serverProcessList.get(k).getCurrentAccount().getUsername()
                                                .equals(conversation.getParticipants().get(i).getUsername())) {
                                            sendOtherClient(
                                                    "removeUser " + conversation.getConversationId() + " "
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
                    } catch (AccountNotExistException e) {
                        e.printStackTrace();
                    }
                case ("createMsg"):
                    // Changed "sendMsg" to "createMsg"
                    int convoID = Integer.parseInt(token[1]);
                    String placeholder = commandString.substring(commandString.indexOf(" ") + 1);
                    String newMessageContent = placeholder.substring(placeholder.indexOf(" ") + 1);
                    Message tempMessage = new Message(this.currentAccount.getUsername(), newMessageContent, database);
                    database.getConversationById(convoID).addMessage(tempMessage);
                    for (int i = 0; i < database.getConversationById(convoID).getParticipants().size(); i++) {
                        for (int j = 0; j < activeUsersList.size(); j++) {
                            if (database.getConversationById(convoID).getParticipants().get(i).getUsername()
                                    .equals(activeUsersList.get(j).getUsername())) {
                                for (int k = 0; k < serverProcessList.size(); k++) {
                                    if (serverProcessList.get(k).getCurrentAccount().getUsername().equals(database
                                            .getConversationById(convoID).getParticipants().get(i).getUsername())) {
                                        sendOtherClient(
                                                "addMsg " + convoID + " " + tempMessage.getId() + " "
                                                        + this.currentAccount.getUsername() + " "
                                                        + tempMessage.getTimestamp() + " " + newMessageContent,
                                                serverProcessList.get(k).getClientSocket());
                                    }
                                }
                            }
                        }
                    }

                    break;
                case ("editMsg"):
                    // SERVER HAS TO VALIDATE THAT USER HAS THE AUTHORITY (CLIENT ALREADY DOES THIS
                    // BUT WE MAY NEED SERVER TO DO IT)
                    int convoIdentifier = Integer.parseInt(token[1]);
                    int initialID = Integer.parseInt(token[2]);
                    String placeH = commandString.substring(commandString.indexOf(" ") + 1);
                    String placeHo = placeH.substring(placeH.indexOf(" ") + 1);
                    String newMessageEdit = placeHo.substring(placeHo.indexOf(" ") + 1);
                    Message messageToEdit;
                    try {
                        messageToEdit = database.getMessageById(initialID);
                        messageToEdit.editMessage(newMessageEdit);
                        for (int i = 0; i < database.getConversationById(convoIdentifier).getParticipants()
                                .size(); i++) {
                            for (int j = 0; j < activeUsersList.size(); j++) {
                                if (database.getConversationById(convoIdentifier).getParticipants().get(i).getUsername()
                                        .equals(activeUsersList.get(j).getUsername())) {
                                    for (int k = 0; k < serverProcessList.size(); k++) {
                                        if (serverProcessList.get(k).getCurrentAccount().getUsername()
                                                .equals(database.getConversationById(convoIdentifier).getParticipants()
                                                        .get(i).getUsername()))
                                            sendOtherClient(
                                                    "editMsg " + convoIdentifier + " " + initialID + " " + this.userID
                                                            + " " + messageToEdit.getTimestamp() + " " + newMessageEdit,
                                                    serverProcessList.get(k).getClientSocket());

                                    }
                                }
                            }
                        }
                    } catch (MessageNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case ("deleteMsg"):
                    int convoIdent = Integer.parseInt(token[1]);
                    int messageIdentifier = Integer.parseInt(token[2]);
                    try {
                        database.getMessageById(messageIdentifier).deleteMessage();
                        for (int i = 0; i < database.getConversationById(convoIdent).getParticipants().size(); i++) {
                            for (int j = 0; j < activeUsersList.size(); j++) {
                                if (database.getConversationById(convoIdent).getParticipants().get(i).getUsername()
                                        .equals(activeUsersList.get(j).getUsername())) {
                                    for (int k = 0; k < serverProcessList.size(); k++) {
                                        if (serverProcessList.get(k).getCurrentAccount().getUsername()
                                                .equals(database.getConversationById(convoIdent).getParticipants()
                                                        .get(i).getUsername()))
                                            sendOtherClient("removeMsg " + convoIdent + " " + messageIdentifier,
                                                    serverProcessList.get(k).getClientSocket());
                                    }
                                }
                            }
                        }
                    } catch (MessageNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case ("logoutAccount"):
                    ServerBackground.activeUsers.remove(this.currentAccount);
                    pw.write("logoutTrue");
                    pw.println();
                    pw.flush();
                    exit = true;
                    break;
                default:
                    System.out.println("ERROR - Unknow command: " + cmd);
            }
        } while (!exit);
    }

    public Account checkUserLogin(String username, String password) throws AccountNotExistException {

        ArrayList<Account> accounts = database.getAccounts();

        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getUsername().equals(username)) {
                if (accounts.get(i).getPassword().equals(password)) {
                    return accounts.get(i);
                }
            }
        }
        throw new AccountNotExistException();
    }

    public Account getAccount() {
        return currentAccount;
    }

    public void sendClient(String message) {
        writer.write(message);
        writer.println();
        writer.flush();
    }

    public void sendOtherClient(String message, Socket clientSocket) throws IOException {
        OutputStream outputStreamSend = clientSocket.getOutputStream();
        PrintWriter sendWriter = new PrintWriter(outputStreamSend);

        sendWriter.write(message);
        sendWriter.println();
        sendWriter.flush();
        // sendWriter.close();
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }
}