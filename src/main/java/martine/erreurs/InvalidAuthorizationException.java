package martine.erreurs;

public class InvalidAuthorizationException extends Exception {
    public InvalidAuthorizationException() {
    }

    public InvalidAuthorizationException(Throwable cause) {
        super(cause);
    }

    public InvalidAuthorizationException(String message) {
        super(message);
    }
}
