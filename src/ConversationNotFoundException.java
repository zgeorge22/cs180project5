/**
 * This is the exception thrown if a Conversation does not exist, but is searched for.
 *
 * <p>Purdue University -- CS18000 -- Spring 2021 -- Project 5</p>
 *
 * @author Rishi Banerjee, Zach George, Natalie Wu, Benjamin Davenport, Jack Dorkin
 * @version May 3rd, 2021
 */

public class ConversationNotFoundException extends Exception {
    public ConversationNotFoundException(String message) {
        super(message);
    }

    public ConversationNotFoundException() {
        super();
    }
}
