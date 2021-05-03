import java.util.ArrayList;

/**
 * This class allows for the creation of Account objects.
 *
 * <p>Purdue University -- CS18000 -- Spring 2021 -- Project 5</p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack Dorkin
 * @version May 3rd, 2021
 */

public class Account {

    private String username;
    private String password;
    private ArrayList<Conversation> conversations;  // The conversations that the user is part of.
    private Database database;  // The database that the account should be added to.
    private boolean addToFile;
    // Fields required for the Account.


    // This constructor is called when creating new Account objects. It gives them a username, password and the
    // database that they should be added to.
    // If a new Account is created,  addToFile is true, ensuring that it is added to the text file.
    // Accounts retrieved from an existing file will have addToFile = false, ensuring that they will not get
    // re-added when the database initialises it into the accounts.
    public Account(String username, String password, Database database, boolean addToFile)
            throws UsernameAlreadyExistsException {
        try {
            database.getAccountByUsername(username).getUsername();
        } catch (AccountNotExistException a) {
            this.username = username;
            this.password = password;
            this.conversations = new ArrayList<>();
            this.database = database;
            this.addToFile = addToFile;
            this.database.addToDatabase(this);
        }

        try {
            this.username.equals(username);
        } catch (NullPointerException nullPointerException) {
            throw new UsernameAlreadyExistsException();
        }
    }

    //Getter for whether the account needed to be added to the text file or now.
    public boolean isAddToFile() {
        return addToFile;
    }

    //Getter for the username for the account.
    public String getUsername() {
        return username;
    }

    // This method allows the username of an account to be changed, changing it in the database and in the text file.
    public void changeUsername(String newUsername) throws UsernameAlreadyExistsException {
        try {
            this.database.getAccountByUsername(newUsername);
        } catch (AccountNotExistException e) {
            String oldUsername = this.getUsername();
            String oldPassword = this.getPassword();
            this.database.changeAccountDetailsInFile(oldUsername, oldPassword, newUsername, null);
            this.username = newUsername;
        }

        if (!this.getUsername().equals(newUsername)) {
            throw new UsernameAlreadyExistsException();
        }
    }

    // Getter for the password of the account
    public String getPassword() {
        return password;
    }

    // This method allows the password of the account to be changed, reflecting in the database and the text file.
    public void changePassword(String newPassword) {
        this.database.changeAccountDetailsInFile(this.getUsername(), this.getPassword(), null, newPassword);
        this.password = newPassword;
    }

    // This is the getter for the conversations ArrayList that any account is part of.
    public ArrayList<Conversation> getConversations() {
        return conversations;
    }

    // This is a method that simply adds an account into a conversation only in the database.
    public void addToConversation(Conversation conversation) {
        conversations.add(conversation);
    }

    // This is a method that simple removes an account from a conversation only in the database.
    public void removeConversation(Conversation conversation) {
        conversations.remove(conversation);
    }

    // This method adds an account into a conversation and syncs the conversation, account and database objects
    // while also writing the changes into the file.
    public void addToConversation(int id) throws ConversationNotFoundException {
        Conversation conversation = this.database.getConversationById(id);
        conversations.add(conversation);
        conversation.addParticipant(this);
        this.database.addParticipantToConversationFile(id, this.getUsername());
    }

    // This method removes an account into a conversation and syncs the conversation, account and database objects
    // while also writing the changes into the file.
    public void removeConversation(int id) throws ConversationNotFoundException {
        Conversation conversation = this.database.getConversationById(id);
        conversations.remove(conversation);
        conversation.removeParticipant(this);
        this.database.removeParticipantFromConversationFile(id, this.getUsername());
    }

    // This method gets an ArrayList containing the conversationIDs of all conversations that the user is a part of.
    public ArrayList<Integer> getConversationIds() {

        ArrayList<Integer> conversationIds = new ArrayList<>();
        for (int i = 0; i < this.getConversations().size(); i++) {
            conversationIds.add(this.getConversations().get(i).getConversationId());
        }

        return conversationIds;
    }

    // This is the toString method for Account objects, when they need to be converted to Strings.
    public String toString() {
        return this.getUsername() + "," + this.getPassword();
    }

}