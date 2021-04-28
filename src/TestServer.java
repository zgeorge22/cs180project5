import java.time.LocalDateTime;

public class TestServer {
    Client client;

    public TestServer(Client client) {
        this.client = client;
    }

    public boolean receivedMessage(int conversationID, String sender, String content) {
        System.out.println(
                "SERVER - Received message from " + sender + " for conversationID " + conversationID + ": " + content);

        Message.setNextMessageId(Message.getNextMessageId() + 1);
        client.receivedMessage(conversationID, Message.getNextMessageId(), sender, LocalDateTime.now(), content);

        return true;
    }
}
