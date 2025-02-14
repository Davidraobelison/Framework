package mg.itu.prom16.validation.constraints;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})

public @interface Min {
    double value() default 0;
    String message() default " Minimum is 0 " ;
}

