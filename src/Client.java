import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class is the client class which the client will run to launch the program. This client will connect with the
 * server and allow users to chat with each other, from their own clients.
 *
 * <p>Purdue University -- CS18000 -- Spring 2021 -- Project 5</p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack Dorkin
 * @version May 3rd, 2021
 */

public class Client {
    private Socket socket;

    private Database db;
    private LoginWindow lw;
    private MainWindow mw;

    private Scanner in;
    private PrintWriter out;

    private String username;

    public static void main(String[] args) {
        Client client = new Client();

        try {
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void run() throws IOException {
        socket = new Socket("localhost", 4242);
        System.out.println("Client - Connected to server");

        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);

        lw = new LoginWindow(this);

        serverMessageLoop:
        while (in.hasNextLine()) {
            String serverInput = in.nextLine();

            String command = serverInput;
            String details = "";
            if (serverInput.contains(" ")) {
                command = serverInput.substring(0, serverInput.indexOf(" "));
                details = serverInput.substring(serverInput.indexOf(" ") + 1);
            }

            if (username == null) {
                // User not logged in
                switch (command) {
                    case ("createAccountSuccessful"):
                        receivedSuccessfulLogin(details);
                        break;
                    case ("createAccountFailed"):
                        // reprompt user on login window
                        break;
                    case ("loginAccountSuccessful"):
                        receivedSuccessfulLogin(details);
                        break;
                    case ("loginFailed"):
                        // reprompt user on login window
                        break;
                    default:
                        System.out.println("ERROR - Not Logged In - Unknown command: " + command);
                        break;
                }
            } else {
                // User logged in
                switch (command) {
                    case ("prepareForDataDump"):
                        receivedPrepareForDataDump(details);
                        break;
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
                        break serverMessageLoop;
                    case ("succeeded"):
                        System.out.println("succeeded"); // REMOVE?
                        break;
                    case ("failedCreateConvo"):
                        System.out.println("ERROR - Invalid Conversation Participant"); // REMOVE?
                        break;
                    default:
                        System.out.println("ERROR - Logged In - Unknown command: " + command);
                        break;
                }
            }
        }

        System.out.println("Client - Disconnected from server");
    }

    public boolean sendServer(String command) {
        out.write(command);
        out.println();
        out.flush();

        return true;
    }

    // ===========================================================================
    // ----------------------- Request commands TO server ------------------------
    // ===========================================================================

    public boolean requestLoginAccount(String username, String password) {
        System.out.println(
                "CLIENT - Requested loginAccount with username [" + username + "] and passowrd [" + password + "]");

        return sendServer("loginAccount " + username + " " + password);
    }

    public boolean requestCreateAccount(String username, String password) {
        System.out.println(
                "CLIENT - Requested createAccount with username [" + username + "] and passowrd [" + password + "]");

        return sendServer("createAccount " + username + " " + password);
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
        System.out
                .println("CLIENT - Requested leaveConvo for conversationID [" + conversation.getConversationId() + "]");

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

    // ===========================================================================
    // ---------------------- Recieved commands FROM server ----------------------
    // ===========================================================================

    public void receivedPrepareForDataDump(String details) {

        System.out.println("CLIENT - Received prepareForDataDump");
        String placeholder = details;
        System.out.println(details);
        int numAccsSent = Integer.parseInt(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        int numConvosSent = Integer.parseInt(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        int numMsgsSent = Integer.parseInt(placeholder);

        db = new Database(false);

        try {
            for (int i = 0; i < numAccsSent; i++) {
                String accountDetails = in.nextLine();
                Account account = new Account(accountDetails, "", db, false);
            }
            for (int i = 0; i < numConvosSent; i++) {
                String convoDetails = in.nextLine();
                int convoId = Integer.parseInt(convoDetails.substring(0, convoDetails.indexOf(" ")));
                convoDetails = convoDetails.substring(convoDetails.indexOf(" ") + 1);
                ArrayList<Account> convoParticipants = new ArrayList<>();
                String[] accountsInConversation = convoDetails.split(",");
                for (String s : accountsInConversation) {
                    convoParticipants.add(db.getAccountByUsername(s));
                }
                String name = convoParticipants.size() > 2 ? "GC" : "DM";
                Conversation thisConversation = new Conversation(convoId, name, convoParticipants, db);
            }

            for (int i = 0; i < numMsgsSent; i++) {
                String msgDetails = in.nextLine();
                int convoId = Integer.parseInt(msgDetails.substring(0, msgDetails.indexOf(",")));
                msgDetails = msgDetails.substring(msgDetails.indexOf(",") + 1);
                String[] thisMessage = msgDetails.split(",");

                Message message = new Message(Integer.parseInt(thisMessage[0]), LocalDateTime.parse(thisMessage[1]),
                        thisMessage[2], thisMessage[3], false, db);
                db.getConversationById(convoId).addMessage(message);
            }
        } catch (UsernameAlreadyExistsException e) {
            e.printStackTrace();
        } catch (AccountNotExistException e) {
            e.printStackTrace();
        } catch (ConversationNotFoundException e) {
            e.printStackTrace();
        }

        // Start main window GUI
        mw = new MainWindow(this);
        mw.setChatList(db.getConversations());
    }

    public void receivedSuccessfulLogin(String details) {
        username = details;
        lw.dispose();

        System.out.println("CLIENT - Received successfulLogin for username [" + username + "]");
    }

    public void receivedAddConvo(String details) {
        String placeholder = details;

        int conversationID = Integer.parseInt(placeholder.substring(0, placeholder.indexOf(" ")));
        placeholder = placeholder.substring(placeholder.indexOf(" ") + 1);
        String participantsString = placeholder;

        ArrayList<Account> participants = new ArrayList<>();
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
            // mw.updateMsgEntry(message);

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