import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

/**
 * This is the main server class. When it is run, the server starts up and is ready to allow clients to connect. The
 * server handles all data storage and receives messages from clients and sends it to the respective recipients of
 * those messages.
 *
 * <p>Purdue University -- CS18000 -- Spring 2021 -- Project 5</p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack Dorkin
 * @version May 3rd, 2021
 */

public class Server extends Thread {
    private int serverPort;
    private static ArrayList<ServerProcess> serverList = new ArrayList<>();

    // This main class must be run to start the server.
    public static void main(String[] args) {
        System.out.println("SERVER - Starting Up!");
        // database reads in all stored Database information from past server runs.
        Database database = new Database(true);
        ArrayList<Account> activeUsers = new ArrayList<>();
        // try method connects a new client, creates a new ServerProcess for that client, and adds that ServerProcess
        // to the active processes in serverList for later retrieval.
        try {
            ServerSocket serverSocket = new ServerSocket(4242);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("SERVER - Client Connected: " + clientSocket.getPort());
                ServerProcess process = new ServerProcess(clientSocket, database); // this line has error
                serverList.add(process);
                process.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Allows for the serverList to be returned in each ServerProcess
    public static ArrayList<ServerProcess> getServerList() {
        return serverList;
    }
}