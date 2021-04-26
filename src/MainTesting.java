public class MainTesting {
    public static void main(String[] args) throws AccountNotExistException, InterruptedException {

        Database database = new Database(true);

        System.out.println(database.getAccountByUsername("jim").toString() + " " + database.getAccountByUsername("bean"));
        // "jim,jim bean,dumb" should be printed

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

        try {
            Account realAccount = new Account("sheila", "sheilaPass", database, true);
        } catch (UsernameAlreadyExistsException e) {
            e.printStackTrace();
        }
        //Creates the account for "sheila" and should add it to the accounts.txt file
        System.out.println(database.getAccountByUsername("sheila"));
        //"sheila,sheilaPass" should be printed, indicating that the account is stored in the database


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
