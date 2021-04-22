import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    //TODO Implement timestamps properly

    private static int nextMessageId;
    private final int id;
    private LocalDateTime timestamp;
    private final String sender;
    private String content;

    public Message(LocalDateTime localDateTime, String senderUsername, String content) {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        this.timestamp = LocalDateTime.now();
        this.sender = senderUsername;
        this.content = content;
        this.id = getNextMessageId();

        setNextMessageId(++nextMessageId);

        Database.addToDatabase(this);
    }

    public Message(int id, Timestamp timestamp, String senderUsername, String content) {
        //TODO
        // What to do about this timestamp?
        // this.timestamp = timestamp;
        this.sender = senderUsername;
        this.content = content;
        this.id = id;
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

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {

        return this.getId() + ", " + this.getTimestamp().toString() + ", "
                + this.getSender() + ", " + this.getContent();
    }

    // This method doesn't go here
//    public static Message parseMessage(String message) {
//
//        String[] splitMessage = message.split(",", 4);
//
//        // TODO fix the timestamp stuff
//
//        return new Message(Integer.parseInt(splitMessage[0]), Timestamp.valueOf(splitMessage[1]),
//                splitMessage[2], splitMessage[3]);
//    }
}