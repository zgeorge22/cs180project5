import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class is a processing class that spawns a thread each time a user logs in from their client. Then, this
 * thread handles all server-side processing related to that client.
 *
 * <p>Purdue University -- CS18000 -- Spring 2021 -- Project 5</p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack Dorkin
 * @version May 3rd, 2021
 */

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
        } catch (IOException | AccountNotExistException | UsernameAlreadyExistsException
                | ConversationNotFoundException | MessageNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void clientProcess() throws IOException, AccountNotExistException, UsernameAlreadyExistsException,
            ConversationNotFoundException, MessageNotFoundException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        writer = new PrintWriter(outputStream);

        Boolean loggedIn = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<Account> activeUsersList = ServerBackground.getActiveUsers();

        clientLoginLoop:
        do {
            String commandString = (reader.readLine());
            String[] tokenLogin = commandString.split(" ");
            String cmdLogin = tokenLogin[0];
            // TODO - fix if user already exists
            switch (cmdLogin) {
                case ("createAccount") -> {
                    String newUsername = tokenLogin[1];
                    String newPassword = tokenLogin[2];
                    Account newAccount = new Account(newUsername, newPassword, database, true);
                    currentAccount = newAccount;
                    activeUsersList.add(newAccount);
                    loggedIn = true;
                    writer.write("createAccountSuccessful " + newUsername);
                    writer.println();
                    writer.flush();
                }
                case ("loginAccount") -> {
                    String existingUsername = tokenLogin[1];
                    String existingPassword = tokenLogin[2];
                    Account exitingAccount = checkUserLogin(existingUsername, existingPassword);
                    currentAccount = exitingAccount;
                    activeUsersList.add(exitingAccount);
                    loggedIn = true;
                    writer.write("loginAccountSuccessful " + existingUsername);
                    writer.println();
                    writer.flush();
                }
                default -> System.out.println("ERROR - Unknown command: " + cmdLogin);
            }
        } while (!loggedIn);

        if (loggedIn) {
            messagingProcess(activeUsersList);
        }

        writer.close();
    }

    public void messagingProcess(ArrayList<Account> activeUsersList) throws IOException, AccountNotExistException,
             ConversationNotFoundException, MessageNotFoundException {

        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        boolean exit = false;

        dataDump();

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
                    send("succeeded", this.clientSocket);
                    String participantsString = token[1];
                    String[] participantsUsernameList = participantsString.split(",");
                    // create conversation
                    ArrayList<Account> newConvoAccountList = new ArrayList<>();
                    for (String s : participantsUsernameList) {
                        newConvoAccountList.add(database.getAccountByUsername(s));
                    }
                    String placeHolder = commandString.substring(commandString.indexOf(" ") + 1);
                    String initialMsg = placeHolder.substring(placeHolder.indexOf(" ") + 1);
                    String name = newConvoAccountList.size() > 2 ? "GC" : "DM";
                    Conversation newConvo = new Conversation(name, newConvoAccountList, true, database);
                    Message newMessage = new Message(this.currentAccount.getUsername(), initialMsg, database);
                    newConvo.addMessage(newMessage);
                    int newConvoID = newConvo.getConversationId();
                    for (Account account : newConvoAccountList) {
                        for (Account value : activeUsersList) {
                            if (account.getUsername().equals(value.getUsername())) {
                                for (ServerProcess serverProcess : serverProcessList) {
                                    if (serverProcess.getCurrentAccount().getUsername()
                                            .equals(account.getUsername())) {
                                        send(String.format("addConvo %d %s", newConvoID, participantsString),
                                                serverProcess.getClientSocket());
                                        send(("addMsg " + newConvoID + " " + newMessage.getId() + " "
                                                + this.currentAccount.getUsername() + " " + newMessage.getTimestamp()
                                                + " " + initialMsg), serverProcess.getClientSocket());
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
                            for (Account account : activeUsersList) {
                                if (conversation.getParticipants().get(i).getUsername()
                                        .equals(account.getUsername())) {
                                    for (ServerProcess serverProcess : serverProcessList) {
                                        if (serverProcess.getCurrentAccount().getUsername()
                                                .equals(conversation.getParticipants().get(i).getUsername())) {
                                            send("removeUser " + conversation.getConversationId() + " "
                                                            + this.currentAccount.getUsername(),
                                                    serverProcess.getClientSocket());
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
                case ("createMsg"):
                    // Changed "sendMsg" to "createMsg"
                    int convoID = Integer.parseInt(token[1]);
                    String placeholder = commandString.substring(commandString.indexOf(" ") + 1);
                    String newMessageContent = placeholder.substring(placeholder.indexOf(" ") + 1);
                    Message tempMessage = new Message(this.currentAccount.getUsername(), newMessageContent, database);
                    database.getConversationById(convoID).addMessage(tempMessage);
                    for (int i = 0; i < database.getConversationById(convoID).getParticipants().size(); i++) {
                        for (Account account : activeUsersList) {
                            if (database.getConversationById(convoID).getParticipants().get(i).getUsername()
                                    .equals(account.getUsername())) {
                                for (ServerProcess serverProcess : serverProcessList) {
                                    if (serverProcess.getCurrentAccount().getUsername().equals(database
                                            .getConversationById(convoID).getParticipants().get(i).getUsername())) {
                                        send("addMsg " + convoID + " " + tempMessage.getId() + " "
                                                + this.currentAccount.getUsername() + " " + tempMessage.getTimestamp()
                                                + " " + newMessageContent, serverProcess.getClientSocket());
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
                    Message messageToEdit = database.getMessageById(initialID);
                    messageToEdit.editMessage(newMessageEdit);
                    for (int i = 0; i < database.getConversationById(convoIdentifier).getParticipants().size(); i++) {
                        for (Account account : activeUsersList) {
                            if (database.getConversationById(convoIdentifier).getParticipants().get(i).getUsername()
                                    .equals(account.getUsername())) {
                                for (ServerProcess serverProcess : serverProcessList) {
                                    if (serverProcess.getCurrentAccount().getUsername()
                                            .equals(database.getConversationById(convoIdentifier).getParticipants()
                                                    .get(i).getUsername()))
                                        send("editMsg " + convoIdentifier + " " + initialID + " "
                                                        + this.userID + " " + messageToEdit.getTimestamp()
                                                        + " " + newMessageEdit,
                                                serverProcess.getClientSocket());

                                }
                            }
                        }
                    }
                    break;
                case ("deleteMsg"):
                    int convoIdent = Integer.parseInt(token[1]);
                    int messageIdentifier = Integer.parseInt(token[2]);
                    database.getMessageById(messageIdentifier).deleteMessage();
                    for (int i = 0; i < database.getConversationById(convoIdent).getParticipants().size(); i++) {
                        for (Account account : activeUsersList) {
                            if (database.getConversationById(convoIdent).getParticipants().get(i).getUsername()
                                    .equals(account.getUsername())) {
                                for (ServerProcess serverProcess : serverProcessList) {
                                    if (serverProcess.getCurrentAccount().getUsername().equals(database
                                            .getConversationById(convoIdent).getParticipants().get(i).getUsername()))
                                        send("removeMsg " + convoIdent + " " + messageIdentifier,
                                                serverProcess.getClientSocket());
                                }
                            }
                        }
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

    private void dataDump() {

        Database clientData = new Database(false);
        for (int i = 0; i < database.getConversations().size(); i++) {
            if (database.getConversations().get(i).getParticipants().contains(this.currentAccount))
                clientData.addToDatabase(database.getConversations().get(i));
        }

        ArrayList<String> accountUsernameStrings = new ArrayList<>();
        for (int i = 0; i < clientData.getConversations().size(); i++) {
            for (int j = 0; j < clientData.getConversations().get(i).getParticipants().size(); j++) {
                accountUsernameStrings.add(clientData.getConversations().get(i).getParticipants().get(j).getUsername());
            }
        }
        ArrayList<String> finalAccountUsernameStrings = new ArrayList<>();
        finalAccountUsernameStrings.add(accountUsernameStrings.get(0));

        for (int i = 0; i < accountUsernameStrings.size(); i++) {
            for (int j = 0; j < finalAccountUsernameStrings.size(); j++) {
                if (!finalAccountUsernameStrings.contains(accountUsernameStrings.get(i))) {
                    finalAccountUsernameStrings.add(accountUsernameStrings.get(i));
                }
            }
        }
        System.out.println(accountUsernameStrings.toString());
        System.out.println(finalAccountUsernameStrings.toString());


        ArrayList<String> conversationStrings = new ArrayList<>();
        for (int i = 0; i < clientData.getConversations().size(); i++) {
            String conversationString = Integer.toString(clientData.getConversations().get(i).getConversationId());
            conversationString = conversationString + " ";

            String thisConversationParticipants = "";
            for (int j = 0; j < clientData.getConversations().get(i).getParticipants().size(); j++) {
                thisConversationParticipants = thisConversationParticipants +
                        clientData.getConversations().get(i).getParticipants().get(j).getUsername() + ",";
            }
            thisConversationParticipants = thisConversationParticipants.substring
                    (0, thisConversationParticipants.length() - 1);

            conversationString = conversationString + thisConversationParticipants;

            conversationStrings.add(conversationString);
        }

        ArrayList<String> messageStrings = new ArrayList<>();
        for (int i = 0; i < clientData.getConversations().size(); i++) {
            for (int j = 0; j < clientData.getConversations().get(i).getMessages().size(); j++) {
                String messageString = clientData.getConversations().get(i).getConversationId() + ",";
                messageString = messageString + clientData.getConversations().get(i).getMessages().get(j).toString();
                messageStrings.add(messageString);
            }
        }

        int numAccsToSend = finalAccountUsernameStrings.size();
        int numConvosToSend = conversationStrings.size();
        int numMsgsToSend = messageStrings.size();

        send("prepareForDataDump " + numAccsToSend + " " + numConvosToSend + " " + numMsgsToSend, clientSocket);

        for (String finalAccountUsernameString : finalAccountUsernameStrings) {
            send(finalAccountUsernameString, clientSocket);
        }
        for (String conversationString : conversationStrings) {
            send(conversationString, clientSocket);
        }
        for (String messageString : messageStrings) {
            send(messageString, clientSocket);
        }
    }

    public Account checkUserLogin(String username, String password)
            throws AccountNotExistException {

        ArrayList<Account> userList = database.getAccounts();
        ArrayList<String> databaseUsernames = new ArrayList<>();
        ArrayList<String> databasePasswords = new ArrayList<>();

        for (int i = 0; i < database.getAccounts().size(); i++) {
            databaseUsernames.add(database.getAccounts().get(i).getUsername());
            databasePasswords.add(database.getAccounts().get(i).getPassword());
        }

        if (databaseUsernames.contains(username) && databasePasswords.contains(password) // Final login
                && (databaseUsernames.indexOf(username) == databasePasswords.indexOf(password))) {
            return (database.getAccountByUsername(username));
        } else {
            throw new AccountNotExistException();
        }

    }

    public Account getAccount() {
        return currentAccount;
    }

    public void send(String message, Socket clientSocket) {
        OutputStream ous = null;
        try {
            ous = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(ous);
        pw.write(message);
        pw.println();
        pw.flush();
        // writer.close();
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }
}