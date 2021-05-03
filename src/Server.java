import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Server extends Thread {
    private int serverPort;
    private static ArrayList<ServerProcess> serverList = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("SERVER - Starting Up!");

        Database database = new Database(true);
        ArrayList<Account> activeUsers = new ArrayList<>();

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

    public static ArrayList<ServerProcess> getServerList() {
        return serverList;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}