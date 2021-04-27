package com.company;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ServerProcess extends Thread {

    private final Socket clientSocket;
    private OutputStream outputStream;
    private ArrayList<String> usernameList;
    private ArrayList<String> passwordList;
    private Server server;

    public ServerProcess(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.usernameList = new ArrayList<>(Arrays.asList("John", "Anna", "Pete"));
        this.passwordList = new ArrayList<>(Arrays.asList("1234", "5678", "1357"));

    }

    public void run() {
        try {
            //clientProcess();
            messagingProcess();
        } catch (IOException | AccountNotExistException e) {
            e.printStackTrace();
        }
    }

    private void clientProcess() throws IOException, AccountNotExistException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());

        String loginOption = reader.readLine();
        boolean loggedIn = false;
        Account userAccount = new Account("PH", "PH");


        if (loginOption.equals("0")) {
            String username = reader.readLine();
            String password = reader.readLine();
            userAccount = accountLogin(username, password);
            loggedIn = true;
            ArrayList<Account> activeUsers = ServerBackground.getActiveUsers();
        } else if (loginOption.equals("1")) {
            String username = reader.readLine();
            String password = reader.readLine();
            userAccount.setUsername(username);
            userAccount.setPassword(password);
            loggedIn = true;
        }

        if (loggedIn && !userAccount.getUsername().equals("PH") && !userAccount.getPassword().equals("PH")) {
            pw.write("User logged in " + userAccount.getUsername());
            pw.println();
            pw.flush();
            ServerBackground.addUser(userAccount);
        }

        clientSocket.close();
        ServerBackground.removeUser(userAccount);
    }

    private Account accountLogin(String username, String password) throws AccountNotExistException {

        Account checkAccount = Database.getAccountByUsername(username);
        //if (!password.equals(checkAccount.getPassword())) {
          //  //TODO re-enter password
        //}

        // temporary method
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

    public Account createAccount(String username, String password) {
        return new Account(username, password);
    }

    // temporary method
    public void messagingProcess() throws IOException, AccountNotExistException{
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
        String username = bfr.readLine();
        String password = bfr.readLine();
        String messageUser = bfr.readLine();
        String message = bfr.readLine();
        Account currentAccount = createAccount(username, password);
        ArrayList<Account> accountList = ServerBackground.getActiveUsers();
        accountList.add(currentAccount);

        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).getUsername().equals(messageUser)) {
                sendDirectMessage(accountList.get(i), message);
            }
        }



    }
    public void sendDirectMessage(Account user1, String message) {
        ArrayList<ServerProcess> serverList = Server.getServerList();
        try {
            for (ServerProcess server : serverList) {
                if (server.getName() == user1.getUsername()) {
                    outputStream.write(message.getBytes());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Account checkUserLogin(String username, String password) throws AccountNotExistException{
        ArrayList<String> usernameList = new ArrayList<>(Arrays.asList("John", "Anna", "Pete"));
        ArrayList<String> passwordList = new ArrayList<>(Arrays.asList("1234", "5678", "1357"));
        if (usernameList.contains(username) && passwordList.contains(password)
                && (usernameList.indexOf(username) == passwordList.indexOf(password))) {
            return (new Account(username, password));
        } else {
            throw new AccountNotExistException();
        }
    }

}
