package mg.itu.prom16.util;

import java.lang.reflect.Method;
import java.util.HashMap;

public class Mapping {
    
    Class<?> class1;
    HashMap<String, Method> apiRequests;

    
    public Mapping(Class<?> class1, HashMap<String, Method> apiRequests) {
        this.class1 = class1;
        this.apiRequests = apiRequests;
    }

    public Class<?> getClass1() {
        return class1;
    }

    public void setClass1(Class<?> class1) {
        this.class1 = class1;
    }

    public HashMap<String, Method> getApiRequests() {
        return apiRequests;
    }

    public void setApiRequests(HashMap<String, Method> apiRequests) {
        this.apiRequests = apiRequests;
    }
}
