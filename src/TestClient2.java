import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TestClient2 {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 4242);

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream());

        Scanner scan = new Scanner(System.in);

        System.out.println("0 for login, 1 for new account: ");
        String loginChoice = scan.nextLine();
        writer.write(loginChoice);
        writer.println();
        writer.flush();

        System.out.println("Enter username: ");
        String username = scan.nextLine();
        writer.write(username);
        writer.println();
        writer.flush();

        System.out.println("Enter Password: ");
        String password = scan.nextLine();
        writer.write(password);
        writer.println();
        writer.flush();

        String end = reader.readLine();
        System.out.println(end);
    }
}
