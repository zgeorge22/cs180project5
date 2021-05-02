package src;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientProcess2 {


    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Socket socket = new Socket("localhost", 4242);
        Database database = new Database(false); //TODO remove if database is being sent from server
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        String confirm;
        Scanner scan = new Scanner(System.in); //temp
        do {
            System.out.println("Enter loginAccount (0) or createAccount (1)"); //Testing Input
            String loginChoice = scan.nextLine();
            String choice = null;
            if (loginChoice.equals("0"))
                choice = "loginAccount";
            else if (loginChoice.equals("1"))
                choice = "createAccount";
            System.out.println("Username: ");
            String username = scan.nextLine();
            System.out.println("Password: ");
            String password = scan.nextLine();

            String login = getLogin(choice, username, password); //GUI connection
            writer.write(login);
            writer.println();
            writer.flush();


            confirm = reader.readLine();

        } while(confirm.equals("false"));

/*
        InputStream inputStream = socket.getInputStream();
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        Database clientData = (Database) objectInputStream.readObject();
        System.out.println("OIS works");

 */


        String exit = "false";
        do {
            // SENDING
            System.out.println("Enter Command: ");

            String GUIcmd = scan.nextLine(); //Placeholder GUI command

            writer.write(GUIcmd);
            writer.println();
            writer.flush();

            // RECEIVING
            String commandString = (reader.readLine());
            String[] token = commandString.split(" ");
            String inputcmd = token[0];
            String inputVar = null;

            switch (inputcmd) {
                case ("addConvo"):
                    inputVar = commandString.substring(commandString.indexOf(" ") + 1);
                    //GUI code
                    System.out.println(inputVar); //All println are temp
                    break;
                case ("addMsg"):
                    //GUI code
                    System.out.println(inputVar);
                    break;
                case ("removeUser"):
                    //GUI code
                    System.out.println(inputVar);
                    break;
                //case ("addMsg"):
                case ("editMsg"):
                    //GUI code
                    System.out.println(inputVar);
                    break;
                case ("removeMsg"):
                    //GUI code
                    System.out.println(inputVar);
                    break;
                case ("logoutTrue"):
                    exit = "true";
                    break;
            }

        }while(exit.equals("false"));



    }

    public static String getLogin(String choice, String username, String password) {
        String loginInformation = choice + " " + username + " " + password;
        return loginInformation;
    }

}