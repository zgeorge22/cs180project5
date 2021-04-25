import javax.swing.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;

public class Test {

    private static Random random = new Random();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Database db = new Database();

                Account z = new Account("Zach", "help");
                Account r = new Account("Rishi", "1234");
                Account j = new Account("Jack", "password");
                Account b = new Account("Ben", "qwerty");
                Account n = new Account("Natalie", "asdf");

                ArrayList<Account> accountsListSmall = new ArrayList<>();
                accountsListSmall.add(z);
                accountsListSmall.add(r);

                ArrayList<Account> accountsListMedium = new ArrayList<>();
                accountsListMedium.add(z);
                accountsListMedium.add(r);
                accountsListMedium.add(j);
                accountsListMedium.add(b);

                ArrayList<Account> accountsListLarge = new ArrayList<>();
                accountsListLarge.add(z);
                accountsListLarge.add(r);
                accountsListLarge.add(j);
                accountsListLarge.add(b);
                accountsListLarge.add(n);

                Conversation chatSmall = new Conversation(accountsListSmall);
                Conversation chatMedium = new Conversation(accountsListMedium);
                Conversation chatLarge = new Conversation(accountsListLarge);

                ArrayList<Conversation> conversationList = new ArrayList<Conversation>();
                conversationList.add(chatSmall);
                conversationList.add(chatLarge);

                // TEST CHAT LIST
                MainWindow mw = new MainWindow();

                // will be used once a conversations list is received from the server
                mw.setChatList(conversationList);

                // will be used upon new chat
                mw.addNewChat(chatMedium);

                // TEST CHAT WINDOW
                final int minMessages = 20;
                final int maxMessages = 50;

                ArrayList<Message> messagesListSmall = generateRandomMessages(accountsListSmall,
                        getRandomIntBetween(minMessages, maxMessages));
                ArrayList<Message> messagesListMedium = generateRandomMessages(accountsListMedium,
                        getRandomIntBetween(minMessages, maxMessages));
                ArrayList<Message> messagesListLarge = generateRandomMessages(accountsListLarge,
                        getRandomIntBetween(minMessages, maxMessages));

                chatSmall.setMessages(messagesListSmall);
                chatMedium.setMessages(messagesListMedium);
                chatLarge.setMessages(messagesListLarge);

                // will be used for participant changes (leaving chats)
                try {
                    b.removeConversation(chatMedium.getConversationId());
                } catch (ConversationNotFoundException e) {
                    System.out.println("Could not remove conversation!");
                }
                mw.updateChatEntry(chatMedium);
            }
        });
    }

    // ** TEST METHOD **
    private static ArrayList<Message> generateRandomMessages(ArrayList<Account> participants, int numMessages) {
        ArrayList<Message> messages = new ArrayList<Message>();

        Timestamp now = new Timestamp(System.currentTimeMillis());
        final int messageDelay = 60; // seconds

        final int minWords = 2;
        final int maxWords = 10;

        for (int i = 0; i < numMessages; i++) {
            Timestamp later = new Timestamp(now.getTime() + i * messageDelay * 1000);
            String sender = participants.get(getRandomIntBetween(0, participants.size() - 1)).getUsername();
            String content = generateRandomMessage(getRandomIntBetween(minWords, maxWords));
            Message newMessage = new Message(later, sender, content);
            messages.add(newMessage);
        }

        return messages;
    }

    // ** TEST METHOD **
    private static String generateRandomMessage(int numberOfWords) {
        final int minChars = 1;
        final int maxChars = 10;

        String randomMessage = "";

        for (int i = 0; i < numberOfWords; i++) {
            char[] word = new char[getRandomIntBetween(minChars, maxChars)];
            for (int j = 0; j < word.length; j++) {
                word[j] = (char) ('a' + getRandomIntBetween(0, 25));
            }
            randomMessage += new String(word) + " ";
        }
        return randomMessage.substring(0, randomMessage.length() - 1);
    }

    private static int getRandomIntBetween(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }
}
