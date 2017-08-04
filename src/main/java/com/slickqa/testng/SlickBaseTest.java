package com.slickqa.testng;

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

    @AfterMethod(alwaysRun = true)
    public void cleanupSlickBaseTest() {
        SlickResult.cleanupThreadLocal();
    }

    public SlickResultLogger slickLog() {
        SlickResultLogger slickResultLogger;
        if (SlickResult.getThreadSlickResultLogger() != null) {
            slickResultLogger = SlickResult.getThreadSlickResultLogger();
        }
        else {
            slickResultLogger = new SlickResultLogger(SlickResultLogger.NOTDEFINED);
        }
        return slickResultLogger;
    }

    public SlickFileAttacher slickFileAttach() {
        SlickFileAttacher slickFileAttacher;
        if (SlickResult.getThreadSlickFileAttacher() != null) {
            slickFileAttacher =  SlickResult.getThreadSlickFileAttacher();
        }
        else {
            return new SlickFileAttacher(SlickResultLogger.NOTDEFINED);
        }
        return slickFileAttacher;
    }
}
