package Exceptions;

/**
 * Created by timo on 16.01.16.
 */
public class InvalidPPMFileException extends Exception {

    public InvalidPPMFileException(){}

    public InvalidPPMFileException(String message){
        super(message);
    }
}
