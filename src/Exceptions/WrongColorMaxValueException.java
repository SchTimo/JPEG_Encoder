package Exceptions;

/**
 * Created by timo on 12.10.15.
 */
public class WrongColorMaxValueException extends Exception {

    public WrongColorMaxValueException() {
    }

    public WrongColorMaxValueException(String message) {
        super(message);
    }
}
