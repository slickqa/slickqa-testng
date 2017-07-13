package com.slickqa.testng;

import org.testng.ITestContext;
import org.testng.annotations.*;

/**
 * Created by slambson on 7/12/17.
 */
public class SlickBaseTest {

    private ThreadLocal<SlickResultLogger> logger;
    private ThreadLocal<SlickFileAttacher> fileAttacher;

    @BeforeMethod(alwaysRun = true)
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

    @BeforeTest(alwaysRun = true)
    public void setupTest() {
        System.out.println("SlickBaseTest.BeforeTest");
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
