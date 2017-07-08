package com.slickqa.testng.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This should be applied only to @Test methods.
 * SlickMetaData will be used to create Results
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
public @interface SlickMetaData {
    String value = "";

    String title();
    String component() default value;
    String feature() default value;
    String automationId() default value;
    String automationKey() default value;
    Step[] steps();
}
