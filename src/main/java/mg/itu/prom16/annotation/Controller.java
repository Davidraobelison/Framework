package mg.itu.prom16.annotation;
import java.lang.annotation.*;

import mg.itu.prom16.annotation.stereotype.Component;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Component  // Le controller est un composant géré par le conteneur IoC
public @interface Controller {
    String value() default "";
}
