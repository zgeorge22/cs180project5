import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * This is the conversation class which creates conversation objects which allow
 * users to interact with each other.
 *
 * <p>
 * Purdue University -- CS18000 -- Spring 2021 -- Project 5
 * </p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack
 *         Dorkin
 * @version May 3rd, 2021
 */

public class Conversation {

    private static int nextConversationId; // Static integer that the conversation uses to generate conversationIDs
    private final int conversationId; // The conversation ID of a particular conversation
    private ArrayList<Account> participants; // The list of participants in the conversation.
    private ArrayList<Message> messages; // The messages which are part of this conversation.
    private String conversationName; // The unused conversationName field.
    boolean addToFile; // The boolean which represents if it should be added to file or not.
    Database database; // The database that the conversation is stored in.

    // When Conversations are created, addToFile is set to true, in order to add it
    // to the text file.
    // Conversations retrieved from an existing file will have addToFile = false,
    // ensuring they are not duplicated in
    // the text files.
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

    // This constructor generates conversation objects given the parameters. This is
    // only used when reading in
    // conversations from text files.
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

    // Getter for the next conversation ID, used to store the ID of the next
    // conversation
    public static int getNextConversationId() {
        return nextConversationId;
    }

    // Setter for the conversation ID incremented whenever a conversation is
    // created.
    public static void setNextConversationId(int nextConversationId) {
        Conversation.nextConversationId = nextConversationId;
    }

    // Setter for the conversation name (which is mostly unused).
    public String getConversationName() {
        return conversationName;
    }

    // Getter for the conversation ID of a conversation object.
    public int getConversationId() {
        return conversationId;
    }

    // Getter for the ArrayList of participants in a conversation.
    public ArrayList<Account> getParticipants() {
        return participants;
    }

    // Getter for a String containing the list of all the participants in the
    // conversation.
    public String getParticipantsString() {
        String s = "";

        if (participants.size() > 0) {
            for (Account participant : participants) {
                s += participant.getUsername() + ", ";
            }
            s = s.substring(0, s.length() - 2);
        }

        return s;
    }

    // Getter for an ArrayList containing all of the messages in a conversation.
    public ArrayList<Message> getMessages() {
        return messages;
    }

    // Sets the messages contained within the conversation.
    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    // Getter for the AddToFile boolean
    public boolean isAddToFile() {
        return addToFile;
    }

    // This is a method that simply adds an account into a conversation only in the
    // database.
    public void addParticipant(Account account) {
        participants.add(account);
    }

    // This is a method that simply removes an account from a conversation only in
    // the database.
    public void removeParticipant(Account account) {
        participants.remove(account);
    }

    // The username based addParticipant method adds participants and syncs the
    // database and the text file.
    public void addParticipant(String username) throws AccountNotExistException {
        Account account = this.database.getAccountByUsername(username);
        participants.add(account);
        account.addToConversation(this);
        if (this.database.isServer()) {
            this.database.addParticipantToConversationFile(this.getConversationId(), username);
        }
    }

    // The username based removeParticipant method removes participants and syncs
    // the database and the text file.
    public void removeParticipant(String username) throws AccountNotExistException {
        Account account = this.database.getAccountByUsername(username);
        participants.remove(account);
        account.removeConversation(this);
        if (this.database.isServer()) {
            this.database.removeParticipantFromConversationFile(this.getConversationId(), username);
        }
    }

    // This method adds a message to the conversation, when the message is sent to
    // the conversation.
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

    // This method is called in order to export the details of a particular
    // conversation to CSV
    public void exportToCSV() {
        this.database.createCSV(this.getConversationId());
    }

}