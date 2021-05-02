package src;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Database implements Serializable {

    private ArrayList<Account> accounts;  // ArrayList which stores Accounts
    private ArrayList<Conversation> conversations; //ArrayList which stores Conversations
    private ArrayList<Message> messages; //ArrayList which stores Messages
    private final boolean isServer;  // Boolean which reflects whether the database is the server or not.


    //Constructor for the database - takes in 1 parameter. Creates a database depending on whether the database is the
    //server database or the client database.
    public Database(boolean isServer) {
        ArrayList<Account> accountList = new ArrayList<>();
        ArrayList<Conversation> conversationList = new ArrayList<>();
        ArrayList<Message> messageList = new ArrayList<>();


        this.accounts = accountList;
        this.conversations = conversationList;
        this.messages = messageList;
        this.isServer = isServer;
        // Instantiates the arraylists which store all of the data in the database.

        // Starts up the database by loading in accounts/conversations/messages from their respective .txt files.
        if (isServer) {
            this.startup();
        }

    }

    // This is the getter for the isServer parameter of the database.
    public boolean isServer() {
        return isServer;
    }

    // This method occurs on the startup of a database if it is the server database.
    // This method reads in all of the data from the accounts file and any and all conversation files.
    // The appropriate accounts, conversations and messages are loaded into the database.
    public void startup() {

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
                    new Account(splitAccount[0], splitAccount[1], this, false);
                } catch (UsernameAlreadyExistsException e) {
                    e.printStackTrace();
                }
            }
        } else {
            this.createAccountFile();
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
                        conversationParticipants.add(this.getAccountByUsername(accountUsername));
                    } catch (AccountNotExistException e) {
                        e.printStackTrace();
                    }
                }

                Conversation conversation = new Conversation(conversationData.get(1), conversationParticipants, false,
                        this);

                for (int i = 3; i < conversationData.size(); i++) {
                    String[] thisMessage = conversationData.get(i).split(",", 4);

                    Message message = new Message(Integer.parseInt(thisMessage[0]), LocalDateTime.parse(thisMessage[1]),
                            thisMessage[2], thisMessage[3], false, this);

                    conversation.addMessage(message);
                }

                thisConversationId++;
                conversationFile = new File(thisConversationId + ".txt");
            } while (conversationFile.exists());
        }

        int messagesCreated = this.messages.size();
        Message.setNextMessageId(messagesCreated);
    }

    // Getter for getting Account objects by inputting the username. Throws the AccountNotExistException if the
    // account being sought does not exist.
    public Account getAccountByUsername(String username) throws AccountNotExistException {
        for (Account account : accounts) {
            if (username.equals(account.getUsername())) {
                return account;
            }
        }
        throw new AccountNotExistException();
    }

    // Getter for getting Account objects by inputting the Conversation ID. Throws the ConversationNotFoundException
    // if the conversation being sought does not exist.
    public Conversation getConversationById(int id) throws ConversationNotFoundException {
        for (Conversation conversation : conversations) {
            if (id == conversation.getConversationId()) {
                return conversation;
            }
        }
        throw new ConversationNotFoundException();
    }

    // Getter for getting Message objects by inputting the Message ID. Throws the MessageNotFoundException if the
    // message being sought does not exist.
    public Message getMessageById(int id) throws MessageNotFoundException {
        for (Message message : messages) {
            if (id == message.getId()) {
                return message;
            }
        }
        throw new MessageNotFoundException();
    }

    // This method creates an account file, if one does not already exist.
    public void createAccountFile() {

        try {
            new FileOutputStream("accounts.txt", false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // This method adds an account to the database.
    public void addToDatabase(Account account) {
        accounts.add(account);
        if (this.isServer && account.isAddToFile()) {
            this.addAccountToFile(account);
        }
    }

    // This method adds a conversation to the database.
    public void addToDatabase(Conversation conversation) {
        conversations.add(conversation);

        if (this.isServer && conversation.isAddToFile()) {
            this.createConversationFile(conversation);
        }
    }

    // This method adds a message to the database.
    public void addToDatabase(Message message) {
        messages.add(message);
    }

    // This method adds an account to the accounts.txt file and stores it in the text file.
    public void addAccountToFile(Account account) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream("accounts.txt", true);
            PrintWriter accountWriter = new PrintWriter(fileOutputStream);

            accountWriter.println(account.toString());
            accountWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // This method is used to change the username and password of an account, and updates the text file.
    public void changeAccountDetailsInFile(String oldUsername, String oldPassword, String newUsername,
            String newPassword) {

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

        FileOutputStream accountOutputStream;
        try {
            accountOutputStream = new FileOutputStream("accounts.txt", false);
            PrintWriter accountsWriter = new PrintWriter(accountOutputStream);

            for (String accountsDatum : accountsData) {
                accountsWriter.println(accountsDatum);
            }
            accountsWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Account thisAccount = null;
        try {
            thisAccount = this.getAccountByUsername(oldUsername);
        } catch (AccountNotExistException e) {
            e.printStackTrace();
        }

        ArrayList<Integer> conversationIds = thisAccount.getConversationIds();

        for (Integer conversationId : conversationIds) {

            ArrayList<String> conversationData = new ArrayList<>();

            try {
                FileReader filer = new FileReader(conversationId + ".txt");
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

            StringBuilder participantsToFile = new StringBuilder();

            for (String conversationParticipant : conversationParticipants) {
                participantsToFile.append(conversationParticipant).append(",");
            }
            participantsToFile = new StringBuilder(participantsToFile.substring(0, participantsToFile.length() - 1));
            conversationData.set(2, participantsToFile.toString());

            for (int j = 3; j < conversationData.size(); j++) {
                String[] messageSplit = conversationData.get(j).split(",", 4);
                if (messageSplit[2].equals(oldUsername)) {
                    messageSplit[2] = newUsername;
                }
                String newMessage = messageSplit[0] + "," + messageSplit[1] + "," + messageSplit[2] + ","
                        + messageSplit[3];
                conversationData.set(j, newMessage);
            }

            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(conversationId + ".txt", false);
                PrintWriter conversationWriter = new PrintWriter(fileOutputStream);
                for (String conversationDatum : conversationData) {
                    conversationWriter.println(conversationDatum);
                }
                conversationWriter.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // This method creates the conversation text file to save details of the conversation to, when a conversation
    // is created.
    public void createConversationFile(Conversation conversation) {

        FileOutputStream fileOutputStream;
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

    // This method adds messages into the conversation text file when the methods is called.
    public void writeMessageToConversationFile(Conversation conversation, Message message)
            throws FileNotFoundException {

        FileOutputStream fileOutputStream = new FileOutputStream(conversation.getConversationId() + ".txt", true);
        PrintWriter conversationWriter = new PrintWriter(fileOutputStream);

        conversationWriter.println(message.toString());
        conversationWriter.close();
    }

    // This method adds a new user into a conversation and into the conversation text file when it is called.
    public void addParticipantToConversationFile(int conversationID, String username) {

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

        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = new FileOutputStream(conversationID + ".txt", false);
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

    // This method removes a user from a conversation in the text file, and from the database.
    public void removeParticipantFromConversationFile(int conversationID, String username) {

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

        StringBuilder toWrite = new StringBuilder();

        for (String s : participantsList) {
            if (!s.equals(username)) {
                toWrite.append(s).append(",");
            }
        }

        toWrite = new StringBuilder(toWrite.substring(0, toWrite.length() - 1));

        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = new FileOutputStream(conversationID + ".txt", false);
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

    // This method edits the content of a message in the conversation text file.
    public void editMessageInConversationFile(int messageID, String newMessage) {

        int conversationID = -1;

        for (Conversation conversation : this.conversations) {
            for (int j = 0; j < conversation.getMessages().size(); j++) {
                if (conversation.getMessages().get(j).getId() == messageID) {
                    conversationID = conversation.getConversationId();
                    break;
                }
            }
        }

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

        int lineToEdit = -1;

        for (int i = 0; i < conversationFile.size(); i++) {
            if (conversationFile.get(i).startsWith(Integer.toString(messageID))) {
                lineToEdit = i;
            }
        }

        String editMessage = conversationFile.get(lineToEdit);
        String[] splitEditMessage = editMessage.split(",", 4);
        splitEditMessage[3] = newMessage;
        String toWrite = splitEditMessage[0] + "," + splitEditMessage[1] + "," + splitEditMessage[2] + ","
                + splitEditMessage[3];
        conversationFile.set(lineToEdit, toWrite);

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(conversationID + ".txt", false);
            PrintWriter conversationWriter = new PrintWriter(fileOutputStream);

            for (String s : conversationFile) {
                conversationWriter.println(s);
            }
            conversationWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    // This method is used to delete a message from the database, and from the text file.
    public int removeMessageById(int messageID) {
        int conversationID = -1;

        for (Conversation conversation : this.conversations) {
            for (int j = 0; j < conversation.getMessages().size(); j++) {
                if (conversation.getMessages().get(j).getId() == messageID) {
                    conversationID = conversation.getConversationId();
                    conversation.getMessages().remove(j);
                    break;
                }
            }
        }

        for (int i = 0; i < this.messages.size(); i++) {
            if (this.messages.get(i).getId() == messageID) {
                this.messages.remove(i);
            }
        }

        return conversationID;
    }

    // This method is used to delete a message from the conversation text file, and from the database.
    public void deleteMessageFromConversationFile(int messageID) {

        int conversationID = removeMessageById(messageID);

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
        int lineToRemove = -1;

        for (int i = 0; i < conversationFile.size(); i++) {
            if (conversationFile.get(i).startsWith(Integer.toString(messageID))) {
                lineToRemove = i;
            }
        }

        conversationFile.remove(lineToRemove);

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(conversationID + ".txt", false);
            PrintWriter conversationWriter = new PrintWriter(fileOutputStream);

            for (String s : conversationFile) {
                conversationWriter.println(s);
            }
            conversationWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // This method creates a CSV file from a conversation, and exports it to the folder of the client.
    public void createCSV(int conversationID) {

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

        String conversationName = conversationFile.get(1);

        conversationFile.remove(0);
        conversationFile.remove(0);
        conversationFile.remove(0);

        conversationFile.add(0, "MessageID,Timestamp,SenderUsername,Message");

        for (int i = 1; i < conversationFile.size(); i++) {
            String fileLine = conversationFile.get(i);
            String[] fileLineSplit = fileLine.split(",", 4);
            String newFileLine = "\"" + fileLineSplit[3] + "\"";
            conversationFile.set(i,
                    fileLineSplit[0] + "," + fileLineSplit[1] + "," + fileLineSplit[2] + "," + newFileLine);
        }

        FileWriter fileWriter;
        File csvFile = new File(conversationName + ".csv");

        try {
            fileWriter = new FileWriter(csvFile);

            for (String s : conversationFile) {
                fileWriter.append(s);
                fileWriter.append("\n");
            }
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This is the getter for the entire ArrayList of conversations stored in the database.
    public ArrayList<Conversation> getConversations() {
        return conversations;
    }

    // This is the getter for the entire ArrayList of accounts stored in the database.
    public ArrayList<Account> getAccounts() {
        return accounts;
    }
}