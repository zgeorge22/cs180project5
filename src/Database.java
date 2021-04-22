import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class Database {

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

        Database.createAccountFile();
    }

    public static void addToDatabase(Account account) {
        accounts.add(account);
        Database.addAccountToFile(account);
    }

    public static void addToDatabase(Conversation conversation) {
        conversations.add(conversation);
        Database.createConversationFile(conversation);
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

    public static void createAccountFile() {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("accounts.txt", false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void addAccountToFile(Account account) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("accounts.txt", true);
            PrintWriter accountWriter = new PrintWriter(fileOutputStream);

            accountWriter.println(account.toString());
            accountWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void createConversationFile(Conversation conversation) {

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(conversation.getConversationId() + ".txt", false);
            PrintWriter conversationWriter = new PrintWriter(fileOutputStream);

            conversationWriter.println("ConversationID: " + conversation.getConversationId());
            conversationWriter.println("ConversationName: " + conversation.getConversationName());
            ArrayList<Account> participants = conversation.getParticipants();
            StringBuilder participantsString = new StringBuilder();
            participantsString.append("Participants: ");

            for (int i = 0; i < participants.size(); i++) {
                participantsString.append(participants.get(i).getUsername()).append(",");
            }
            participantsString.replace(participantsString.length() - 1, participantsString.length(), "");
            conversationWriter.println(participantsString);

            conversationWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeMessageToConversationTxt(Conversation conversation, Message message) throws FileNotFoundException {

        FileOutputStream fileOutputStream = new FileOutputStream(conversation.getConversationId() + ".txt", true);
        PrintWriter conversationWriter = new PrintWriter(fileOutputStream);

        conversationWriter.println(message.toString());
        conversationWriter.close();
    }
}
