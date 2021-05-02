//package com.company;
package src;

import java.util.ArrayList;

public class ServerBackground {

    public static ArrayList<Account> activeUsers = new ArrayList<>();

    public static void addUser(Account user) {
        ServerBackground.activeUsers.add(user);
    }

    public static void removeUser(Account user) {
        ServerBackground.activeUsers.remove(user);
    }

    public static ArrayList<Account> getActiveUsers() {
        return activeUsers;
    }
}