import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Database {

    private ArrayList<Account> accounts;
    private ArrayList<Conversation> conversations;
    private ArrayList<Message> messages;
    private boolean isServer;

    public Database(boolean isServer) {
        ArrayList<Account> accountList = new ArrayList<>();
        ArrayList<Conversation> conversationList = new ArrayList<>();
        ArrayList<Message> messageList = new ArrayList<>();

        this.accounts = accountList;
        this.conversations = conversationList;
        this.messages = messageList;
        this.isServer = isServer;

        if (isServer) {
            this.startup();
        }
    }

    public boolean isServer() {
        return isServer;
    }

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
                    Account thisAccount = new Account(splitAccount[0], splitAccount[1], this, false);
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

                Conversation conversation = new Conversation(conversationData.get(1),
                        conversationParticipants, false, this);

                for (int i = 3; i < conversationData.size(); i++) {
                    String[] thisMessage = conversationData.get(i).split(",", 4);

                    Message message = new Message(Integer.parseInt(thisMessage[0]),
                            LocalDateTime.parse(thisMessage[1]), thisMessage[2], thisMessage[3],
                            false, this);
                    conversation.addMessage(message);
                }

                thisConversationId++;
                conversationFile = new File(thisConversationId + ".txt");
            } while (conversationFile.exists());
        }

        int messagesCreated = this.messages.size();
        Message.setNextMessageId(messagesCreated);
    }

    public Account getAccountByUsername(String username) throws AccountNotExistException {
        for (Account account : accounts) {
            if (username.equals(account.getUsername())) {
                return account;
            }
        }
        throw new AccountNotExistException();
    }

    public Conversation getConversationById(int id) throws ConversationNotFoundException {
        for (Conversation conversation : conversations) {
            if (id == conversation.getConversationId()) {
                return conversation;
            }
        }
        throw new ConversationNotFoundException();
    }

    public Message getMessageById(int id) throws MessageNotFoundException {
        for (Message message : messages) {
            if (id == message.getId()) {
                return message;
            }
        }
        throw new MessageNotFoundException();
    }

    public void createAccountFile() {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("accounts.txt", false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addToDatabase(Account account) {
        accounts.add(account);
        if (this.isServer && account.isAddToFile()) {
            this.addAccountToFile(account);
        }
    }

    public void addToDatabase(Conversation conversation) {
        conversations.add(conversation);

        if (this.isServer && conversation.isAddToFile()) {
            this.createConversationFile(conversation);
        }
    }

    public void addToDatabase(Message message) {
        messages.add(message);
    }

    public void addAccountToFile(Account account) {
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

    public void changeAccountDetailsInFile(String oldUsername, String oldPassword,
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

            String participantsToFile = "";

            for (String conversationParticipant : conversationParticipants) {
                participantsToFile = participantsToFile + conversationParticipant + ",";
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

    public void createConversationFile(Conversation conversation) {

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

    public void writeMessageToConversationFile(Conversation conversation, Message message) throws FileNotFoundException {

        FileOutputStream fileOutputStream = new FileOutputStream(conversation.getConversationId() + ".txt", true);
        PrintWriter conversationWriter = new PrintWriter(fileOutputStream);

        conversationWriter.println(message.toString());
        conversationWriter.close();
    }

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

        FileOutputStream fileOutputStream = null;

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

        String toWrite = "";

        for (String s : participantsList) {
            if (!s.equals(username)) {
                toWrite = toWrite + s + ",";
            }
        }

        toWrite = toWrite.substring(0, toWrite.length() - 1);

        FileOutputStream fileOutputStream = null;

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
        String toWrite = splitEditMessage[0] + "," + splitEditMessage[1] + "," + splitEditMessage[2] + "," + splitEditMessage[3];
        conversationFile.set(lineToEdit, toWrite);

        FileOutputStream fileOutputStream = null;
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

    public void deleteMessageFromConversationFile(int messageID) {

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

        FileOutputStream fileOutputStream = null;
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

    public void exportToCSV(int ConversationId) {
        // TODO

    }
}
