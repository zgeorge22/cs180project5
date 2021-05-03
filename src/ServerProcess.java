import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class is a processing class that spawns a thread each time a user logs
 * in from their client. Then, this thread handles all server-side processing
 * related to that client.
 *
 * <p>
 * Purdue University -- CS18000 -- Spring 2021 -- Project 5
 * </p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack
 * Dorkin
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

    // Runs clientProcess to handle all client requests. Closes the clientSocket and notifies the terminal when a user
    // disconnects.
    public void run() {
        try {
            clientProcess();
            clientSocket.close();
            System.out.println("SERVER - Client Disconnected: " + clientSocket.getPort());
        } catch (IOException | ConversationNotFoundException | MessageNotFoundException e) {
            e.printStackTrace();
        }
    }

    // clientProcess allows the user to log in or create an account before starting the messagingProcess method.
    private void clientProcess() throws IOException, ConversationNotFoundException, MessageNotFoundException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        writer = new PrintWriter(outputStream);

        boolean loggedIn = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<Account> activeUsersList = ServerBackground.getActiveUsers();

        loginLoop:
        do {
            // Reads in a command from the client and splits it into tokens which provide all needed information.
            String commandString = (reader.readLine());
            String[] tokenLogin = commandString.split(" ");
            String cmdLogin = tokenLogin[0];

            switch (cmdLogin) {
                // This case allows users to create a new account via new username and password.
                case ("createAccount") -> {
                    String newUsername = tokenLogin[1];
                    String newPassword = tokenLogin[2];
                    System.out
                            .println("SERVER (" + clientSocket.getPort() + ") - Requested createAccount with username ["
                                    + newUsername + "] and passowrd [" + newPassword + "]");
                    try {
                        currentAccount = new Account(newUsername, newPassword, database, true);
                        activeUsersList.add(currentAccount);
                        loggedIn = true;

                        sendClient("createAccountSuccessful " + newUsername);
                    } catch (UsernameAlreadyExistsException e) {
                        sendClient("createAccountFailed");
                    }
                }
                // This case allows users to login to a pre-existing account by providing the proper username and
                // password associated with the account.
                case ("loginAccount") -> {
                    String existingUsername = tokenLogin[1];
                    String existingPassword = tokenLogin[2];

                    System.out
                            .println("SERVER (" + clientSocket.getPort() + ") - Requested loginAccount with username ["
                                    + existingUsername + "] and passowrd [" + existingPassword + "]");
                    try {
                        currentAccount = checkUserLogin(existingUsername, existingPassword);
                        activeUsersList.add(currentAccount);

                        loggedIn = true;

                        sendClient("loginAccountSuccessful " + existingUsername);
                    } catch (AccountNotExistException e) {
                        sendClient("loginFailed");
                    }
                }
                // This case allows the user to cancel the login process and exit the program.
                case ("cancelLogin") -> {
                    System.out.println("SERVER (" + clientSocket.getPort() + ") - Client cancelled login");
                    break loginLoop;
                }
                // The default case handles all unknown commands by sending an error message to the server terminal.
                default -> {
                    System.out.println("ERROR - Unknown command: " + cmdLogin);
                }
            }

        } while (!loggedIn);

        // loggedIn will only become true if a user has either logged in or created an account successfully.
        // Once this is true, messaging process is able to start.
        if (loggedIn) {
            messagingProcess(activeUsersList);
        }

        // Closing the writer will occur only once messagingProcess has ended.
        writer.close();
    }

    // messagingProcess handles all client processes after logging in. This includes sending messages, creating or
    // leaving conversations, editing and deleting messages, editing each user's account, and logging off.
    public void messagingProcess(ArrayList<Account> activeUsersList) throws IOException, ConversationNotFoundException {

        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        // Boolean exit will only become true when user selects the logoutAccount case. This exit boolean will then
        // break the loop and exit the client.
        boolean exit = false;

        // Sends the client all user data if the user has any existing conversations.
        if (this.currentAccount.getConversationIds().size() != 0) {
            dataDump();
        } else
            sendClient("prepareForDataDump 0 0 0");

        // do-while loop will repeat until user logs off. This loop contains all needed commands and actions for
        // interacting with the client.
        do {
            // Creates a BufferedReader and PrintWriter to read in and send out all needed commands.
            BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            String commandString = (bfr.readLine());

            System.out.println("SERVER (" + clientSocket.getPort() + ") - " + commandString);

            // Collects command from the client and tokenizes it for proper functionality.
            String[] token = commandString.split(" ");
            String cmd = token[0];
            ArrayList<ServerProcess> serverProcessList = Server.getServerList();
            switch (cmd) {

                // case allows the user to change their password.
                case ("editPassword"):
                    String editPassword = token[1];
                    currentAccount.changePassword(editPassword);

                    System.out.println("SERVER (" + clientSocket.getPort() + ") - Received editPassword to ["
                            + editPassword + "]");
                    break;

                // case allows user to create a new conversation.
                case ("createConvo"):
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
                                            sendOtherClient(
                                                    String.format("addConvo %d %s", newConvoID, participantsString),
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
                    System.out.println("SERVER (" + clientSocket.getPort() + ") - Received createConvo for ["
                            + participantsString + "] with initialMsg [" + initialMsg + "] from ["
                            + currentAccount.getUsername() + "]");
                    break;

                // case allows for users to leave a conversation that they are currently a part of.
                case ("leaveConvo"):
                    try {
                        int leaveConversationID = Integer.parseInt(token[1]);
                        Conversation conversation = database.getConversationById(leaveConversationID);
                        for (int i = 0; i < conversation.getParticipants().size(); i++) {
                            for (Account account : activeUsersList) {
                                if (conversation.getParticipants().get(i).getUsername().equals(account.getUsername())) {
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

                        System.out.println("SERVER (" + clientSocket.getPort()
                                + ") - Received leaveConvo from conversationID [" + leaveConversationID + "]");

                        break;
                    } catch (ConversationNotFoundException | AccountNotExistException e) {
                        e.printStackTrace();
                    }
                    // case allows for a user to create and send a new message.
                case ("createMsg"):
                    System.out.println("SERVER (" + clientSocket.getPort() + ") - Received createMsg...");

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
                // case allows a user to edit a message that they sent
                case ("editMsg"):
                    System.out.println("SERVER (" + clientSocket.getPort() + ") - Received editMsg...");
                    int convoIdentifier = Integer.parseInt(token[1]);
                    int initialID = Integer.parseInt(token[2]);
                    String placeH = commandString.substring(commandString.indexOf(" ") + 1);
                    String placeHo = placeH.substring(placeH.indexOf(" ") + 1);
                    String newMessageEdit = placeHo.substring(placeHo.indexOf(" ") + 1);
                    try {
                        Message messageToEdit = database.getMessageById(initialID);
                        messageToEdit.editMessage(newMessageEdit);
                        for (int i = 0; i < database.getConversationById(convoIdentifier).getParticipants()
                                .size(); i++) {
                            for (Account account : activeUsersList) {
                                if (database.getConversationById(convoIdentifier).getParticipants().get(i).getUsername()
                                        .equals(account.getUsername())) {
                                    for (ServerProcess serverProcess : serverProcessList) {
                                        if (serverProcess.getCurrentAccount().getUsername()
                                                .equals(database.getConversationById(convoIdentifier).getParticipants()
                                                        .get(i).getUsername()))
                                            sendOtherClient(
                                                    "editMsg " + convoIdentifier + " " + initialID + " "
                                                            + currentAccount.getUsername() + " "
                                                            + messageToEdit.getTimestamp() + " " + newMessageEdit,
                                                    serverProcess.getClientSocket());

                                    }
                                }
                            }
                        }
                    } catch (MessageNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;

                // case allows a user to delete a message that they sent
                case ("deleteMsg"):
                    System.out.println("SERVER (" + clientSocket.getPort() + ") - Received deleteMsg...");

                    int convoIdent = Integer.parseInt(token[1]);
                    int messageIdentifier = Integer.parseInt(token[2]);
                    try {
                        database.getMessageById(messageIdentifier).deleteMessage();
                        for (int i = 0; i < database.getConversationById(convoIdent).getParticipants().size(); i++) {
                            for (Account account : activeUsersList) {
                                if (database.getConversationById(convoIdent).getParticipants().get(i).getUsername()
                                        .equals(account.getUsername())) {
                                    for (ServerProcess serverProcess : serverProcessList) {
                                        if (serverProcess.getCurrentAccount().getUsername()
                                                .equals(database.getConversationById(convoIdent).getParticipants()
                                                        .get(i).getUsername()))
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

                // case allows a user to logout.
                case ("logoutAccount"):
                    System.out.println("SERVER (" + clientSocket.getPort() + ") - Received logoutAccount");

                    ServerBackground.activeUsers.remove(this.currentAccount);
                    pw.write("logoutTrue");
                    pw.println();
                    pw.flush();
                    exit = true;
                    break;
                // default case recognizes an unknown command and sends an error message to the server terminal.
                default:
                    System.out.println("ERROR - Unknown command: " + cmd);
            }
        } while (!exit);
    }

    //dataDump sends the current user all of their past information. This method will not activate for any new users.
    private void dataDump() {

        // Creates a new database to add all of the clients data.
        Database clientData = new Database(false);
        // Adds all conversations the client is a part of into the clientData database.
        for (int i = 0; i < database.getConversations().size(); i++) {
            if (database.getConversations().get(i).getParticipants().contains(this.currentAccount))
                clientData.addToDatabase(database.getConversations().get(i));
        }

        // Creates an ArrayList to collect all users the current client is associated with.
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

        ArrayList<String> conversationStrings = new ArrayList<>();
        for (int i = 0; i < clientData.getConversations().size(); i++) {
            String conversationString = Integer.toString(clientData.getConversations().get(i).getConversationId());
            conversationString = conversationString + " ";

            String thisConversationParticipants = "";
            for (int j = 0; j < clientData.getConversations().get(i).getParticipants().size(); j++) {
                thisConversationParticipants = thisConversationParticipants
                        + clientData.getConversations().get(i).getParticipants().get(j).getUsername() + ",";
            }
            thisConversationParticipants = thisConversationParticipants.substring(0,
                    thisConversationParticipants.length() - 1);

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

        // Creates a count of accounts, conversations, and messages that are being sent over to the client.
        int numAccsToSend = finalAccountUsernameStrings.size();
        int numConvosToSend = conversationStrings.size();
        int numMsgsToSend = messageStrings.size();

        // Sends the client the "prepareForDataDump" command and the number of accounts, conversations, and messages
        // that will be sent.
        sendClient("prepareForDataDump " + numAccsToSend + " " + numConvosToSend + " " + numMsgsToSend);

        // Sends the client all accounts, messages, and conversations
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

    // checkUserLogin makes sure that a user's username and password are correct and correlate to a current user ID.
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

    // getAccount returns the account active with a given ServerProcess thread.
    public Account getAccount() {
        return currentAccount;
    }

    // sendClient sends a message to the client who is connected to this thread
    public void sendClient(String message) {
        writer.write(message);
        writer.println();
        writer.flush();
    }

    // sendOtherClient sends a message to users connected to other threads of ServerProcess.
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

    // getClientSocket returns the client socket of a specific user.
    public Socket getClientSocket() {
        return clientSocket;
    }

    // getCurrentAccount returns the active account within a specific ServerProcess.
    public Account getCurrentAccount() {
        return currentAccount;
    }
}