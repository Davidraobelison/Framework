package mg.itu.prom16.validation;
import java.io.Serializable;
import java.util.List;

public class FieldError implements Serializable {
    private String field;          // Le nom du champ
    private String errorMessage;   // Le message d'erreur
    private Object rejectedValue;  // La valeur soumise invalide
    private String errorCode;      // Le code d'erreur (ex: "NotBlank", "Size", etc.)

    // Constructeur

    public static String getSpanError(List<FieldError> fieldErrors, String colorError, String nameField) {
        String str = "";
        if (fieldErrors == null) {
            return str;   
        }
        for (FieldError fieldError : fieldErrors) {
            if (nameField.equals(fieldError.getField())) {
                str += "<span style='color: "+ colorError + ";'>"+ fieldError.getErrorMessage() + 
                        "</span><br>";
            }
        }
        return str;
    }

    public FieldError(String field, String errorMessage, Object rejectedValue, String errorCode) {
        this.field = field;
        this.errorMessage = errorMessage;
        this.rejectedValue = rejectedValue;
        this.errorCode = errorCode;
    }

    public FieldError(String field, String errorMessage) {
        this.field = field;
        this.errorMessage = errorMessage;
    }

    // Getters et Setters
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    // Méthode toString() pour le débogage
    @Override
    public String toString() {
        return "FieldError{" +
               "field='" + field + '\'' +
               ", errorMessage='" + errorMessage + '\'' +
               ", rejectedValue=" + rejectedValue +
               ", errorCode='" + errorCode + '\'' +
               '}';
    }
}
