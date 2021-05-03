import java.time.LocalDateTime;

/**
 * This is the class that allows for the creation of message objects.
 *
 * <p>Purdue University -- CS18000 -- Spring 2021 -- Project 5</p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack Dorkin
 * @version May 3rd, 2021
 */

public class Message {

    private static int nextMessageId; //Static integer that is used to generate messageIDs
    private final int id; // The messageID of the message object.
    private final LocalDateTime timestamp;  // The time the message was initially sent.
    private final String sender; // The username of the sender.
    private String content; // The contents of the message.
    private boolean addToFile; // The boolean that decides if the message is added to the file or not.
    private Database database; // The database that the message is stored in.


    // When Messages are created, addToFile is set to true, in order to add it to the text file.
    // Messages retrieved from an existing file will have addToFile = false, ensuring they are not duplicated in the
    // text files.
    public Message(String senderUsername, String content, Database database) {

        this.timestamp = LocalDateTime.now();
        this.sender = senderUsername;
        this.content = content;
        this.id = getNextMessageId();
        this.addToFile = true;
        this.database = database;

        setNextMessageId(++nextMessageId);

        this.database.addToDatabase(this);
    }

    // This constructor generates message objects given the parameters. This is only used when reading in
    // messages from text files.
    public Message(int id, LocalDateTime timestamp, String senderUsername, String content, boolean addToFile,
                   Database database) {

        this.timestamp = timestamp;
        this.sender = senderUsername;
        this.content = content;
        this.id = id;
        this.addToFile = addToFile;
        this.database = database;

        this.database.addToDatabase(this);
    }

    // Getter for the nextMessageID which is used to set the Message ID
    public static int getNextMessageId() {
        return nextMessageId;
    }

    // Setter for the nextMessageID which is called when messages are created to increment the messageID.
    public static void setNextMessageId(int nextMessageId) {
        Message.nextMessageId = nextMessageId;
    }

    // Getter for the MessageID
    public int getId() {
        return id;
    }

    // Getter for the sender of the message.
    public String getSender() {
        return sender;
    }

    //Getter for the timestamp of the message.
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    //Getter for the content of the message.
    public String getContent() {
        return content;
    }

    //Method that is used to edit the content of a message, and requests the database to edit the file.
    public void editMessage(String content) {
        this.content = content;

        if (this.database.isServer()) {
            this.database.editMessageInConversationFile(this.getId(), content);
        }
    }

    // Method that deletes a Message
    public void deleteMessage() {
        if (this.database.isServer()) {
            this.database.deleteMessageFromConversationFile(this.getId());
        }
    }

    // Getter for the AddToFile parameter.
    public boolean isAddToFile() {
        return addToFile;
    }

    // Converts the Message object into a String.
    public String toString() {
        return this.getId() + "," + this.getTimestamp().toString() + "," + this.getSender() + "," + this.getContent();
    }
}