import javax.xml.crypto.Data;
import java.util.ArrayList;

public class Account {

    private String username;
    private String password;
    private ArrayList<Conversation> conversations;
    private Database database;
    private boolean addToFile;

    // When you create a new Account, make sure addToFile is true, or else it will not add it to the file.
    // Accounts retrieved from an existing file will have addToFile = false, ensuring that they will not get
    // re-added when the database initialises it into the accounts.

    public Account(String username, String password, Database database, boolean addToFile) throws UsernameAlreadyExistsException {

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

    public boolean isAddToFile() {
        return addToFile;
    }

    public String getUsername() {
        return username;
    }

    public void changeUsername(String newUsername) throws UsernameAlreadyExistsException {
        try {
            this.database.getAccountByUsername(newUsername);
        } catch (AccountNotExistException e) {
            String oldUsername = this.getUsername();
            String oldPassword = this.getPassword();
            this.database.changeAccountDetailsInFile(oldUsername, oldPassword, newUsername, null);
            this.username = newUsername;
        }

        if (this.getUsername() != newUsername) {
            throw new UsernameAlreadyExistsException();
        }
    }

    public String getPassword() {
        return password;
    }

    public void changePassword(String newPassword) {
        this.database.changeAccountDetailsInFile(this.getUsername(), this.getPassword(), null, newPassword);
        this.password = newPassword;
    }

    public ArrayList<Conversation> getConversations() {
        return conversations;
    }

//    public void setConversations(ArrayList<Conversation> conversations) {
//        this.conversations = conversations;
//    }

    public void addToConversation(Conversation conversation) {
        conversations.add(conversation);
    }

    public void removeConversation(Conversation conversation) {
        conversations.remove(conversation);
    }

    // Use these id based adding or removing conversations in order to sync.
    public void addToConversation(int id) throws ConversationNotFoundException {
        Conversation conversation = this.database.getConversationById(id);
        conversations.add(conversation);
        conversation.addParticipant(this);
        this.database.addParticipantToConversationFile(id, this.getUsername());
    }

    public void removeConversation(int id) throws ConversationNotFoundException {
        Conversation conversation = this.database.getConversationById(id);
        conversations.remove(conversation);
        conversation.removeParticipant(this);
        this.database.removeParticipantFromConversationFile(id, this.getUsername());
    }

    public ArrayList<Integer> getConversationIds() {

        ArrayList<Integer> conversationIds = new ArrayList<>();
        for (int i = 0; i < this.getConversations().size(); i++) {
            conversationIds.add(this.getConversations().get(i).getConversationId());
        }

        return conversationIds;
    }

    public String toString() {
        return this.getUsername() + "," + this.getPassword();
    }

}

