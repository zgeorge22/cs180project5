import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Conversation {

    private static int nextConversationId;
    private final int conversationId;
    private ArrayList<Account> participants;
    private ArrayList<Message> messages;
    private String conversationName;
    boolean addToFile;

    // When you create a new Conversation, make sure addToFile is true, or else it will not add it to the file.
    // Accounts retrieved from an existing file will have addToFile = false, ensuring that they will not get
    // re-added when the database initialises it into the accounts.
    public Conversation(String conversationName, ArrayList<Account> usersInConversation, boolean addToFile) {
        this.conversationId = getNextConversationId();
        this.participants = usersInConversation;
        this.conversationName = conversationName;
        this.messages = new ArrayList<Message>();
        this.addToFile = addToFile;

        for (Account account : usersInConversation) {

            try {
                Database.getAccountByUsername(account.getUsername()).addToConversation(this);
            } catch (AccountNotExistException e) {
                e.printStackTrace();
            }
        }
        setNextConversationId(++nextConversationId);

        Database.addToDatabase(this);
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

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    public int getConversationId() {
        return conversationId;
    }

    public ArrayList<Account> getParticipants() {
        return participants;
    }

    public ArrayList<Message> getMessages() {
        return messages;
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
        Account account = Database.getAccountByUsername(username);
        participants.add(account);
        account.addToConversation(this);
        Database.addParticipantToConversationFile(this.getConversationId(), username);
    }

    public void removeParticipant(String username) throws AccountNotExistException {
        Account account = Database.getAccountByUsername(username);
        participants.remove(account);
        account.removeConversation(this);
        Database.removeParticipantFromConversationFile(this.getConversationId(), username);
    }

    public void addMessage(Message message) {

        messages.add(message);

        try {
            if (message.isAddToFile()) {
                Database.writeMessageToConversationFile(this, message);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
// TODO is this method needed?

//    public void deleteMessage(int messageId) {
//
//        for (int i = 0; i < this.getMessages().size(); i++) {
//            if (messageId == this.getMessages().get(i).getId()) {
//                messages.remove(messages.get(i));
//            }
//        }
//    }
}
