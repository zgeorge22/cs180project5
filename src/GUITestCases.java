import javax.swing.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

public class GUITestCases {

    private static Random random = new Random();
    private static Database db;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    Client client = new Client();
                    MainWindow mw = client.getMainWindow();
                    db = client.getDatabase();

                    Account z = new Account("Zach", "help", db, false);
                    Account r = new Account("Rishi", "1234", db, false);
                    Account j = new Account("Jack", "password", db, false);
                    Account b = new Account("Ben", "qwerty", db, false);
                    Account n = new Account("Natalie", "asdf", db, false);

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

                    Conversation chatSmall = new Conversation("small", accountsListSmall, false, db);
                    Conversation chatMedium = new Conversation("medium", accountsListMedium, false, db);
                    Conversation chatLarge = new Conversation("large", accountsListLarge, false, db);

                    ArrayList<Conversation> conversationList = new ArrayList<Conversation>();
                    conversationList.add(chatSmall);
                    conversationList.add(chatLarge);

                    // TEST INITIAL DATA DUMP TO CLIENT MAIN WINDOW

                    // TEST CHAT LIST
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

                    // TEST NEW MESSAGE TO CLIENT
                    Message.setNextMessageId(Message.getNextMessageId() + 1);
                    client.receivedAddMsg(chatLarge.getConversationId() + " " + Message.getNextMessageId() + " "
                            + "Jack" + " " + LocalDateTime.now() + " " + "Hello World!");

                    // will be used for participant changes (leaving chats)
                    try {
                        b.removeConversation(chatMedium.getConversationId());
                    } catch (ConversationNotFoundException e) {
                        System.out.println("Could not remove conversation!");
                    }
                    mw.updateChatEntry(chatMedium);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ** TEST METHOD **
    private static ArrayList<Message> generateRandomMessages(ArrayList<Account> participants, int numMessages) {
        ArrayList<Message> messages = new ArrayList<Message>();

        LocalDateTime now = LocalDateTime.now();
        final int messageDelay = 20; // min

        final int minWords = 2;
        final int maxWords = 10;

        for (int i = numMessages; i > 0; i--) {
            LocalDateTime later = now.plusMinutes(-i * messageDelay);
            String sender = participants.get(getRandomIntBetween(0, participants.size() - 1)).getUsername();
            String content = generateRandomMessage(getRandomIntBetween(minWords, maxWords));
            Message newMessage = new Message(Message.getNextMessageId(), later, sender, content, false, db);
            Message.setNextMessageId(Message.getNextMessageId() + 1);
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
