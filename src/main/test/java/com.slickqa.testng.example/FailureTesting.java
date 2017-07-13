package com.slickqa.testng.example;

import com.slickqa.testng.annotations.SlickMetaData;
import com.slickqa.testng.annotations.Step;
import com.slickqa.testng.SlickBaseTest;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * An example Test
 */
public class ExampleTest extends SlickBaseTest {

    @Test
    @SlickMetaData(title = "Assertion Failure",
            component = "A very fine component",
            steps = {
                    @Step(step = "first step", expectation = "first step worked"),
                    @Step(step = "second")
            })
    public void exampleTestOne() throws Exception {
        Assert.as
    }

    @Test
    @SlickMetaData(title = "Unexpected exception",
            component = "Another very fine component",
            feature = "A feature",
            steps = {
                    @Step(step = "first step", expectation = "first step worked"),
                    @Step(step = "second")
            })
    public void exampleFileAttachTest() throws Exception {
        Path file = Paths.get(ExampleTest.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                .resolve(Paths.get("com", "slickqa", "junit", "example", "screenshot.png"));
        slickFileAttach.addFile(file);
    }
}