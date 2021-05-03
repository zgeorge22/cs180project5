# CS 180 Project 5 README
Authors:
- Rishi Banerjee
- Zach George
- Benjamin Davenport
- Jack Dorkin
- Natalie Wu
___
**Submission**
---
- Report - Submitted by Natalie Wu
- Vocareum - Submitted by Zach George
- Video Presentation - Submitted by Benjamin Davenport
___
**Usage**
---
- *Server.java*
- *Client.java*

***server.java***

The first time that the server is run, ensure there are no text files in the source folder of the server, 
that may exist due the test cases creating them. The given text files as a part of the submission, are such 
text files which are used only for testing purposes, and should be removed before starting the server.

Once all .txt files are removed, the server can be started, by running the "main" method within *server.java*

If you wish to change the port from the default port (*4242*), then edit the code for *server.java* by changing the 
number in this line at the top:  
`private static final int port = 4242;`  



***client.java***

Each client must have the following classes:
- Account
- AccountNotExistException
- Client
- Conversation
- ConversationNotFoundException
- Database
- LoginWindow
- MainWindow
- Message
- UsernameAlreadyExistsException

To run the client, the main method within *client.java* should be run to start the program.
The next section will detail documentation about how to run the program.

If the server is not running on localhost, then edit the code for *client.java* by changing the following
String in this line at the top, and changing it to the ip address of the server:  
`private static final String ip = "localhost";`

If the server has a different port number from the default port (*4242*), then edit the code for *client.java* 
by changing the number in this line at the top:  
`private static final int port = 4242;`

- Login/Create Account
  - Users must enter a valid and existing username and password (i.e. no non-alphanumeric characters) to login
  - To create an account, the username must not already exist within the file of usernames, username must follow 
    the guidelines shown before. Errors are thrown otherwise.
- Main GUI
  - Change Password
    - To change their password, a user must first login to their account. Once they are logged in, they can select 
      the “Account” button in the bottom left. This will prompt them with a change password GUI. They can enter the
      new password that they want (it must adhere to the same standards as the account creation) and press “Ok” to change their password. They can also press cancel to stop the changing process.
  - Create Conversation with participants
    - User must first click the “Create chat” button. Then, they add usernames of other users that they wish to 
      add to a chat separated by a comma into the “participants” text box. If the user attempts to send a message to a
      nonexistent user or improperly formats the participants list, errors will be thrown. User can type the first 
      message of a new chat into the message box, then either press enter or the send arrow to begin a conversation.
  - Send Message
    - User can select from their list of created conversations by clicking on them at the left. From there, they
      must make sure that their drop down box on the bottom right is on “Send”. Then they can simply type their 
      message and either press enter or press the send arrow on the right side of the text box.
  - Edit Message
    - To edit a message (users can only edit their own messages) users can change the drop down box to the “edit”
      function, and then select the message they want to edit. They then can modify the message in the text box, 
      and press enter as if they are sending the message. The message will be changed for both the user and those 
      they are in a conversation with.
  - Delete Message
    - To delete a message (users can only delete their own messages) users can change the drop down box to the
      “delete” function. They then can select the message that they want to delete by clicking on it, then follow 
      it up by pressing Enter. The message will be deleted on all ends.
  - Leave Conversation
    - To leave a conversation, users can select a conversation from the list of chats and press the 
      “Leave Conversation” button. It will be removed from their list and they will be removed from the chat.
  - Log Out
    - While logged in, the user can either press the “Sign Out” button or close the window in any fashion that they would like, and they will be signed out of the program and it will close.
  - Export CSV
    - To export a conversation to a CSV file, a user must first make or select a chat that they would like to export. Once they have selected the conversation, they can click the “Export Chat” button.
  
___
**Testing/Test Cases**
---

***DatabaseAccountConversationMessageTestCases.java***

Testing for the Database classes (Database, Account, Conversation and Message) was done using this class.


In order to run the test cases, create 3 text files: *accounts.txt*, *0.txt*, and *1.txt* in the source folder.
NO OTHER .txt files should exist in the source folder. If they do, delete them. 
These 3 text files should contain exactly the following lines. Ensure that there are no extra spaces or lines.

*accounts.txt* should have 4 lines of text.  
`guest,guest`  
`jim,jim`  
`bob,bob`  
`bean,person`

*0.txt* should have 5 lines of text:  
`0`  
`New Conversation`  
`jim,bob`  
`0,2021-04-23T17:00:42.870743,guest,Hi, I am a guest user `  
`1,2021-04-23T17:00:42.870743,jim,Hi, I am jim`

*1.txt* should have 3 lines of text:  
`1`  
`Other Conversation`  
`guest,bob,bean` 

The code within the .java should then be run, once these three files exist.
Comments within the .java file describe the intended output, and the terminal will throw exceptions
and print statements as described in those comments.

---
***GUI/Server/Client Testing***

The GUI was integrated very closely with the Client. For this reason, the GUI can only be tested by running the client. 
In order to do this, the server and client need to be functioning. By this point, the entire application is running,
and therefore we felt that including GUI Test cases and/or Server/Client related test cases was not prudent. 

In order to test these, the application should be run, and if the functions of the application correctly execute, 
the GUI, as well as the server and client should be running successfully.

___
**Class Descriptions**
---
- Account 
  - Allows the creation of accounts with usernames and passwords.
  - Accounts will be stored in databases, and marked as participants within Conversations.
  - Accounts are able to send messages.
  
- AccountNotExistException
  - This exception is thrown when Accounts are queried, but do not exist in the database.
  - Thrown when user tries to login with invalid login details, or tries to create conversations with users who do 
    not exist in the database.
    
- Client
  - This is the class which the client should run to access the program. It creates the GUI and displays it to the user.
  - The client communicates with the server and sends messages, and receives messages from the server, 
    in order to update the GUI.
    
- Conversation
  - Allows the creation of Conversation objects. 
  - Conversations generate with unique ID numbers.
  - Conversations have participants who are able to send messages to the conversation.
  - Messages are added to conversations, as the user sends messages.    
  - Conversations are stored in the database.
  - Conversations can be exported to CSV by the user.
  
- ConversationNotFoundException
  - This exception is thrown when Conversations are queried, but do not exist in the database.
  - Generally not thrown by user input in any way.
  
- Database
  - An object that stores Accounts, Conversations and Messages.
  - The client will contain a local database with the accounts that they are in conversation with, as well as those 
    conversations and any messages within those conversations.
    - If a conversation is requested to export to CSV, the database will handle the export.
  - The server contains a database with all Accounts, Conversations, and Messages ever sent.
  - The server database writes Accounts, Conversations, and Messages into .txt files stored serverside.
    - If the server crashes and starts over, it reads in the .txt files and recreates the database, ensuring that data
      is never lost, and all Accounts/Conversations/Messages persist.
      
- DatabaseAccountConversationMessageTestCases
  - This class contains test cases for all methods in the Database, Account, Conversation and Message Test Cases.
  - When it is run, the default .txt files which are provided as part of the submission will be changed to ensure 
    that all methods are functional.
  
- LoginWindow
  - Contains the code for displaying the GUI for the Login Window.
  - It handles situations when usernames or passwords entered are empty strings.
  
- MainWindow
  - This class contains the code for the main GUI of the chatting application.
  - It contains buttons to manage the account (change password), create and remove oneself from conversations,
    send messages, view messages from other users, edit messages and delete messages from conversations.

- Message
  - This class allows for the creation of Message objects.
  - Message objects are generated with a unique ID, and the timestamp of when they were created.
  - The client generates these objects each time a user wishes to send a message.
  - They also generate keeping track of the user which generated it and the content of their message.
  
- MessageNotFoundException
  - This exception is thrown when Messages are queried, but do not exist in the database.
  - When a message is sent to a user, if it does not exist in their database, this will be thrown, the message will 
    then be added to their database  
  - Generally not thrown by user input in any way.
  
- Server
  - This class is the main server class that utilizes other server classes.
  - This class is the only server class that needs to be runned by the user before running the client class. The user should not worry about running the ServerBackground nor the ServerProcess, the Server will handle calling those two classes on its own.
  - This class runs ServerProcess as a thread.
  
- ServerBackground
  - This class updates the array list of all active users that are online.
  
- ServerProcess
  - This class accepts connections from the client and reads in the input commands from the client.
  - This class can update the database with new accounts, messages, and conversations based on the commands.
  - This class can also send messages to another client that is connected to a different thread.
  - This class can also write commands back to the client it's connected to.
  
- UsernameAlreadyExistsException
  - This exception is thrown when a user attempts to create an account with a username that already exists.

___