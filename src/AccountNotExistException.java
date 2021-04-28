public class AccountNotExistException extends Exception {
    public AccountNotExistException(String message) {
        super(message);
    }

    public AccountNotExistException() {
        super();
    }
}