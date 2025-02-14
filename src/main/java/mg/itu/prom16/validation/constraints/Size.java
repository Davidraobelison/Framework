package mg.itu.prom16.validation.constraints;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})

public @interface Size {
    int min() default 0;
    int max() default 0;
    String message() default "" ;
}
