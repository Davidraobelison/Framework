package mg.itu.prom16.annotation.stereotype;

// Service.java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component  // Hérite de Component
public @interface Service {
    
}
