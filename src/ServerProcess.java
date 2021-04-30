//package com.company;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ServerProcess extends Thread {

    private final Socket clientSocket;
    private OutputStream outputStream;
    //private ArrayList<String> usernameList;
    //private ArrayList<String> passwordList;
    //private Server server;
    private String userID;
    private Account currentAccount;

    public ServerProcess(Socket clientSocket) {
        this.clientSocket = clientSocket;
        //this.server = server;
        //this.usernameList = new ArrayList<>(Arrays.asList("John", "Anna", "Pete"));
        //this.passwordList = new ArrayList<>(Arrays.asList("1234", "5678", "1357"));

    }

    public void run() {
        try {
            clientProcess();
        } catch (IOException | AccountNotExistException | UsernameAlreadyExistsException | ConversationNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void clientProcess() throws IOException, AccountNotExistException, UsernameAlreadyExistsException, ConversationNotFoundException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        Boolean loggedIn = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter print = new PrintWriter(clientSocket.getOutputStream());
        ArrayList<Account> activeUsersList = ServerBackground.getActiveUsers();
        String commandString = (reader.readLine());
        String[] tokenLogin = commandString.split(" ");
        String cmdLogin = tokenLogin[0];
        switch (cmdLogin) {
            case ("createAccount"):
                String newUsername = tokenLogin[1];
                String newPassword = tokenLogin[2];
                Account newAccount = new Account(newUsername, newPassword, true);
                this.currentAccount = newAccount;
                activeUsersList.add(newAccount);
                loggedIn = true;
                break;
            case ("loginAccount"):
                String existingUsername = tokenLogin[1];
                String existingPassword = tokenLogin[2];
                Account exitingAccount = checkUserLogin(existingUsername, existingPassword);
                this.currentAccount = exitingAccount;
                activeUsersList.add(exitingAccount);
                loggedIn = true;
                break;
        }

        /*
        String loginOption = reader.readLine();
        boolean loggedIn = false;
        Account userAccount = new Account("PH", "PH", false);


        if (loginOption.equals("Login")) {
            String username = reader.readLine();
            String password = reader.readLine();
            userAccount = checkUserLogin(username, password);
            loggedIn = true;
            ArrayList<Account> activeUsersList = ServerBackground.getActiveUsers();
        } else if (loginOption.equals("CreateAccount")) {
            String username = reader.readLine();
            String password = reader.readLine();
            userAccount = createAccount(username, password);
            loggedIn = true;
        }

        if (loggedIn && !userAccount.getUsername().equals("PH") && !userAccount.getPassword().equals("PH")) {
            pw.write("User logged in " + userAccount.getUsername());
            pw.println();
            pw.flush();
            ServerBackground.addUser(userAccount);
        }
         */
        ArrayList<Conversation> userConversations = new ArrayList<>();
        for (int i = 0; i < Database.conversations.size(); i++) {
            if (Database.conversations.get(i).getParticipants().contains(this.currentAccount))
                userConversations.add(Database.conversations.get(i));
        }

        ObjectOutputStream outputObjectStream = new ObjectOutputStream(clientSocket.getOutputStream());
        outputObjectStream.writeObject(userConversations);
        outputObjectStream.flush();
        outputObjectStream.close();

        if (loggedIn) {
            messagingProcess(activeUsersList);
        }

        clientSocket.close();

    }
    /*
    private Account accountLogin(String username, String password) throws AccountNotExistException {

        Account checkAccount = Database.getAccountByUsername(username);

        if (usernameList.contains(username) && passwordList.contains(password)) {
            if (usernameList.indexOf(username) == passwordList.indexOf(password)) {
                return checkAccount;
            } else {
                throw new AccountNotExistException();
            }
        } else {
            throw new AccountNotExistException();
        }
    }

    public Account createAccount(String username, String password) throws UsernameAlreadyExistsException {
        return new Account(username, password, true);
    }
    */


    public void messagingProcess(ArrayList<Account> activeUsersList) throws IOException, AccountNotExistException, UsernameAlreadyExistsException, ConversationNotFoundException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        boolean exit = false;

        do {
            BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            String commandString = (bfr.readLine());
            String[] token = commandString.split(" ");
            String cmd = token[0];
            switch (cmd) {
                case ("createAccount"):
                    String newUsername = token[1];
                    String newPassword = token[2];
                    Account newAccount = new Account(newUsername, newPassword, true);
                    this.currentAccount = newAccount;
                    activeUsersList.add(newAccount);
                    break;
                case ("loginAccount"):
                    String existingUsername = token[1];
                    String existingPassword = token[2];
                    Account exitingAccount = checkUserLogin(existingUsername, existingPassword);
                    this.currentAccount = exitingAccount;
                    activeUsersList.add(exitingAccount);
                    break;
                case ("editUsername"):
                    String editUsername = token[1];
                    this.currentAccount.changeUsername(editUsername);
                    break;
                case ("editPassword"):
                    String editPassword = token[1];
                    this.currentAccount.changePassword(editPassword);
                    break;
                case ("createConvo"):
                    String participantsString = token[1];
                    String[] participantsUsernameList = participantsString.split(",");
                    ArrayList<Account> newConvoAccountList = new ArrayList<>();
                    for (String username : participantsUsernameList) {
                        for (Account activeUser : activeUsersList) {
                            if (activeUser.getUsername().equals(username)) {
                                newConvoAccountList.add(activeUser);
                            }
                        }
                    }
                    String initialMsg = token[2];
                    Conversation newConvo = new Conversation(null, newConvoAccountList, true);
                    // TODO: send initialMsg to users in newConvo
                    // TODO: send commands to client
                    break;
                case ("leaveConvo"):
                    try {
                        int leaveConversationID = Integer.parseInt(token[1]);
                        Database.getConversationById(leaveConversationID).removeParticipant(this.currentAccount);
                        break;
                    } catch (ConversationNotFoundException e) {
                        e.printStackTrace();
                    }
                case ("createMsg"):
                    //TODO - Content
                    break;
                case ("sendMsg"):
                    int convoID = Integer.parseInt(token[1]);
                    String placeholder = commandString.substring(commandString.indexOf(" ") + 1);
                    String newMessageContent = placeholder.substring(placeholder.indexOf(" ") + 1);
                    Database.getConversationById(convoID).addMessage(new Message(LocalDateTime.now(), this.userID,
                            newMessageContent));
                    for (int i = 0; i < Database.getConversationById(convoID).getParticipants().size(); i++) {
                        for (int j = 0; j < activeUsersList.size(); j++) {
                            if (Database.getConversationById(convoID).getParticipants().get(i).getUsername().equals
                                    (activeUsersList.get(j))) {
                                sendDirectMessage(activeUsersList.get(j), "addMsg " + convoID + " " +
                                        Database.getMessages().get(Database.getMessages().size()).getId() + " " +
                                        this.userID + " " +
                                        Database.getMessages().get(Database.getMessages().size()).getTimestamp()
                                        + " " + newMessageContent);
                            }
                        }
                    }
                    break;
                case ("editMsg"):
                    int convoIdentifier = Integer.parseInt(token[1]);
                    int initialID = Integer.parseInt(token[2]); //TODO - Change to message content
                    String placeH = commandString.substring(commandString.indexOf(" ") + 1);
                    String placeHo = placeH.substring(placeH.indexOf(" ") + 1);
                    String newMessageEdit = placeHo.substring(placeHo.indexOf(" ") + 1);
                    LocalDateTime dateTime = null;
                    for (int i = 0; i < Database.getMessages().size(); i++) {
                        if (initialID == Database.getMessages().get(i).getId()) {
                            Database.getMessages().get(i).setContent(newMessageEdit);
                            Database.getMessages().get(i).setTimestamp(LocalDateTime.now());
                            dateTime = Database.getMessages().get(i).getTimestamp();
                        }
                    }
                    for (int i = 0; i < Database.getConversationById(convoIdentifier).getParticipants().size(); i++) {
                        for (int j = 0; j < activeUsersList.size(); j++) {
                            if (Database.getConversationById(convoIdentifier).getParticipants().get(i).getUsername()
                                    .equals(activeUsersList.get(j))) {
                                sendDirectMessage(activeUsersList.get(j), "editMsg " + convoIdentifier + " " +
                                        initialID + " " + this.userID + " " + dateTime + " " + newMessageEdit);
                            }
                        }
                    }
                    break;
                case ("deleteMsg"):
                    int convoIdent = Integer.parseInt(token[1]);
                    int messageIdentifier = Integer.parseInt(token[2]);
                    Database.getConversationById(convoIdent).deleteMessage(messageIdentifier);
                    for (int i = 0; i < Database.getConversationById(convoIdent).getParticipants().size(); i++) {
                        for (int j = 0; j < activeUsersList.size(); j++) {
                            if (Database.getConversationById(convoIdent).getParticipants().get(i).getUsername()
                                    .equals(activeUsersList.get(j))) {
                                sendDirectMessage(activeUsersList.get(j), "removeMsg " + convoIdent + " " +
                                        messageIdentifier);
                            }
                        }
                    }
                    break;
                case ("logoutAccount"):
                    ServerBackground.activeUsers.remove(this.currentAccount); //Might be a duplicate of code in run
                    exit = true;
                    break;
            }
        }while(!exit);
/*
        int conversationID = Integer.parseInt(bfr.readLine());
        String message = bfr.readLine();
        Account currentAccount = createAccount(username, password);

        accountList.add(currentAccount);

        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).getUsername().equals(conversationID)) {
                sendDirectMessage(accountList.get(i), message);
            }
        }

        for (int i = 0; i < Database.conversations.size(); i++) {
            if (Database.conversations.get(i).getConversationId() == (conversationID)) {
                Database.conversations.get(i).addMessage(new Message(null, currentAccount.getUsername(),
                        message)); //TODO - Update once timestamps work in Message.java
                for (int j = 0; j < Database.conversations.get(i).getParticipants().size(); j++) {
                    for (int k = 0; k < activeUsersList.size(); k++) {
                        if (Database.conversations.get(i).getParticipants().get(j) == activeUsersList.get(k)) {
                            sendDirectMessage(Database.conversations.get(i).getParticipants().get(j), message);
                        }
                    }
                }
            }
        }

 */
    }

    public void sendDirectMessage(Account user1, String message) {
        ArrayList<ServerProcess> serverList = Server.getServerList();
        try {
            for (ServerProcess server : serverList) {
                if (server.getName() == user1.getUsername()) { //TODO - Change to send message to correct location
                    outputStream.write(message.getBytes());

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Account checkUserLogin(String username, String password) throws AccountNotExistException, UsernameAlreadyExistsException {
        ArrayList<String> usernameList = new ArrayList<>(Arrays.asList("John", "Anna", "Pete")); //Temp users
        ArrayList<String> passwordList = new ArrayList<>(Arrays.asList("1234", "5678", "1357")); //Temp users
        ArrayList<Account> userList = Database.accounts;
        ArrayList<String> databaseUsernames = new ArrayList<>();
        ArrayList<String> databasePasswords = new ArrayList<>();

        for (int i = 0; i < Database.accounts.size(); i++) {
            databaseUsernames.add(Database.accounts.get(i).getUsername());
            databasePasswords.add(Database.accounts.get(i).getPassword());
        }

        /*
        if (usernameList.contains(username) && passwordList.contains(password) //Temp Login *
                && (usernameList.indexOf(username) == passwordList.indexOf(password))) {
            return (new Account(username, password, true));
        } else {
            throw new AccountNotExistException(); // *
        }
        */

        if (databaseUsernames.contains(username) && databasePasswords.contains(password) //Final login
                && (databaseUsernames.indexOf(username) == databasePasswords.indexOf(password))) {
            return (new Account(username, password, true));
        } else {
            throw new AccountNotExistException();
        }

    }

    public Account getAccount() {
        return currentAccount;
    }

}