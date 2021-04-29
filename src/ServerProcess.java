//package com.company;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
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
        } catch (IOException | AccountNotExistException | UsernameAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

    private void clientProcess() throws IOException, AccountNotExistException, UsernameAlreadyExistsException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());

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


        messagingProcess(userAccount);


        clientSocket.close();
        ServerBackground.removeUser(userAccount);
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
    */
    public Account createAccount(String username, String password) throws UsernameAlreadyExistsException {
        return new Account(username, password, true);
    }

    public void messagingProcess(Account currentAccount) throws IOException, AccountNotExistException, UsernameAlreadyExistsException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
        ArrayList<Account> activeUsersList = ServerBackground.getActiveUsers();
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
                Account exitingAccount= checkUserLogin(existingUsername, existingPassword);
                this.currentAccount = exitingAccount;
                activeUsersList.add(exitingAccount);
                break;
            case("editUsername"):
                String editUsername = token[1];
                this.currentAccount.changeUsername(editUsername);
                break;
            case("editPassword"):
                String editPassword = token[1];
                this.currentAccount.changePassword(editPassword);
                break;
            case("createConvo"):
                String participantsString = token[1];
                String[] participantsUsernameList = participantsString.split(",");
                ArrayList<Account> newConvoAccountList = new ArrayList<>();
                for (String username: participantsUsernameList) {
                    for (Account activeUser: activeUsersList) {
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
            case("leaveConvo"):
                try {
                    int leaveConversationID = Integer.valueOf(token[1]);
                    Database.getConversationById(leaveConversationID).removeParticipant(this.currentAccount);
                    break;
                } catch (ConversationNotFoundException e) {
                    e.printStackTrace();
                }
        }
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
                if (server.getName() == user1.getUsername()) { //Change to send message to correct location
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