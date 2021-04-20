
import java.util.ArrayList;

public class Conversation {

    private static int nextConversationId;
    private final int conversationId;
    private ArrayList<Account> participants;
    private ArrayList<Message> messages;

    public Conversation(ArrayList<Account> usersInConversation) {
        this.conversationId = getNextConversationId();
        this.participants = usersInConversation;

        setNextConversationId(++nextConversationId);
    }

    public static int getNextConversationId() {
        return nextConversationId;
    }

    public static void setNextConversationId(int nextConversationId) {
        Conversation.nextConversationId = nextConversationId;
    }

    public int getConversationId() {
        return conversationId;
    }

    public ArrayList<Account> getParticipants() {
        return participants;
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

    public void removeParticipant(String username) {

        ArrayList<Account> conversationMembers = this.getParticipants();

        for (int i = 0; i < this.getParticipants().size(); i++) {
            if (conversationMembers.get(i).getUsername().equals(username)) {
                this.getParticipants().get(i).removeConversation(this.getConversationId());
                conversationMembers.remove(i);
            }
        }

        this.setParticipants(conversationMembers);
    }

    public void addMessage(Message message) {

        ArrayList<Message> conversationMessages = this.getMessages();
        conversationMessages.add(message);
        this.setMessages(conversationMessages);


    }

    public void deleteMessage(int messageId) {

        ArrayList<Message> conversationMessages = this.getMessages();

        for (int i = 0; i < this.getMessages().size(); i++) {
            if (messageId == this.getMessages().get(i).getId()) {
                conversationMessages.remove(i);
            }
        }

        this.setMessages(conversationMessages);
    }
}
