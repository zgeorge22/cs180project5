import java.util.ArrayList;

public class Account {

    private String username;
    private String password;
    private ArrayList<Conversation> conversations;

    public Account(String username, String password) throws UsernameAlreadyExistsException {

        try {
            String user = Database.getAccountByUsername(username).getUsername();
        } catch (AccountNotExistException a) {
            this.username = username;
            this.password = password;
            this.conversations = new ArrayList<>();
            Database.addToDatabase(this);
        }

        try {
            this.username.equals(username);
        } catch (NullPointerException nullPointerException) {
            throw new UsernameAlreadyExistsException();
        }
    }

    public String toString() {
        return this.getUsername() + "," + this.getPassword();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(ArrayList<Conversation> conversations) {
        this.conversations = conversations;
    }

    public void addToConversation(Conversation conversation) {
        conversations.add(conversation);
    }

    public void removeConversation(Conversation conversation) {
        conversations.remove(conversation);
    }

    // Use these id based adding or removing conversations in order to sync.
    public void addToConversation(int id) throws ConversationNotFoundException {
        Conversation conversation = Database.getConversationById(id);
        conversations.add(conversation);
        conversation.addParticipant(this);
    }

    public void removeConversation(int id) throws ConversationNotFoundException {
        Conversation conversation = Database.getConversationById(id);
        conversations.remove(conversation);
        conversation.removeParticipant(this);
    }

    public ArrayList<Integer> getConversationIds() {

        ArrayList<Integer> conversationIds = new ArrayList<>();
        for (int i = 0; i < this.getConversations().size(); i++) {
            conversationIds.add(this.getConversations().get(i).getConversationId());
        }

        return conversationIds;
    }
}