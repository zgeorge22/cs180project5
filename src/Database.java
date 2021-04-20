import javax.security.auth.login.AccountNotFoundException;
import javax.xml.crypto.Data;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Database {

    private static ArrayList<Account> accounts;
    private static ArrayList<Conversation> conversations;
    private static ArrayList<Message> messages;

    public Database() {
        ArrayList<Account> accountList = new ArrayList<>();
        ArrayList<Conversation> conversationList = new ArrayList<>();
        ArrayList<Message> messageList = new ArrayList<>();

        Database.accounts = accountList;
        Database.conversations = conversationList;
        Database.messages = messageList;
    }

    public Database(ArrayList<Account> accounts, ArrayList<Conversation> conversations, ArrayList<Message> messages) {
        Database.accounts = accounts;
        Database.conversations = conversations;
        Database.messages = messages;
    }

    public static void addToDatabase(Account account) {
        accounts.add(account);
    }

    public static void addToDatabase(Conversation conversation) {
        conversations.add(conversation);
    }

    public static void addToDatabase(Message message) {
        messages.add(message);
    }

    public static Account getAccountByUsername(String username) throws AccountNotExistException {
        for (Account account : accounts) {
            if (username.equals(account.getUsername())) {
                return account;
            }
        }
        throw new AccountNotExistException();
    }

    public static Conversation getConversationById(int id) throws ConversationNotFoundException {
        for (Conversation conversation : conversations) {
            if (id == conversation.getConversationId()) {
                return conversation;
            }
        }
        throw new ConversationNotFoundException();
    }
}
