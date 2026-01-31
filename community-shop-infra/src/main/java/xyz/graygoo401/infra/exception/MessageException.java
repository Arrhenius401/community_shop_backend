package xyz.graygoo401.infra.exception;

public class MessageException extends RuntimeException {
    public MessageException(String message) {
        super(message);
    }
}
