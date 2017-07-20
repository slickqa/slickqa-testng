package com.slickqa.testng;

import org.testng.ITestContext;
import org.testng.annotations.*;

/**
 * Base
 *
 * Also look at {@link com.slickqa.client.apiparts.FilesQueryApi}.
 *
 * @author Created by Sharon Lambson on 7/14/17.
 */
@Listeners({SlickSuite.class, SlickResult.class})
public class SlickBaseTest {

    private ThreadLocal<SlickResultLogger> logger;
    private ThreadLocal<SlickFileAttacher> fileAttacher;

    @BeforeMethod(alwaysRun = true)
    public void setupMethod(ITestContext testContext) {
        SlickResult slickResult = (SlickResult) testContext.getAttribute(SlickResult.slickResultTestContextIdentifier);
        if (slickResult != null && slickResult.getSlickClient() != null) {
            logger.set(new SlickResultLogger(slickResult));
            fileAttacher.set(new SlickFileAttacher(slickResult));
        }
    }

    @AfterMethod
    public void cleanupMethod() {
        if (logger.get() != null) {
            logger.get().flushLogs();
        }
    }

    @BeforeTest(alwaysRun = true)
    public void setupTest() {
        System.out.println("SlickBaseTest.BeforeTest");
        logger = new ThreadLocal<>();
        fileAttacher = new ThreadLocal<>();
    }

    public SlickResultLogger slickLog() {
        if (logger.get() != null) {
            return logger.get();
        }
        return new SlickResultLogger(new SlickResult());
    }

    public SlickFileAttacher slickFileAttach() {
        if (fileAttacher.get() != null) {
            return fileAttacher.get();
        }
        return new SlickFileAttacher(new SlickResult());
    }
}
