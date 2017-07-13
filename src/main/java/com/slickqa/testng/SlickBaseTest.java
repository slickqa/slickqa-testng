package com.slickqa.testng;

import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/**
 * Created by slambson on 7/12/17.
 */
public class SlickBaseTest {

    private ThreadLocal<SlickResultLogger> logger;
    private ThreadLocal<SlickFileAttacher> fileAttacher;

    @BeforeMethod
    public void setupMethod(ITestContext testContext) {
        System.out.println("SlickBaseTest.BeforeMethod");
        SlickResult slickResult = (SlickResult) testContext.getAttribute(SlickResult.slickResultTestContextIdentifier);
        logger.set(new SlickResultLogger(slickResult));
        fileAttacher.set(new SlickFileAttacher(slickResult));
    }

    @AfterMethod
    public void cleanupMethod() {
        logger.get().flushLogs();
    }

    @BeforeSuite
    public void setupSuite() {
        System.out.println("SlickBaseTest.BeforeSuite");
        logger = new ThreadLocal<>();
        fileAttacher = new ThreadLocal<>();
    }

    public SlickResultLogger slickLog() {
        return logger.get();
    }

    public SlickFileAttacher slickFileAttach() {
        return fileAttacher.get();
    }
}
