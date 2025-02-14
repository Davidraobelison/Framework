package mg.itu.prom16.validation.constraints;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})

public @interface Max {
    double value() default 0;
    String message() default " Maximum  is 0 " ;
}

