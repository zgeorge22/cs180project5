
import javax.xml.crypto.Data;
import java.sql.Timestamp;

public class Message {
    //TODO Implement timestamps properly

    private static int nextMessageId;
    private final int id;
    private Timestamp timestamp;
    private final String sender;
    private String content;

    public Message(Timestamp timestamp, String senderUsername, String content) {
        this.timestamp = timestamp;
        this.sender = senderUsername;
        this.content = content;
        this.id = getNextMessageId();

        setNextMessageId(++nextMessageId);

        Database.addToDatabase(this);
    }

    public Message(int id, Timestamp timestamp, String senderUsername, String content) {
        this.timestamp = timestamp;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {

        return this.getId() + "," + this.getTimestamp().toString() + ","
                + this.getSender() + "," + this.getContent();
    }

    public static Message parseMessage(String message) {

        String[] splitMessage = message.split(",", 4);

        // Does the timestamp valueOf work?

        return new Message(Integer.parseInt(splitMessage[0]), Timestamp.valueOf(splitMessage[1]),
                splitMessage[2], splitMessage[3]);

        // OR should it be this:
        // return Database.getMessageById(Integer.parseInt(splitMessage[0]));
    }


}