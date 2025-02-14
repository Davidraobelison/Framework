package mg.itu.prom16.validation;

import java.util.List;

import mg.itu.prom16.util.ModelView;

public class BindingResult {
    List<FieldError> fieldErrors;
    ModelView backPage;

    public BindingResult(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
    
    public boolean hasErros() {
        return !fieldErrors.isEmpty();
    }

    public ModelView getBackPage() {
        return backPage;
    }

    public void setBackPage(ModelView backPage) {
        this.backPage = backPage;
    }
}
