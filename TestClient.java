package com.company;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TestClient {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 4242);

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        System.out.println("connected!");
        Scanner scan = new Scanner(System.in);

        /* commented out for debug purpose
        System.out.println("0 for login, 1 for new account: ");
        String loginChoice = scan.nextLine();
        writer.write(loginChoice);
        writer.println();
        writer.flush();
        */
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

        // added for debug purpose
        System.out.println("which user you want to send direct message to?");
        String messageUser = scan.nextLine();
        writer.write(messageUser);
        writer.println();
        writer.flush();

        System.out.println("Type in the message you want to send: ");
        String message = scan.nextLine();
        writer.write(message);
        writer.println();
        writer.flush();
        // end

        String output = reader.readLine();
        while (output != null) {
            System.out.println(message);
            output = reader.readLine();
        }

    }
}

