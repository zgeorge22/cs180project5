package src;

public class UsernameAlreadyExistsException extends Exception {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    public UsernameAlreadyExistsException() {
        super();
    }
}