import java.io.*;
import java.time.LocalDateTime;
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

            for (String accountsDatum : accountsData) {
                String[] splitAccount = accountsDatum.split(",");

                try {
                    Account thisAccount = new Account(splitAccount[0], splitAccount[1], false);
                } catch (UsernameAlreadyExistsException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Database.createAccountFile();
        }

        int thisConversationId = 0;
        File conversationFile = new File(thisConversationId + ".txt");

        if (conversationFile.exists()) {
            do {
                ArrayList<String> conversationData = new ArrayList<>();

                try {
                    FileReader filer = new FileReader(conversationFile);
                    BufferedReader buffer = new BufferedReader(filer);

                    String fileLine = buffer.readLine();

                    while (fileLine != null) {
                        conversationData.add(fileLine);
                        fileLine = buffer.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ArrayList<Account> conversationParticipants = new ArrayList<>();
                String[] accountUsernames = conversationData.get(2).split(",");

                for (String accountUsername : accountUsernames) {
                    try {
                        conversationParticipants.add(Database.getAccountByUsername(accountUsername));
                    } catch (AccountNotExistException e) {
                        e.printStackTrace();
                    }
                }

                Conversation conversation = new Conversation(conversationData.get(1),
                        conversationParticipants, false);

                for (int i = 3; i < conversationData.size(); i++) {
                    String[] thisMessage = conversationData.get(i).split(",",4);

                    Message message = new Message(Integer.parseInt(thisMessage[0]),
                            LocalDateTime.parse(thisMessage[1]), thisMessage[2], thisMessage[3], false);
                    conversation.addMessage(message);
                }

                thisConversationId++;
                conversationFile = new File(thisConversationId + ".txt");
            } while (conversationFile.exists());
        }
    }

    public static void addToDatabase(Account account) {
        accounts.add(account);

        if (account.getAddToFile())
        Database.addAccountToFile(account);
    }

    public static void addToDatabase(Conversation conversation) {
        conversations.add(conversation);

        if (conversation.isAddToFile())
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

    public static void changeAccountDetailsInFile(String oldUsername, String oldPassword,
                                                  String newUsername, String newPassword) {

        if (newUsername == null) {
            newUsername = oldUsername;
        }

        if (newPassword == null) {
            newPassword = oldPassword;
        }

        ArrayList<String> accountsData = new ArrayList<>();

        try {
            FileReader filer = new FileReader("accounts.txt");
            BufferedReader buffer = new BufferedReader(filer);

            String fileLine = buffer.readLine();

            while (fileLine != null) {
                accountsData.add(fileLine);
                fileLine = buffer.readLine();
            }
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < accountsData.size(); i++) {
            if (accountsData.get(i).equals(oldUsername + "," + oldPassword)) {
                accountsData.set(i, newUsername + "," + newPassword);
            }
        }

        FileOutputStream accountOutputStream = null;
        try {
            accountOutputStream = new FileOutputStream("accounts.txt", false);
            PrintWriter accountsWriter = new PrintWriter(accountOutputStream);

            for (int i = 0; i < accountsData.size(); i++) {
                accountsWriter.println(accountsData.get(i));
            }
            accountsWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Account thisAccount = null;
        try {
            thisAccount = Database.getAccountByUsername(oldUsername);
        } catch (AccountNotExistException e) {
            e.printStackTrace();
        }

        ArrayList<Integer> conversationIds = thisAccount.getConversationIds();

        for (int i = 0; i < conversationIds.size(); i++) {

            ArrayList<String> conversationData = new ArrayList<>();

            try {
                FileReader filer = new FileReader(conversationIds.get(i) +".txt");
                BufferedReader buffer = new BufferedReader(filer);

                String fileLine = buffer.readLine();

                while (fileLine != null) {
                    conversationData.add(fileLine);
                    fileLine = buffer.readLine();
                }
                buffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String[] conversationParticipants = conversationData.get(2).split(",");
            for (int j = 0; j < conversationParticipants.length; j++) {
                if (conversationParticipants[j].equals(oldUsername)) {
                    conversationParticipants[j] = newUsername;
                }
            }

            String participantsToFile = "";

            for (int j = 0; j < conversationParticipants.length; j++) {
                participantsToFile = participantsToFile + conversationParticipants[j] + ",";
            }
            participantsToFile = participantsToFile.substring(0, participantsToFile.length() - 1);
            conversationData.set(2, participantsToFile);

            for (int j = 3; j < conversationData.size(); j++) {
                String[] messageSplit = conversationData.get(j).split(",", 4);
                if (messageSplit[2].equals(oldUsername)) {
                    messageSplit[2] = newUsername;
                }
                String newMessage = messageSplit[0] + "," + messageSplit[1] + ","
                        + messageSplit[2] + "," + messageSplit[3];
                conversationData.set(j, newMessage);
            }

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(conversationIds.get(i) + ".txt", false);
                PrintWriter conversationWriter = new PrintWriter(fileOutputStream);
                for (int j = 0; j < conversationData.size(); j++ ) {
                 conversationWriter.println(conversationData.get(j));
                }
                conversationWriter.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


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

            for (Account participant : participants) {
                participantsString.append(participant.getUsername()).append(",");
            }
            participantsString.replace(participantsString.length() - 1, participantsString.length(), "");
            conversationWriter.println(participantsString);

            conversationWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeMessageToConversationFile(Conversation conversation, Message message) throws FileNotFoundException {

        FileOutputStream fileOutputStream = new FileOutputStream(conversation.getConversationId() + ".txt", true);
        PrintWriter conversationWriter = new PrintWriter(fileOutputStream);

        conversationWriter.println(message.toString());
        conversationWriter.close();
    }

    public static ArrayList<Message> getMessages() {
        return messages;
    }

    public static void addParticipantToConversationFile(int conversationID, String username) {

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
        } catch (IOException e) {
            e.printStackTrace();
        }

        String toAdd = conversationFile.get(2);
        toAdd = toAdd + "," + username;

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream( conversationID + ".txt", false);
            PrintWriter conversationWriter = new PrintWriter(fileOutputStream);

            for (int i = 0; i < conversationFile.size(); i++) {
                if (i == 2) {
                    conversationWriter.println(toAdd);
                } else {
                    conversationWriter.println(conversationFile.get(i));
                }
            }
            conversationWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



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

        String toWrite = "";

        for (String s : participantsList) {
            if (!s.equals(username)) {
                toWrite = toWrite + s + ",";
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
