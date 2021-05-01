package src;

import java.time.LocalDateTime;

/*
Commands: (-> means broadcast to relevant & online clients)

createAccount username password

loginAccount username password

editUsername username
-> updateAccount oldUsername newUsername

editPassword password

createConvo participantsString initialMsg
-> addConvo conversationID participantsString
-> addMsg conversationID initialMsg

leaveConvo conversationID
-> removeUser conversationID username

createMsg conversationID content
-> addMsg conversationID messageID sender timestamp content

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

    public boolean receivedNewChat(String participantsString, String content) {
        String[] usernames = participantsString.split(",");

        // CREATE A NEW CHAT WITH A NEW ID
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
