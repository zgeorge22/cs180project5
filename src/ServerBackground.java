//package com.company;
package src;

import java.util.ArrayList;

/**
 * ServerBackground
 *
 * The ServerBackground class creates an ArrayList of users who are active on the server.
 *
 * Past homework assignments
 *
 * @author Natalie Wu, Benjamin Davenport
 * @version Due 05/03/2021
 *
 */

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