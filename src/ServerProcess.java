import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

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
    private Account currentAccount;
    private Database database;
    private PrintWriter writer;


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

        boolean loggedIn = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<Account> activeUsersList = ServerBackground.getActiveUsers();

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

                    sendClient("createAccountSuccessful " + newUsername);
                }
                case ("loginAccount") -> {
                    String existingUsername = tokenLogin[1];
                    String existingPassword = tokenLogin[2];

                    try {
                        currentAccount = checkUserLogin(existingUsername, existingPassword);
                        activeUsersList.add(currentAccount);

                        loggedIn = true;

                        sendClient("loginAccountSuccessful " + existingUsername);
                    } catch (AccountNotExistException e) {

                    }
                }
                default -> {
                    System.out.println("ERROR - Unknown command: " + cmdLogin);
                }
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

        if (this.currentAccount.getConversationIds().size() != 0) {
            dataDump();
        } else sendClient("prepareForDataDump 0 0 0");

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
                    String placeHolder = commandString.substring(commandString.indexOf(" ") + 1);
                    String initialMsg = placeHolder.substring(placeHolder.indexOf(" ") + 1);
                    try {
                        for (String s : participantsUsernameList) {
                            newConvoAccountList.add(database.getAccountByUsername(s));
                        }
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
                                            sendOtherClient(String.format("addConvo %d %s", newConvoID, participantsString),
                                                    serverProcess.getClientSocket());
                                            sendOtherClient(
                                                    ("addMsg " + newConvoID + " " + newMessage.getId() + " "
                                                            + this.currentAccount.getUsername() + " "
                                                            + newMessage.getTimestamp() + " " + initialMsg),
                                                    serverProcess.getClientSocket());
                                        }
                                    }

                                }
                            }
                        }
                    } catch (AccountNotExistException e) {
                        sendClient("failedCreateConvo");
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
                                            sendOtherClient(
                                                    "removeUser " + conversation.getConversationId() + " "
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
                        for (Account account : activeUsersList) {
                            if (database.getConversationById(convoID).getParticipants().get(i).getUsername()
                                    .equals(account.getUsername())) {
                                for (ServerProcess serverProcess : serverProcessList) {
                                    if (serverProcess.getCurrentAccount().getUsername().equals(database
                                            .getConversationById(convoID).getParticipants().get(i).getUsername())) {
                                        sendOtherClient(
                                                "addMsg " + convoID + " " + tempMessage.getId() + " "
                                                        + this.currentAccount.getUsername() + " "
                                                        + tempMessage.getTimestamp() + " " + newMessageContent,
                                                serverProcess.getClientSocket());
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
                    try {
                        Message messageToEdit = database.getMessageById(initialID);
                        messageToEdit.editMessage(newMessageEdit);
                        for (int i = 0; i < database.getConversationById(convoIdentifier).getParticipants().size();
                             i++) {
                            for (Account account : activeUsersList) {
                                if (database.getConversationById(convoIdentifier).getParticipants().get(i).getUsername()
                                        .equals(account.getUsername())) {
                                    for (ServerProcess serverProcess : serverProcessList) {
                                        if (serverProcess.getCurrentAccount().getUsername()
                                                .equals(database.getConversationById(convoIdentifier).getParticipants()
                                                        .get(i).getUsername()))
                                            sendOtherClient(
                                                    "editMsg " + convoIdentifier + " " + initialID + " "
                                                            + currentAccount.getUsername()
                                                            + " " + messageToEdit.getTimestamp() + " "
                                                            + newMessageEdit,
                                                    serverProcess.getClientSocket());

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
                            for (Account account : activeUsersList) {
                                if (database.getConversationById(convoIdent).getParticipants().get(i).getUsername()
                                        .equals(account.getUsername())) {
                                    for (ServerProcess serverProcess : serverProcessList) {
                                        if (serverProcess.getCurrentAccount().getUsername().equals(database
                                                .getConversationById(convoIdent).getParticipants().get(i).getUsername()))
                                            sendOtherClient("removeMsg " + convoIdent + " " + messageIdentifier,
                                                    serverProcess.getClientSocket());
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

        for (String accountUsernameString : accountUsernameStrings) {
            for (int j = 0; j < finalAccountUsernameStrings.size(); j++) {
                if (!finalAccountUsernameStrings.contains(accountUsernameString)) {
                    finalAccountUsernameStrings.add(accountUsernameString);
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

        sendClient("prepareForDataDump " + numAccsToSend + " " + numConvosToSend + " " + numMsgsToSend);

        for (String finalAccountUsernameString : finalAccountUsernameStrings) {
            sendClient(finalAccountUsernameString);
        }
        for (String conversationString : conversationStrings) {
            sendClient(conversationString);
        }
        for (String messageString : messageStrings) {
            sendClient(messageString);
        }
    }

    public Account checkUserLogin(String username, String password) throws AccountNotExistException {

        ArrayList<Account> accounts = database.getAccounts();

        for (Account account : accounts) {
            if (account.getUsername().equals(username)) {
                if (account.getPassword().equals(password)) {
                    return account;
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

    public void sendOtherClient(String message, Socket clientSocket) {
        try {
            OutputStream outputStreamSend = clientSocket.getOutputStream();
            PrintWriter sendWriter = new PrintWriter(outputStreamSend);

            sendWriter.write(message);
            sendWriter.println();
            sendWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }
}