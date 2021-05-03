import java.util.ArrayList;

/**
 * This class keeps track of the number of users which are online or active at a given point in time.
 *
 * <p>Purdue University -- CS18000 -- Spring 2021 -- Project 5</p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack Dorkin
 * @version May 3rd, 2021
 */

public class ServerBackground {

    // activeUsers ArrayList keeps track of which users are online to know who can receive live messages.
    public static ArrayList<Account> activeUsers = new ArrayList<>();

    // getActiveUsers returns all users who are active on the server.
    public static ArrayList<Account> getActiveUsers() {
        return activeUsers;
    }
}