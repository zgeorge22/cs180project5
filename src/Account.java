import java.util.ArrayList;

public class Account {

    private String username;
    private String password;
    private ArrayList<Conversation> conversations;

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
        this.conversations = new ArrayList<>();

        Database.addToDatabase(this);
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

    // MAIN METHOD FOR TESTING ONLY
    //TODO Test that the new adding/removing methods work
    public static void main(String[] args) {

        Database database;
        database = new Database();

        Account a = new Account("guest", "guest");
        Account b = new Account("jim", "jim");
        Account c = new Account("bob", "bob");

        ArrayList<Account> accounts = new ArrayList<>();
        accounts.add(a);
        accounts.add(b);
        accounts.add(c);

        ArrayList<Account> otherAccounts = new ArrayList<>();
        otherAccounts.add(a);
        otherAccounts.add(b);
        otherAccounts.add(c);

        Conversation conversation = new Conversation(accounts);

        Conversation otherConversation = new Conversation(otherAccounts);

        System.out.println("Conversation ID: " + conversation.getConversationId());
        System.out.println("OtherConversation ID: " + otherConversation.getConversationId());

        System.out.println("People in convo 1 ------------------------");
        for (int i = 0; i < conversation.getParticipants().size(); i++) {
            System.out.println(conversation.getParticipants().get(i));
        }

        System.out.println("People in convo 2 -----------------------");
        for (int i = 0; i < otherConversation.getParticipants().size(); i++) {
            System.out.println(otherConversation.getParticipants().get(i));
        }

        System.out.println("guest's convos ------------");
        for (int i = 0; i < a.getConversationIds().size(); i++) {
            System.out.println(a.getConversationIds().get(i).toString());
        }

        System.out.println("jim's convos ------------");
        for (int i = 0; i < b.getConversationIds().size(); i++) {
            System.out.println(b.getConversationIds().get(i).toString());
        }

        try {
            System.out.println("Removing from convos --------------------");
            a.removeConversation(0);
            b.removeConversation(1);
        } catch (ConversationNotFoundException e) {
            e.printStackTrace();
        }


        System.out.println("People in convo 1 ------------------------");
        for (int i = 0; i < conversation.getParticipants().size(); i++) {
            System.out.println(conversation.getParticipants().get(i));
        }

        System.out.println("People in convo 2 -----------------------");
        for (int i = 0; i < otherConversation.getParticipants().size(); i++) {
            System.out.println(otherConversation.getParticipants().get(i));
        }

        System.out.println("guest's convos ------------");
        for (int i = 0; i < a.getConversationIds().size(); i++) {
            System.out.println(a.getConversationIds().get(i).toString());
        }

        System.out.println("jim's convos ------------");
        for (int i = 0; i < b.getConversationIds().size(); i++) {
            System.out.println(b.getConversationIds().get(i).toString());
        }



    }
}