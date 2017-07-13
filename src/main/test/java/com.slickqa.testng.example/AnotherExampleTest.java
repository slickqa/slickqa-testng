package com.slickqa.testng.example;

import com.slickqa.testng.annotations.SlickMetaData;
import com.slickqa.testng.annotations.Step;
import com.slickqa.testng.SlickBaseTest;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * An example Test
 */
public class AnotherExampleTest extends SlickBaseTest {

    @Test
    @SlickMetaData(title = "Example Test Three",
            component = "A very fine component",
            feature = "A feature for a very fine component",
            steps = {
                    @Step(step = "first step", expectation = "first step worked"),
                    @Step(step = "second", expectation = "second step worked")
            })
    public void exampleTestThree() {
    }

    @Test
    public void notReportedTest() {
    }

    @Test
    @SlickMetaData(title = "Example Test Four",
            component = "Another very fine component",
            feature = "A feature",
            steps = {
                    @Step(step = "first step that is different", expectation = "first step worked"),
                    @Step(step = "second step", expectation = "second step worked")
            })
    public void exampleTestFour() throws Exception {
    }

    @Test
    @SlickMetaData(title = "Example Logging Test",
            component = "Another very fine component",
            feature = "A feature",
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