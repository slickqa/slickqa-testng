package com.slickqa.testng;

import com.slickqa.client.SlickClient;
import com.slickqa.client.errors.SlickError;
import com.slickqa.client.impl.SlickClientImpl;
import com.slickqa.client.model.Result;
import com.slickqa.testng.annotations.SlickMetaData;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * Base
 *
 * Also look at {@link com.slickqa.client.apiparts.FilesQueryApi}.
 *
 * @author Created by Sharon Lambson on 7/14/17.
 */
public class SlickBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void setupSlickThreads(Method testMethod, ITestContext testContext) {
        String testPlanName = testContext.getCurrentXmlTest().getName();
        SlickClient slickClient = new SlickClientImpl(SlickSuite.getSlickTestNGController().getBaseURL());
        SlickResult.setThreadSlickClient(slickClient);
        if(SlickResult.isUsingSlick()) {
            if (testMethod.getAnnotation(SlickMetaData.class) != null) {
                Result result = SlickSuite.getSlickTestNGController().getOrCreateResultFor(testMethod, testPlanName);
                SlickResult.setThreadCurrentResultId(result.getId());
                SlickResult.setThreadSlickResultLogger(new SlickResultLogger(result.getId()));
                SlickResult.setThreadSlickFileAttacher(new SlickFileAttacher((result.getId())));
                Result update = new Result();
                update.setStarted(new Date());
                update.setReason("");
                update.setRunstatus(SlickResult.RUNNING);
                try {
                    SlickSuite.getSlickTestNGController().updateResultFor(result.getId(), update);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!! ERROR: Unable to set result to starting. !!");
                }
            }
        }
    }

    @AfterMethod(alwaysRun = true)
    public void cleanupSlickThreads() {
        SlickResult.cleanupThreadLocal();
    }

    public static SlickResultLogger slickLog() {
        SlickResultLogger slickResultLogger;
        if (SlickResult.getThreadSlickResultLogger() != null) {
            slickResultLogger = SlickResult.getThreadSlickResultLogger();
        }
        else {
            slickResultLogger = new SlickResultLogger(SlickResultLogger.NOTDEFINED);
        }
        return slickResultLogger;
    }

    public static SlickFileAttacher slickFileAttach() {
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
