import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private static int nextMessageId;
    private final int id;
    private final LocalDateTime timestamp;
    private final String sender;
    private String content; //Remove Final if editing messages is to be allowed.
    private boolean addToFile;
    private Database database;

    // Call this constructor for creating new messages when users send.
    public Message(LocalDateTime localDateTime, String senderUsername, String content, Database database) {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        this.timestamp = LocalDateTime.now();
        this.sender = senderUsername;
        this.content = content;
        this.id = getNextMessageId();
        this.addToFile = true;
        this.database = database;

        setNextMessageId(++nextMessageId);

        this.database.addToDatabase(this);
    }

    // Do not call this constructor for creating new messages.
    public Message(int id, LocalDateTime timestamp, String senderUsername, String content,
                   boolean addToFile, Database database) {

        this.timestamp = timestamp;
        this.sender = senderUsername;
        this.content = content;
        this.id = id;
        this.addToFile = addToFile;
        this.database = database;

        this.database.addToDatabase(this);
    }

    public static int getNextMessageId() {
        return nextMessageId;
    }

    public static void setNextMessageId(int nextMessageId) {
        Message.nextMessageId = nextMessageId;
    }

    public int getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public void editMessage(String content) {
        this.content = content;
        this.database.editMessageInConversationFile(this.getId(), content);
    }

    public void deleteMessage() {
        this.database.deleteMessageFromConversationFile(this.getId());
    }

    public boolean isAddToFile() {
        return addToFile;
    }

    public String toString() {

        return this.getId() + "," + this.getTimestamp().toString() + ","
                + this.getSender() + "," + this.getContent();
    }

}

