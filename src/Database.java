import java.io.*;
import java.lang.reflect.Array;
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

        File accountsFile;

        accountsFile = new File("accounts.txt");
        if (accountsFile.exists()) {

            ArrayList<String> accountsData = new ArrayList<>();

            try {
                FileReader filer = new FileReader(accountsFile);
                BufferedReader buffer = new BufferedReader(filer);

                String fileLine = buffer.readLine();

                while (fileLine != null) {
                    accountsData.add(fileLine);
                    fileLine = buffer.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < accountsData.size(); i++) {
                String[] splitAccount = accountsData.get(i).split(",");

                try {
                    Account thisAccount = new Account(splitAccount[0], splitAccount[1], false);
                } catch (UsernameAlreadyExistsException e) {
                    e.printStackTrace();
                }
            }




        } else {
            Database.createAccountFile();
        }
    }

    public static void addToDatabase(Account account) {
        accounts.add(account);

        if (account.getAddToFile())
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

            conversationWriter.println(conversation.getConversationId());
            conversationWriter.println(conversation.getConversationName());
            ArrayList<Account> participants = conversation.getParticipants();
            StringBuilder participantsString = new StringBuilder();

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

    public static void removeParticipantFromConversationFile(int conversationID, String username) {

        String[] participantsList = null;
        ArrayList<String> conversationFile = new ArrayList<>();

        try {
            FileReader filer = new FileReader(conversationID + ".txt");
            BufferedReader buffer = new BufferedReader(filer);

            String fileLine = buffer.readLine();

            while (fileLine != null) {
                conversationFile.add(fileLine);
                fileLine = buffer.readLine();
            }

            buffer.close();
            filer.close();

            participantsList = conversationFile.get(2).split(",");

        } catch (IOException e) {
            e.printStackTrace();
        }

        int positionInList = -1;

        String toWrite = "";

        for (int i = 0; i < participantsList.length; i++) {
            if (!participantsList[i].equals(username)) {
                toWrite = toWrite + participantsList[i] + ",";
            }
        }

        toWrite = toWrite.substring(0, toWrite.length() - 1);

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream( conversationID + ".txt", false);
            PrintWriter conversationWriter = new PrintWriter(fileOutputStream);

            for (int i = 0; i < conversationFile.size(); i++) {
                if (i == 2) {
                    conversationWriter.println(toWrite);
                } else {
                    conversationWriter.println(conversationFile.get(i));
                }
            }
            conversationWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }










    }
}
