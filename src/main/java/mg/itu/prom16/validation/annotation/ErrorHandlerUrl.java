package mg.itu.prom16.validation.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})

public @interface ErrorHandlerUrl {
    String url() default "";
    String verb() default "GET";
}
