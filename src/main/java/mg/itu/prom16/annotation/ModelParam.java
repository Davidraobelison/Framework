package mg.itu.prom16.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)

public @interface ModelParam {
    String value() default "";
}
