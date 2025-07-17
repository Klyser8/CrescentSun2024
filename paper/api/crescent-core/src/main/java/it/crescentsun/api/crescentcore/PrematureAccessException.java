package it.crescentsun.api.crescentcore;

/**
 * Represents an exception that is thrown when a method is accessed prematurely, such as instances
 * where methods are called before one or more object within it are fully initialized.
 */
public class PrematureAccessException extends RuntimeException {

    public PrematureAccessException(String message) {
        super(message);
    }

}
