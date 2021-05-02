//package com.company;
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
        } catch (IOException | AccountNotExistException | UsernameAlreadyExistsException | ConversationNotFoundException
                | MessageNotFoundException e) {
            e.printStackTrace();
        }
    }

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

        Database clientData = new Database(false);
        for (int i = 0; i < database.getConversations().size(); i++) {
            if (database.getConversations().get(i).getParticipants().contains(this.currentAccount))
                clientData.addToDatabase(database.getConversations().get(i));
        }

        /*
        ObjectOutputStream outputObjectStream = new ObjectOutputStream(clientSocket.getOutputStream());
        outputObjectStream.flush();
        outputObjectStream.writeObject(clientData);
        outputObjectStream.flush();
        outputObjectStream.close(); TODO - Make object transfer work

         */


        if (loggedIn) {
            messagingProcess(activeUsersList);
        }

        clientSocket.close();
        writer.close();

    }

    public void messagingProcess(ArrayList<Account> activeUsersList) throws IOException, AccountNotExistException,
            UsernameAlreadyExistsException, ConversationNotFoundException, MessageNotFoundException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        boolean exit = false;

        do {
            BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            String commandString = (bfr.readLine());
            System.out.println(commandString);
            String[] token = commandString.split(" ");
            String cmd = token[0];
            ArrayList<ServerProcess> serverProcessList = Server.getServerList();
            switch (cmd) {

                case ("editPassword"):
                    String editPassword = token[1];
                    currentAccount.changePassword(editPassword);
                    break;
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
                case ("leaveConvo"):
                    try {
                        int leaveConversationID = Integer.parseInt(token[1]);
                        Conversation conversation = database.getConversationById(leaveConversationID);
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
                case ("createMsg"):
                    //Changed "sendMsg" to "createMsg"
                    int convoID = Integer.parseInt(token[1]);
                    String placeholder = commandString.substring(commandString.indexOf(" ") + 1);
                    String newMessageContent = placeholder.substring(placeholder.indexOf(" ") + 1);
                    Message tempMessage = new Message(this.currentAccount.getUsername(), newMessageContent, database);
                    database.getConversationById(convoID).addMessage(tempMessage);
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
                case ("logoutAccount"):
                    ServerBackground.activeUsers.remove(this.currentAccount);
                    pw.write("logoutTrue");
                    pw.println();
                    pw.flush();
                    exit = true;
                    break;
            }
        }while(!exit);
    }

    public Account checkUserLogin(String username, String password) throws AccountNotExistException, UsernameAlreadyExistsException {

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

    public Account getAccount() {
        return currentAccount;
    }

    public void send(String message, Socket clientSocket) throws IOException {
        OutputStream outputStreamSend = clientSocket.getOutputStream();
        writer = new PrintWriter(outputStreamSend);
        writer.write(message);
        writer.println();
        writer.flush();
        //writer.close();
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }
}