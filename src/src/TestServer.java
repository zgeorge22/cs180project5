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

    public boolean receivedEditPassword(String password, String user) {
        System.out.println("SERVER - Received editPassword to [" + password + "] for [" + user + "]");

        try {
            client.getDatabase().getAccountByUsername(user).changePassword(password);

            return true;
        } catch (AccountNotExistException e) {
            e.printStackTrace(); // should never happen!
            return false;
        }
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

    public boolean receivedLeaveConvo(int conversationID, String sender) {
        System.out.println(
                "SERVER - Received leaveConvo for conversationID [" + conversationID + "] from [" + sender + "]");

        client.receivedRemoveUser(conversationID + " " + sender);

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

    public boolean receivedEditMsg(int conversationID, int messageID, String sender, String content) {
        System.out.println("SERVER - Received editMsg for conversationID [" + conversationID + "] and messageID ["
                + messageID + "] with content [" + content + "]");

        try {
            Message message = client.getDatabase().getMessageById(messageID);
            if (message.getSender().equals(sender)) {
                message.editMessage(content);
                client.receivedEditMsg(conversationID + " " + message.getId() + " " + sender + " "
                        + message.getTimestamp() + " " + content); // update timestamp as well?
                return true;
            }
        } catch (MessageNotFoundException e) {
            e.printStackTrace(); // should never happen!
        }

        return false;
    }

    public boolean receivedDeleteMsg(int conversationID, int messageID, String sender) {
        System.out.println("SERVER - Received deleteMsg for conversationID [" + conversationID + "] and messageID ["
                + messageID + "]");

        try {
            Message message = client.getDatabase().getMessageById(messageID);
            if (message.getSender().equals(sender)) {
                message.deleteMessage();
                client.receivedRemoveMsg(conversationID + " " + message.getId());
                return true;
            }
        } catch (MessageNotFoundException e) {
            e.printStackTrace(); // should never happen!
        }

        return false;
    }
}
