package src;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;


public class Client {
    private MainWindow mw;
    private Database db;
    private String username;

    private Scanner in;
    private PrintWriter out;


    public Client() {
        db = new Database(false);

        // Login stuff
    }

    public static void main(String[] args) {
       Client client = new Client();

       client.username = "jim"; //Do we need this?

        try {
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void run() throws IOException {
        Socket socket = new Socket("localhost", 4242);
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);

        login();

        ObjectInputStream ois = null;
        ois = new ObjectInputStream(socket.getInputStream());
        try {
            db = (Database) ois.readObject();
            //ois.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("Database Received Successfully");
        sendServer("Database Received Successfully");

        mw = new MainWindow(this);

        clientMessageLoop: while (in.hasNextLine()) {
            String fullCommand = in.nextLine();
            System.out.println(fullCommand);
            String[] token = fullCommand.split(" ");
            String inputcmd = token[0];
            String details = "";
            if (token.length > 1) {
                details = fullCommand.substring(fullCommand.indexOf(" ") + 1);
            }

            if (username != null) {
                switch (inputcmd) {
                    case ("addConvo"):
                        receivedAddConvo(details);
                        break;
                    case ("addMsg"):
                        receivedAddMsg(details);
                        break;
                    case ("removeUser"):
                        receivedRemoveUser(details);
                        break;
                    case ("editMsg"):
                        receivedEditMsg(details);
                        break;
                    case ("removeMsg"):
                        receivedRemoveMsg(details);
                        break;
                    case ("logoutTrue"):
                        mw.dispose();
                        username = null;
                        break clientMessageLoop;
                    case ("succeeded"):
                        System.out.println("succeeded");
                        break;
                    case ("failed"):
                        System.out.println("failed");
                        break;
                }
            }
        }
    }

    private void login() {
        String confirm;
        do {
            System.out.println("Enter loginAccount (0) or createAccount (1)"); //Testing Input
            Scanner scanner = new Scanner(System.in);
            String loginChoice = scanner.nextLine();
            String choice = null;
            if (loginChoice.equals("0"))
                choice = "loginAccount";
            else if (loginChoice.equals("1"))
                choice = "createAccount";
            System.out.println("Username: ");
            username = scanner.nextLine();
            System.out.println("Password: ");
            String password = scanner.nextLine();

            String login = getLogin(choice, username, password); //GUI connection
            out.write(login);
            out.println();
            out.flush();

            confirm = in.nextLine();

        } while(confirm.equals("false"));

    }

    private static String getLogin(String choice, String username, String password) {
        String loginInformation = choice + " " + username + " " + password;
        return loginInformation;
    }

    public boolean sendServer(String command) {
        out.write(command);
        out.println();
        out.flush();

//        String response = "RESPONSE";
//        System.out.println(response);
//        if (in.hasNextLine()) {
//            response = in.nextLine();
//            System.out.println(response);
//            if (response.equals("succeeded")) {
                return true;
//            }
//        }

//        return false;
    }

    public boolean requestEditPassword(String password) {
        System.out.println("CLIENT - Requested editPassword to [" + password + "]");

        return sendServer("editPassword " + password);
    }

    public boolean requestCreateConvo(String participantsString, String initialMsg) {
        participantsString = participantsString.replaceAll("\\s", "");
        // VALIDATE characters better!
        participantsString = username + "," + participantsString;

        System.out.println(
                "CLIENT - Requested createConvo for [" + participantsString + "] with initialMsg [" + initialMsg + "]");

        return sendServer("createConvo " + participantsString + " " + initialMsg);
    }

    public boolean requestLeaveConvo(Conversation conversation) {
        System.out.println("CLIENT - Requested leaveConvo for conversationID ["
                + conversation.getConversationId() + "]");

        return sendServer("leaveConvo " + conversation.getConversationId() + " " + username);
    }

    public boolean requestCreateMsg(Conversation conversation, String content) {
        System.out.println("CLIENT - Requested createMsg for conversationID [" + conversation.getConversationId()
                + "] with content [" + content + "]");

        return sendServer("createMsg " + conversation.getConversationId() + " " + content);
    }

    public boolean requestEditMsg(Conversation conversation, Message message, String content) {
        System.out.println("CLIENT - Requested editMsg for conversationID [" + conversation.getConversationId()
                + "] and messageID [" + message.getId() + "] with content [" + content + "]");

        return sendServer("editMsg " + conversation.getConversationId() + " "
                + message.getId() + " " + content);
    }

    public boolean requestDeleteMsg(Conversation conversation, Message message) {
        System.out.println("CLIENT - Requested deleteMsg for conversationID [" + conversation.getConversationId()
                + "] and messageID [" + message.getId() + "]");

        return sendServer("deleteMsg " + conversation.getConversationId() + " " + message.getId());
    }

    public boolean requestLogoutAccount() {
        return sendServer("logoutAccount");
    }

    public void receivedAddConvo(String details) {
        String placeholder = details;

        int conversationID = Integer.parseInt(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        String participantsString = placeholder;

        ArrayList<Account> participants = new ArrayList<Account>();
        for (String username : participantsString.split(",")) {
            try {
                participants.add(db.getAccountByUsername(username));
            } catch (AccountNotExistException e1) {
                try {
                    participants.add(new Account(username, "", db, false));
                } catch (UsernameAlreadyExistsException e2) {
                    e2.printStackTrace(); // should never happen!
                }
            }
        }
        String name = participants.size() > 2 ? "GC" : "DM";

        System.out.println("CLIENT - Received createConvo for [" + participantsString + "]");

        Conversation conversation;
        try {
            conversation = db.getConversationById(conversationID);

            System.out.println("ConversationID " + conversationID + " already exists!");
        } catch (ConversationNotFoundException e) {
            // Create new local conversation object
            conversation = new Conversation(conversationID, name, participants, db);

            mw.addNewChat(conversation);
        }
    }

    public void receivedRemoveUser(String details) {
        // Parse message details
        String placeholder = details;

        int conversationID = Integer.parseInt(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        String user = placeholder;

        System.out.println(
                "CLIENT - Received removeUser for conversationID [" + conversationID + "] from [" + user + "]");

        try {
            Conversation conversation = db.getConversationById(conversationID);
            conversation.removeParticipant(user);
            mw.updateChatEntry(conversation);
        } catch (ConversationNotFoundException e) {
            System.out.println("ConversationID " + conversationID + " does not exist!");
        } catch (AccountNotExistException e) {
            System.out.println("User " + user + " does not exist!");
        }
    }

    public void receivedAddMsg(String details) {
        // Parse message details
        String placeholder = details;

        int conversationID = Integer.parseInt(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        int messageID = Integer.parseInt(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        String sender = placeholder.substring(0, placeholder.indexOf(" "));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        LocalDateTime timestamp = LocalDateTime.parse(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        String content = placeholder;

        System.out.println("CLIENT - Received addMsg for conversationID [" + conversationID + "] and messageID ["
                + messageID + "] with content [" + content + "]");

        Message message;
        try {
            message = db.getMessageById(messageID);

            System.out.println("MessageID " + messageID + " already exists!");
        } catch (MessageNotFoundException e1) {
            // Create new local message object
            message = new Message(messageID, timestamp, sender, content, false, db);

            try {
                Conversation conversation = db.getConversationById(conversationID);
                conversation.addMessage(message);
                mw.updateChatEntry(conversation);
            } catch (ConversationNotFoundException e2) {
                System.out.println("ConversationID " + conversationID + " does not exist!");
            }
        }
    }

    public void receivedEditMsg(String details) {
        // Parse message details
        String placeholder = details;

        int conversationID = Integer.parseInt(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        int messageID = Integer.parseInt(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        String sender = placeholder.substring(0, placeholder.indexOf(" "));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        LocalDateTime timestamp = LocalDateTime.parse(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        String content = placeholder;

        System.out.println("CLIENT - Received editMsg for conversationID [" + conversationID + "] and messageID ["
                + messageID + "] with content [" + content + "]");

        try {
            Message message = db.getMessageById(messageID);
            message.editMessage(content); // update timestamp as well?
            mw.updateMsgEntry(message);

            Conversation conversation = db.getConversationById(conversationID);
            mw.updateChatEntry(conversation);
        } catch (MessageNotFoundException e1) {
            System.out.println("MessageID " + messageID + " does not exist!");
        } catch (ConversationNotFoundException e) {
            System.out.println("ConversationID " + conversationID + " does not exist!");
        }
    }

    public void receivedRemoveMsg(String details) {
        // Parse message details
        String placeholder = details;

        int conversationID = Integer.parseInt(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        int messageID = Integer.parseInt(placeholder);

        System.out.println("CLIENT - Received removeMsg for conversationID [" + conversationID + "] and messageID ["
                + messageID + "]");

        try {
            Message message = db.getMessageById(messageID);
            db.removeMessageById(messageID);
            mw.removeMsgEntry(message);

            Conversation conversation = db.getConversationById(conversationID);
            mw.updateChatEntry(conversation);
        } catch (MessageNotFoundException e1) {
            System.out.println("MessageID " + messageID + " does not exist!");
        } catch (ConversationNotFoundException e) {
            System.out.println("ConversationID " + conversationID + " does not exist!");
        }
    }

    public MainWindow getMainWindow() {
        return mw;
    }

    public void setMainWindow(MainWindow mw) {
        this.mw = mw;
    }

    public Database getDatabase() {
        return db;
    }

    public void setDatabase(Database db) {
        this.db = db;
    }

    public String getUsername() {
        return username;
    }
}