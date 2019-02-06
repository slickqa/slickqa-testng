package com.slickqa.testng.example;

import com.slickqa.testng.SlickBaseTest;
import com.slickqa.testng.annotations.SlickMetaData;
import com.slickqa.testng.annotations.Step;

/**
 * An example Test
 */
public class AnotherExampleTest extends SlickBaseTest {

    // Assertion failures will be marked in Slick as a Failed test
    @Test
    @SlickMetaData(title = "Example Assertion Failure - Failed Test",
            component = "A very fine component",
            feature = "A feature for a very fine component",
            steps = {
                    @Step(step = "first step", expectation = "first step worked"),
                    @Step(step = "second", expectation = "second step worked")
            })
    public void exampleTestAssertionFailedTest() {
        Assert.assertTrue(false, "This is failing because of an assertion so it will be marked as Failed");
    }

    // This test has no SlickMetaData so it will not post results to slick
    @Test
    public void exampleTestNotReported() {
    }

    // Tests that throw an exception other than assertion will be marked in Slick as a Broken test
    @Test
    @SlickMetaData(title = "Example Exception Thrown - Broken Test",
            component = "Another very fine component",
            feature = "A feature",
            steps = {
                    @Step(step = "first step that is different", expectation = "first step worked"),
                    @Step(step = "second step", expectation = "second step worked")
            })
    public void exampleTestExceptionBrokenTest() throws Exception {
        throw new Exception("This test threw an exception so it will be marked as Broken");
    }

    @Test
    @SlickMetaData(title = "Example Logging Test",
            component = "Another very fine component",
            feature = "Logging to Slick",
            steps = {
                    @Step(step = "first step that isn't different", expectation = "first step worked"),
                    @Step(step = "second step", expectation = "second step worked"),
                    @Step(step = "weird, a third step", expectation = "that it is all over")
            })
    public void exampleLoggingTest() throws Exception {
        slickLog().debug("Hello");
        slickLog().info("This is an info {0}", "message.");
        for(int i = 0; i < 10; i++) {
            slickLog().warn("This message is {0} of {1} {2}.", i + 1, 10, "messages");
        }
    }
}