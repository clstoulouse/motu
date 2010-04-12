package fr.cls.atoll.motu.processor.wps;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target( { FIELD })
public @interface InputAnnotation {
    /**
     * @return <code>true</code> if this field is mandatory, <code>false</code> otherwise.
     */
    boolean mandatory() default true;

    /**
     * @return the export name of the field.
     */
    String value();
}
