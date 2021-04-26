public class MainTesting {
    public static void main(String[] args) throws AccountNotExistException, InterruptedException,
            ConversationNotFoundException {

        // DATABASE.JAVA TESTING
        Database database = new Database(true);
        System.out.println(database.getAccountByUsername("jim").toString() + " " + database.getAccountByUsername("bean"));
        // "jim,jim bean,dumb" should be printed indicating that the accounts have been imported from text
        System.out.println(database.getConversationById(0).getMessages().get(0));
        // "0,2021-04-23T17:00:42.870743,guest,Hi, I am a guest user" should be printed indicating
        // that the conversations were correctly imported from text

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
            secondRealAccount = new Account("badUsername", "badPassword", database,
                    true);
        } catch (UsernameAlreadyExistsException e) {
            e.printStackTrace();
        }
        //Creates the account for "sheila" and should add it to the accounts.txt file
        System.out.println(database.getAccountByUsername("sheila") + " " + database.getAccountByUsername("badUsername"));
        //"sheila,sheilaPass badUsername,badPassword" should be printed, indicating that the accounts are
        // created and stored properly
        // The accounts.txt file should be updated accordingly.

        try {
            secondRealAccount.changeUsername("guest");
        } catch (UsernameAlreadyExistsException e) {
            e.printStackTrace();
            Thread.sleep(100);
            // A usernameAlreadyExists Exception should be thrown, since guest already exists as a username
            // The username and password in accounts.txt should be changed to "goodPass,goodUser"
            secondRealAccount.changePassword("goodPass");
            try {
                secondRealAccount.changeUsername("goodUser");
            } catch (UsernameAlreadyExistsException usernameAlreadyExistsException) {
                usernameAlreadyExistsException.printStackTrace();
            }
        }
        System.out.println(database.getAccountByUsername("goodUser"));
        // "goodUser,goodPass" should be printed, indicating that this account is stored in the database.

        System.out.println(database.getAccountByUsername("bob").getConversationIds().get(0) + ","
                + database.getAccountByUsername("bob").getConversationIds().get(1));
        //"0,1" should be printed, since bob is in Conversations 0, and 1.

        try {
            database.getAccountByUsername("bob").removeConversation(0);
            database.getAccountByUsername("sheila").addToConversation(0);
        } catch (ConversationNotFoundException e) {
            e.printStackTrace();
        }
        //bob should be removed and then sheila should be added in his place in the conversation with id 0 txt file

        System.out.println(database.getAccountByUsername("bob").getConversationIds().get(0));
        System.out.println(database.getAccountByUsername("sheila").getConversationIds().get(0));
        //"1" should be printed indicating that bob is no longer in conversation 0 in the database
        //"0" should be printed indicating that sheila has been added to conversation 0 in the database

        //////////////////////////////

        // CONVERSATION.JAVA TESTING




    }
}
// TODO:
// 1. Add isServer to Database Constructor
// 2. Change all static methods to non static.
// 3. Change all methods in the database to ensure they do not read/write unless isServer = true
//
// EXAMPLE
//
// public static void addToDatabase(Account account) {
//    accounts.add(account);
//    if (this.isServer == true) {
//        Database.addAccountToFile(account);
//    }
//}
//
// 4. Add a Database parameter to the constructor for Account, Conversation, Message
// 5. If any methods in conversation/account/message contain "Database.method" methods,
//  make sure that they only run if this.getDatabase.getIsServer = true
//
// EXAMPLE
//
// public void addParticipant(String username) throws AccountNotExistException {
//        Account account = Database.getAccountByUsername(username);
//        participants.add(account);
//        account.addToConversation(this);
//        if (this.getDatabase.getIsServer == true) {
//            this.getDatabase.addParticipantToConversationFile(this.getConversationId(), username);
//        }
//    }
