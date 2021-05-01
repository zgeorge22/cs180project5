import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {

    public static void main(String[] args) throws IOException {
        ArrayList<Account> activeUsers = new ArrayList<>();
        ServerSocket serverSocket = new ServerSocket(1111);
        while(true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client Connected " + clientSocket.getPort());
            ServerProcess process = new ServerProcess(clientSocket);
            process.start();
        }
    }
}
