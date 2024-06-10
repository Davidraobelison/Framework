package mg.itu.prom16.util;

import java.lang.reflect.Method;

public class Mapping {
    
    Class<?> class1;
    Method method;

    
    public Mapping(Class<?> class1, Method method) {
        this.class1 = class1;
        this.method = method;
    }


    public Method getMethod() {
        return method;
    }


    public void setMethod(Method method) {
        this.method = method;
    }


    public Class<?> getClass1() {
        return class1;
    }

    public void setClass1(Class<?> class1) {
        this.class1 = class1;
    }
}
