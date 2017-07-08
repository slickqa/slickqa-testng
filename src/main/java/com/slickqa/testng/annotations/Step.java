package com.slickqa.testng.annotations;

/**
 * For creating steps for SlickMetaData
 */
public @interface Step {
    String step();
    String expectation() default "Not Available";
}
