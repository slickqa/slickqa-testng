package com.slickqa.testng;

import com.slickqa.client.errors.SlickError;
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
