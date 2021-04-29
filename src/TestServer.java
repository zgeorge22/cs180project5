import java.time.LocalDateTime;

public class TestServer {
    Client client;

    public TestServer(Client client) {
        this.client = client;
    }

    public boolean receivedNewChat(String participantsString, String content) {
        String[] usernames = participantsString.split(",");

        // CREATE A NEW CHAT WITH A NEW IDEA
        // "BROADCAST" NEW CHAT TO ALL USERS
        // "BROADCAST" INITIAL MESSAGE TO ALL USERS

        return true;
    }

    public boolean receivedMessage(int conversationID, String sender, String content) {
        System.out.println(
                "SERVER - Received message from " + sender + " for conversationID " + conversationID + ": " + content);

        Message.setNextMessageId(Message.getNextMessageId() + 1);

        // BROADCAST ALL CLINETS
        client.receivedMessage(conversationID, Message.getNextMessageId(), sender, LocalDateTime.now(), content);

        return true;
    }
}
