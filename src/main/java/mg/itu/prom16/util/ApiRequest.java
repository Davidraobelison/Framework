package mg.itu.prom16.util;

import java.lang.reflect.Method;

public class ApiRequest {
    // Champs de la classe
    private Class<?> class1;
    private Method method;

    // Constructeur
    public ApiRequest(Class<?> class1, Method method) {
        this.class1 = class1;
        this.method = method;
    }

    public Class<?> getClass1() {
        return class1;
    }

    public void setClass1(Class<?> class1) {
        this.class1 = class1;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
    
    // Méthode pour afficher les détails de la requête
    public void printRequestDetails() {
        System.out.println("HTTP Verb: " + class1.getSimpleName());
        System.out.println("Method: " + method);
    }
}
