package mg.itu.prom16.exception;

public class InvalidReturnTypeException extends Exception {
    
    public InvalidReturnTypeException(String returnType) {
        super("Invalid return type " + returnType);
    }

}