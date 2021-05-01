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

    public void requestNewChat(String participantsString, String content) {
        participantsString = participantsString.replaceAll("\\s", "");
        // VALIDATE characters!

        System.out.println("CLIENT - Create Chat: " + participantsString + " - Send: " + content);

        // ----- REMOVE later! call broadcast function -----
        boolean successful = server.receivedNewChat(participantsString, content);
        // -------------------------------------------------

        if (successful) {
            mw.clearComposeMessage();
        } else {
            JOptionPane.showMessageDialog(null, "Unable to send message!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void receivedNewChat(int conversationID) {
        // NEED TO BE ABLE TO CONSTRUCT A LOCAL CONVERSATION OBJECT THAT DOESN'T NEED
        // ALL THE ACCOUNT OBJECTS (which require usernames and passwords to construct)
    }

    public void requestSendMessage(Conversation conversation, String content) {
        System.out.println("CLIENT - Send " + conversation.getConversationName() + ": " + content);

        // ----- REMOVE later! call broadcast function -----
        // server won't need username, client handler will know sender
        boolean successful = server.receivedMessage(conversation.getConversationId(), username, content);
        // -------------------------------------------------

        if (successful) {
            mw.clearComposeMessage();
        } else {
            JOptionPane.showMessageDialog(null, "Unable to send message!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void receivedMessage(int conversationID, int messageID, String sender, LocalDateTime timestamp,
            String content) {
        Message message;

        try {
            message = db.getMessageById(messageID);

            // QUESTION: should we update messages here??
            System.out.println("MessageID " + messageID + " already exists!");
        } catch (MessageNotFoundException e1) {
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
}