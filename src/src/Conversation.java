package src;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;

public class Conversation implements Serializable {

    private static int nextConversationId;
    private final int conversationId;
    private ArrayList<Account> participants;
    private ArrayList<Message> messages;
    private String conversationName;
    boolean addToFile;
    Database database;

    // When you create a new Conversation, make sure addToFile is true, or else it
    // will not add it to the file.
    // Accounts retrieved from an existing file will have addToFile = false,
    // ensuring that they will not get
    // re-added when the database initialises it into the accounts.

    // Use this constructor in the server to create new messages.
    public Conversation(String conversationName, ArrayList<Account> participants, boolean addToFile,
            Database database) {
        this.conversationId = getNextConversationId();
        this.participants = participants;
        this.conversationName = conversationName;
        this.messages = new ArrayList<>();
        this.addToFile = addToFile;
        this.database = database;

        for (Account account : participants) {
            try {
                this.database.getAccountByUsername(account.getUsername()).addToConversation(this);
            } catch (AccountNotExistException e) {
                e.printStackTrace();
            }
        }
        setNextConversationId(++nextConversationId);

        this.database.addToDatabase(this);
    }

    // Do not call this constructor for creating new messages in the server.
    public Conversation(int id, String conversationName, ArrayList<Account> participants, Database database) {
        this.conversationId = id;
        this.conversationName = conversationName;
        this.participants = participants;
        this.messages = new ArrayList<>();
        this.addToFile = false;
        this.database = database;

        for (Account account : participants) {
            try {
                this.database.getAccountByUsername(account.getUsername()).addToConversation(this);
            } catch (AccountNotExistException e) {
                e.printStackTrace();
            }
        }

        this.database.addToDatabase(this);
    }

    public static int getNextConversationId() {
        return nextConversationId;
    }

    public static void setNextConversationId(int nextConversationId) {
        Conversation.nextConversationId = nextConversationId;
    }

    public String getConversationName() {
        return conversationName;
    }

    // public void setConversationName(String conversationName) {
    // this.conversationName = conversationName;
    // }

    public int getConversationId() {
        return conversationId;
    }

    public ArrayList<Account> getParticipants() {
        return participants;
    }

    public String getParticipantsString() {
        String s = "";

        for (Account participant : participants) {
            s += participant.getUsername() + ", ";
        }

        return s.substring(0, s.length() - 2);
    }

    public void setParticipants(ArrayList<Account> participants) {
        this.participants = participants;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public boolean isAddToFile() {
        return addToFile;
    }

    public void addParticipant(Account account) {
        participants.add(account);
    }

    public void removeParticipant(Account account) {
        participants.remove(account);
    }

    // Use these the username based add/remove accounts in order to sync.
    public void addParticipant(String username) throws AccountNotExistException {
        Account account = this.database.getAccountByUsername(username);
        participants.add(account);
        account.addToConversation(this);
        if (this.database.isServer()) {
            this.database.addParticipantToConversationFile(this.getConversationId(), username);
        }
    }

    public void removeParticipant(String username) throws AccountNotExistException {
        Account account = this.database.getAccountByUsername(username);
        participants.remove(account);
        account.removeConversation(this);
        if (this.database.isServer()) {
            this.database.removeParticipantFromConversationFile(this.getConversationId(), username);
        }
    }

    public void addMessage(Message message) {

        messages.add(message);

        try {
            if (message.isAddToFile()) {
                this.database.writeMessageToConversationFile(this, message);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void exportToCSV() {
        this.database.createCSV(this.getConversationId());
    }


}