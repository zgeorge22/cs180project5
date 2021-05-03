import java.util.ArrayList;

/**
 * This file contains test cases for the following classes: Database, Account, Conversation, Message
 * It tests if each of the methods works.
 *
 * <p>Purdue University -- CS18000 -- Spring 2021 -- Project 5</p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack Dorkin
 * @version May 3rd, 2021
 */

public class DatabaseAccountConversationMessageTestCases {
    public static void main(String[] args) throws AccountNotExistException, InterruptedException,
            ConversationNotFoundException, MessageNotFoundException {

        // DATABASE.JAVA TESTING
        Database database = new Database(true);
        System.out.println(database.getAccountByUsername("jim").toString() + " "
                + database.getAccountByUsername("bean"));
        // "jim,jim bean,bean" should be printed indicating that the accounts have been
        // imported from text
        System.out.println(database.getConversationById(0).getMessages().get(0));
        // "0,2021-04-23T17:00:42.870743,guest,Hi, I am a guest user" should be printed
        // indicating
        // that the conversations were correctly imported from text (note: this will no
        // longer exist in the
        // file because this message gets deleted later on).

        //////////////////////////////

        // ACCOUNT.JAVA TESTING
        try {
            Account failedAccount = new Account("bob", "barker", database, true);
        } catch (UsernameAlreadyExistsException e) {
            e.printStackTrace();
            Thread.sleep(100);
        }
        // UsernameAlreadyExistsException should be thrown
        System.out.println(database.getAccountByUsername("bob").getPassword());
        // "bob" should be printed, and NOT "barker", indicating that the new account
        // couldn't be added to the database.

        Account realAccount = null;
        Account secondRealAccount = null;
        try {
            realAccount = new Account("sheila", "sheilaPass", database, true);
            secondRealAccount = new Account("badUsername", "badPassword", database, true);
        } catch (UsernameAlreadyExistsException e) {
            e.printStackTrace();
        }
        // Creates the account for "sheila" and should add it to the accounts.txt file
        System.out
                .println(database.getAccountByUsername("sheila") + " " + database.getAccountByUsername("badUsername"));
        // "sheila,sheilaPass badUsername,badPassword" should be printed, indicating
        // that the accounts are
        // created and stored properly
        // The accounts.txt file should be updated accordingly.

        try {
            secondRealAccount.changeUsername("guest");
        } catch (UsernameAlreadyExistsException e) {
            e.printStackTrace();
            Thread.sleep(100);
            // A usernameAlreadyExists Exception should be thrown, since guest already
            // exists as a username
            // The username and password in accounts.txt should be changed to
            // "goodPass,goodUser"
            secondRealAccount.changePassword("goodPass");
            try {
                secondRealAccount.changeUsername("goodUser");
            } catch (UsernameAlreadyExistsException usernameAlreadyExistsException) {
                usernameAlreadyExistsException.printStackTrace();
            }
        }
        System.out.println(database.getAccountByUsername("goodUser"));
        // "goodUser,goodPass" should be printed, indicating that this account is stored
        // in the database.

        System.out.println(database.getAccountByUsername("bob").getConversationIds().get(0) + ","
                + database.getAccountByUsername("bob").getConversationIds().get(1));
        // "0,1" should be printed, since bob is in Conversations 0, and 1.

        try {
            database.getAccountByUsername("bob").removeConversation(0);
            database.getAccountByUsername("sheila").addToConversation(0);
        } catch (ConversationNotFoundException e) {
            e.printStackTrace();
        }
        // bob should be removed and then sheila should be added in his place in the
        // conversation with id 0 txt file

        System.out.println(database.getAccountByUsername("bob").getConversationIds().get(0));
        System.out.println(database.getAccountByUsername("sheila").getConversationIds().get(0));
        // "1" should be printed indicating that bob is no longer in conversation 0 in
        // the database
        // "0" should be printed indicating that sheila has been added to conversation 0
        // in the database

        //////////////////////////////

        // CONVERSATION.JAVA / MESSAGE.JAVA TESTING

        ArrayList<Account> thirdConversationAccounts = new ArrayList<>();
        thirdConversationAccounts.add(database.getAccountByUsername("sheila"));
        thirdConversationAccounts.add(database.getAccountByUsername("goodUser"));
        thirdConversationAccounts.add(database.getAccountByUsername("bean"));

        Conversation conversation = new Conversation("Third Conversation",
                thirdConversationAccounts, true, database);
        // A new text file called 2.txt should be created with the details for this
        // conversation - it should include
        // the users sheila, goodUser and bean (

        System.out.println(database.getConversationById(2).getConversationName());
        // "Third Conversation" should be printed in the terminal indicating this
        // conversation now
        // exists in the terminal

        System.out.println(database.getAccountByUsername("sheila").getConversationIds().get(0));
        // "2" should be printed in the terminal indicating that this conversation is in
        // sheila's list of conversations

        database.getConversationById(1).addParticipant("sheila");
        database.getConversationById(1).removeParticipant("guest");
        for (int i = 0; i < database.getConversationById(1).getParticipants().size(); i++) {
            System.out.print(database.getConversationById(1).getParticipants().get(i).getUsername() + " ");
        }
        System.out.println();
        // "bob bean sheila" should be printed in the terminal indicating that guest was
        // removed and bob was added.
        // 1.txt should reflect this change

        Message newMessage = new Message("sheila", "hello, my name is sheila", database);
        database.getConversationById(2).addMessage(newMessage);
        // 2.txt should now have this message
        System.out.println(database.getConversationById(2).getMessages().get(0).toString());
        // "" should be printed in the terminal indicating that the message is created
        // and exists in the conversation database.
        System.out.println(database.getMessageById(2).toString());
        // "" should be printed in the terminal indicating that the message is created
        // and exists in the messages database.

        Message newMessage2 = new Message("bean", "hello, my name is bean", database);
        database.getConversationById(2).addMessage(newMessage2);
        // Another message should be added to 2.txt

        database.getMessageById(1).editMessage("Hello, i'm editing this message");
        System.out.println(database.getMessageById(1).getContent());
        // "Hello, i'm editing this message" should be printed in the terminal,
        // indicating the
        // edited message has been updated in the database.
        // The message with id = 1 in 0.txt should have the content be set to be "Hello,
        // i'm editing this message"

        database.getMessageById(0).deleteMessage();
        System.out.println(database.getConversationById(0).getMessages().get(0));
        // "1,2021-04-23T17:00:42.870743,jim,Hello, i'm editing this message" should be
        // printed,
        // indicating that the message with ID 0 has been deleted.

        database.getConversationById(2).exportToCSV();
        // A csvFile named "conversationName".txt should be created with the details of
        // the messages.
    }
}