
import javax.xml.crypto.Data;
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

        Database.addToDatabase(this);
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

    public void addParticipant(Account account) {
        participants.add(account);
    }
    public void removeParticipant(Account account) {
        participants.remove(account);
    }

    // Use these the username based add/remove accounts in order to sync.
    public void addParticipant(String username) throws AccountNotExistException {
        Account account = Database.getAccountByUsername(username);
        participants.add(account);
        account.addToConversation(this);
    }

    public void removeParticipant(String username) throws AccountNotExistException {
        Account account = Database.getAccountByUsername(username);
        participants.remove(account);
        account.removeConversation(this);
    }



    public void addMessage(Message message) {
        messages.add(message);
    }

    public void deleteMessage(int messageId) {

        for (int i = 0; i < this.getMessages().size(); i++) {
            if (messageId == this.getMessages().get(i).getId()) {
                messages.remove(messages.get(i));
            }
        }
    }
}
