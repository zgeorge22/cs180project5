

import java.util.ArrayList;

public class Account {

    private String username;
    private String password;
    private ArrayList<Conversation> conversations;

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
        this.conversations = new ArrayList<>();
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
        ArrayList<Conversation> currentConversations = this.getConversations();
        currentConversations.add(conversation);
        this.setConversations(currentConversations);

    }

    // DO NOT USE THIS TO REMOVE CONVERSATIONS
    // USE CONVERSATION.REMOVEPARTICPANT WHICH CALLS THIS METHOD
    public void removeConversation(int id) {
        ArrayList<Conversation> currentConversations = this.getConversations();

        for (int i = 0; i < this.getConversations().size(); i++) {
            if (currentConversations.get(i).getConversationId() == id) {

                ArrayList<Account> participants = this.getConversations().get(i).getParticipants();
                this.getConversations().get(i).setParticipants(participants);
                currentConversations.remove(i);
            }
        }

        this.setConversations(currentConversations);
    }

    public ArrayList<Integer> getConversationIds() {

        ArrayList<Integer> conversationIds=  new ArrayList<>();
        for (int i = 0; i < this.getConversations().size(); i++) {
            conversationIds.add(this.getConversations().get(i).getConversationId());
        }

        return conversationIds;
    }

    public static void main(String[] args) {

        Account a = new Account("guest", "guest");
        Account b = new Account("jim", "jim");
        Account c = new Account("Keval", "Keval");

        ArrayList<Account> accounts = new ArrayList<>();
        accounts.add(a);
        accounts.add(b);
        accounts.add(c);

        ArrayList<Account> otherAccounts = new ArrayList<>();
        otherAccounts.add(b);
        otherAccounts.add(c);

        Conversation conversation = new Conversation(accounts);
        a.addToConversation(conversation);
        b.addToConversation(conversation);
        c.addToConversation(conversation);

        Conversation otherConversation = new Conversation(otherAccounts);
        a.addToConversation(otherConversation);
        b.addToConversation(otherConversation);
        c.addToConversation(otherConversation);

        System.out.println("Conversation ID: " + conversation.getConversationId());
        System.out.println("OtherConversation ID: " + otherConversation.getConversationId());

        otherConversation.removeParticipant(a.getUsername());

        System.out.println(otherConversation.getParticipants().get(0));
        System.out.println(otherConversation.getParticipants().get(1));
        System.out.println(a.getConversationIds().get(1));

    }
}