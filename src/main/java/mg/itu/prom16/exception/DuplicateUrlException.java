package mg.itu.prom16.exception;

public class DuplicateUrlException extends Exception {
    
    public DuplicateUrlException(String nameUrlDuplicate, String verb) {
        super("Duplicate url "+ nameUrlDuplicate + " and verb " + verb + " exception ");
    }

}