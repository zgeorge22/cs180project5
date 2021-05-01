package src;

import java.util.ArrayList;
import java.time.LocalDateTime;
import javax.swing.JOptionPane;

public class Client {
    private MainWindow mw;
    private Database db;
    private String username;

    TestServer server; // TEMPORARY

    public Client() {
        mw = new MainWindow(this);
        db = new Database(false);

        // Login stuff

        // ----- REMOVE later! once server socket established -----
        username = "Zach";
        server = new TestServer(this);
        // --------------------------------------------------------
    }

    public boolean sendServer(String command) {
        boolean status = false;

        // SEND SERVER: command + " " details
        // if recieve back "succeeded" return true
        // else if receive back "failed" return false
        // else throw unknown server message error

        return status;
    }

    public boolean requestCreateConvo(String participantsString, String initialMsg) {
        participantsString = participantsString.replaceAll("\\s", "");
        // VALIDATE characters better!
        participantsString = username + "," + participantsString;

        System.out.println(
                "CLIENT - Requested createConvo for [" + participantsString + "] with initialMsg [" + initialMsg + "]");

        // ****************************** REMOVE later! ******************************
        // server won't need username, client handler will know sender
        return server.receivedCreateConvo(participantsString, username, initialMsg);

        // return sendServer("createConvo " + participantsString + " " + initialMsg);
    }

    public boolean requestCreateMsg(Conversation conversation, String content) {
        System.out.println("CLIENT - Requested createMsg for conversationID [" + conversation.getConversationId()
                + "] with content [" + content + "]");

        // ****************************** REMOVE later! ******************************
        // server won't need username, client handler will know sender
        return server.receivedCreateMessage(conversation.getConversationId(), username, content);

        // return sendServer("createMsg " + conversation.getConversationId() + " " +
        // content);
    }

    public boolean requestEditMsg(Conversation conversation, Message message, String content) {
        System.out.println("CLIENT - Requested editMsg for conversationID [" + conversation.getConversationId()
                + "] and messageID [" + message.getId() + "] with content [" + content + "]");

        // ****************************** REMOVE later! ******************************
        // server won't need username, client handler will know sender
        return server.receivedEditMsg(conversation.getConversationId(), message.getId(), username, content);

        // return sendServer("editMsg " + conversation.getConversationId() + " " +
        // message.getId() + " " + content);
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

        Message message;
        try {
            message = db.getMessageById(messageID);
            message.editMessage(content); // update timestamp as well?
            mw.updateMsgEntry(message);
        } catch (MessageNotFoundException e1) {
            System.out.println("MessageID " + conversationID + " does not exist!");
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