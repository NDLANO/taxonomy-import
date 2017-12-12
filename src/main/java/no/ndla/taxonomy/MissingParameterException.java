package no.ndla.taxonomy;

public class MissingParameterException extends RuntimeException {

    public MissingParameterException(String message, int lineNumber) {
        super("Line " + lineNumber + ": " + message);
    }

    public MissingParameterException(String ressurstype) {
        super("Specification is missing header: " + ressurstype);
    }
}
