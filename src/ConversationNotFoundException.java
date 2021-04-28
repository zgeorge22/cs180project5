public class ConversationNotFoundException extends Exception {
    public ConversationNotFoundException(String message) {
        super(message);
    }

    public ConversationNotFoundException() {
        super();
    }
}