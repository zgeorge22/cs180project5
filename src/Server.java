//package com.company;

import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server extends Thread {
    private int serverPort;
    private static ArrayList<ServerProcess> serverList = new ArrayList<>();

    public static void main(String[] args) {
        ArrayList<Account> activeUsers = new ArrayList<>();
        try {
            ServerSocket serverSocket = new ServerSocket(4242);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Connected " + clientSocket.getPort());
                ServerProcess process = new ServerProcess(clientSocket); // this line has error
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