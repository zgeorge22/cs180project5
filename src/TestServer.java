package src;

import java.time.LocalDateTime;

/*
Commands: (-> means broadcast to relevant & online clients)

createAccount username password

loginAccount username password

editUsername username
-> updateAccount oldUsername newUsername

editPassword password

leaveConvo conversationID
-> removeUser conversationID username

editMsg conversationID messageID content
-> editMsg conversationID messageID sender timestamp content

deleteMsg conversationID messageID
-> removeMsg conversationID messageID

logoutAccount

-> succeeded
-> failed

*/

public class TestServer {
    Client client;

    public TestServer(Client client) {
        this.client = client;
    }

    public boolean receivedCreateConvo(String participantsString, String sender, String content) {
        System.out.println("SERVER - Received createConvo for [" + participantsString + "] with initialMsg [" + content
                + "] from [" + sender + "]");

        Conversation.setNextConversationId(Conversation.getNextConversationId() + 1);
        client.receivedAddConvo(Conversation.getNextConversationId() + " " + participantsString);

        Message.setNextMessageId(Message.getNextMessageId() + 1);
        client.receivedAddMsg(Conversation.getNextConversationId() + " " + Message.getNextMessageId() + " " + sender
                + " " + LocalDateTime.now() + " " + content);

        return true;
    }

    public boolean receivedCreateMessage(int conversationID, String sender, String content) {
        System.out.println("SERVER - Received createMessage for conversationID [" + conversationID + "] with content ["
                + content + "] from [" + sender + "]");

        Message.setNextMessageId(Message.getNextMessageId() + 1);
        client.receivedAddMsg(conversationID + " " + Message.getNextMessageId() + " " + sender + " "
                + LocalDateTime.now() + " " + content);

        return true;
    }
}
