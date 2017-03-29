package no.ndla.taxonomy;

public class MissingParameterException extends RuntimeException {

    public MissingParameterException(int column) {
        super("Compulsory parameter in column " + column + " is missing.");
    }
}
