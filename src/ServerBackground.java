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

    public static ArrayList<Account> activeUsers = new ArrayList<>();

    //TODO do we need these methods which are never called? Does this class need to exist?
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