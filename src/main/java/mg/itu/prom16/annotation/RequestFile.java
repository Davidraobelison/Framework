package mg.itu.prom16.annotation;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})

public @interface RequestFile {
    String value() default "";
}

