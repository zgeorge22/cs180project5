# CS 180 Project 5 README
___
**Submission**
---
- Report -
- Vocareum - 
- Video Presentation - 
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

Mention something about Ports? hostnames? etc.

To run the client, the main method within *client.java* should be run to start the program.
The next section will detail documentation about how to run the program.

- Login/Create Account
  - //add stuff
- Main GUI
  - Change Password
    - // add stuff
  - Create Conversation with participants
    - // add stuff
  - Send Message
    - // add stuff
  - Edit Message
    - // add stuff
  - Delete Message
    - // add stuff
  - Leave Conversation
    - // add stuff
  - Log Out
    - // add stuff
  - **Anything else that I might have forgotten?**     - // add stuff


  
___
**Testing/Test Cases**
---
- *DatabaseAccountConversationMessageTestCases.java*
- *GUITestCases.java*

***DatabaseAccountConversationMessageTestCases.java***

Testing for the Database classes (Database, Account, Conversation and Message) was done using this class.


In order to run the test cases, ensure that accounts.txt, 0.txt, and 1.txt exist in the source folder. NO OTHER
.txt files should exist in the source folder. If they do, delete them. These 3 text files should contain exactly
the following lines.

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


***GUITesting.java***

// add stuff

***Server/Client Testing***

Testing of the server and client was done by running the server and ensuring that messages were sent and received 
correctly. For this reason, there is not a separate class to denote Test Cases for the Server and client.

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
    not exist in the databse.
    
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
      
- DatabaseAccuontConversationMessageTestCases
  - This class contains test cases for all methods in the Database, Account, Conversation and Message Test Cases.
  - When it is run, the default .txt files which are provided as part of the submission will be changed to ensure 
    that all methods are functional.
    
- GUITestCases
  
  
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
* Instructions on how to compile and run your project.
* A list of who submitted which parts of the assignment on Brightspace and Vocareum. 
  * For example: Student 1 - Submitted Report on Brightspace. Student 2 - Submitted Vocareum workspace.
* A detailed description of each class. This should include the functionality included in the class, the testing done to verify it works properly, and its relationship to other classes in the project. 
* Descriptions of the testing done on each class. For GUI testing, provide step-by-step guidance on the tests performed.
