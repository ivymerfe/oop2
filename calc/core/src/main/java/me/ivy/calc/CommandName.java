package me.ivy.calc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares command names associated with command implementation class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandName {
    /**
     * Command names and aliases.
     *
     * @return supported command tokens
     */
    String[] value();
}