package no.ndla.taxonomy;

public class MissingParameterException extends RuntimeException {

    public MissingParameterException(String message, int lineNumber) {
        super("Line " + lineNumber + ": " + message);
    }
}
