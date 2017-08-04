package com.slickqa.testng;

import com.slickqa.client.SlickClient;
import com.slickqa.client.errors.SlickError;
import com.slickqa.client.impl.SlickClientImpl;
import com.slickqa.client.model.Result;
import com.slickqa.testng.annotations.SlickMetaData;
import org.apache.logging.log4j.LogManager;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;
import java.util.Arrays;

import java.lang.reflect.Method;
import java.util.Date;

public class SlickResult implements IResultListener2  {
    private static ThreadLocal<String> threadCurrentResultId;
    private static ThreadLocal<SlickClient> threadSlickClient;
    private static ThreadLocal<SlickResultLogger> threadSlickResultLogger;
    private static ThreadLocal<SlickFileAttacher> threadSlickFileAttacher;
    private String RUNNING = "RUNNING";
    private String PASS = "PASS";
    private String FINISHED = "FINISHED";
    private String FAIL = "FAIL";
    private String BROKEN_TEST = "BROKEN_TEST";
    private String SKIPPED = "SKIPPED";

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(SlickResult.class);

    public boolean isUsingSlick() {
        if(SlickSuite.getSlickTestNGController() != null) {
            return SlickSuite.getSlickTestNGController().isUsingSlick();
        }
        return false;
    }

    @Override
    public void onTestStart(ITestResult testResult) {
        Method testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
        String testPlanName = testResult.getMethod().getXmlTest().getName();
        SlickClient slickClient = new SlickClientImpl(SlickSuite.getSlickTestNGController().getBaseURL());
        threadSlickClient.set(slickClient);
        if(isUsingSlick()) {
            if (testMethod.getAnnotation(SlickMetaData.class) != null) {
                Result result = SlickSuite.getSlickTestNGController().getOrCreateResultFor(testMethod, testPlanName);
                threadCurrentResultId.set(result.getId());
                threadSlickResultLogger.set(new SlickResultLogger(threadCurrentResultId.get()));
                threadSlickFileAttacher.set(new SlickFileAttacher(threadCurrentResultId.get()));
                Result update = new Result();
                update.setStarted(new Date());
                update.setReason("");
                update.setRunstatus(RUNNING);
                try {
                    result = SlickSuite.getSlickTestNGController().updateResultFor(result.getId(), update);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!! ERROR: Unable to set result to starting. !!");
                }
            }
        }
    }

    @Override
    public void beforeConfiguration(ITestResult testResult) {
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        if(isUsingSlick() && threadSlickResultLogger != null && threadSlickResultLogger.get() != null) {
            Result result = SlickSuite.getSlickTestNGController().getResultFor(testResult.getMethod().getConstructorOrMethod().getMethod());
            if (result != null) {
                Result update = new Result();
                update.setFinished(new Date());
                update.setStatus(PASS);
                update.setRunstatus(FINISHED);
                try {
                    SlickSuite.getSlickTestNGController().updateResultFor(result.getId(), update);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!! ERROR: Unable to pass result !!");
                }
            }
            threadSlickResultLogger.get().flushLogs();
        }
        else {
            logger.debug("Not logging to slick");
        }
        //cleanupThreadLocal();
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        String status = BROKEN_TEST;

        if(isUsingSlick() && threadSlickResultLogger != null && threadSlickResultLogger.get() != null) {
            Throwable cause = testResult.getThrowable();
            if (null != cause) {
                if (cause.toString().contains("java.lang.AssertionError")) {
                    status = FAIL;
                }
                threadSlickResultLogger.get().error(cause.toString());
                threadSlickResultLogger.get().error(Arrays.toString(cause.getStackTrace()).replace(" ", "\r\n"));
            }
            Result result = SlickSuite.getSlickTestNGController().getResultFor(testResult.getMethod().getConstructorOrMethod().getMethod());
            if (result != null) {
                Result update = new Result();
                update.setFinished(new Date());
                update.setStatus(status);
                update.setRunstatus(FINISHED);
                try {
                    SlickSuite.getSlickTestNGController().updateResultFor(result.getId(), update);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!! ERROR: Unable to fail result !!");
                }
            }
            threadSlickResultLogger.get().flushLogs();
        }
        else {
            logger.debug("Not logging to slick");
        }
        //cleanupThreadLocal();
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        if(isUsingSlick() && threadSlickResultLogger != null && threadSlickResultLogger.get() != null) {
            threadSlickResultLogger.get().debug("Test was skipped!");
            Result result = SlickSuite.getSlickTestNGController().getResultFor(testResult.getMethod().getConstructorOrMethod().getMethod());
            if (result != null) {
                Result update = new Result();
                update.setFinished(new Date());
                update.setStatus(SKIPPED);
                update.setRunstatus(FINISHED);
                try {
                    SlickSuite.getSlickTestNGController().updateResultFor(result.getId(), update);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!! ERROR: Unable to skip result !!");
                }
            }
            threadSlickResultLogger.get().flushLogs();
        }
        else {
            logger.debug("Not logging to slick");
        }
        //cleanupThreadLocal();
    }

    public static SlickClient getThreadSlickClient() {
        SlickClient slickClient;
        try {
            slickClient = threadSlickClient.get();
        } catch (NullPointerException e) {
            slickClient = new SlickClientImpl(SlickSuite.getSlickTestNGController().getBaseURL());
        }

        if (slickClient == null) {
            slickClient = new SlickClientImpl(SlickSuite.getSlickTestNGController().getBaseURL());
        }

        return slickClient;
    }

    public static void cleanupThreadLocal() {
        if (threadCurrentResultId != null && threadCurrentResultId.get() != null) {
            threadCurrentResultId.remove();
        }
        if (threadSlickClient != null && threadSlickClient.get() != null) {
            threadSlickClient.remove();
        }
        if (threadSlickResultLogger != null && threadSlickResultLogger.get() != null) {
            threadSlickResultLogger.remove();
        }
        if (threadSlickFileAttacher != null && threadSlickFileAttacher.get() != null) {
            threadSlickFileAttacher.remove();
        }
    }

    public static SlickResultLogger getThreadSlickResultLogger() {
        return threadSlickResultLogger.get();
    }

    public static SlickFileAttacher getThreadSlickFileAttacher() {
        return threadSlickFileAttacher.get();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult testResult) {
    }

    @Override
    public void onStart(ITestContext context) {
        threadCurrentResultId = new ThreadLocal<String>();
        threadSlickResultLogger = new ThreadLocal<SlickResultLogger>();
        threadSlickClient = new ThreadLocal<SlickClient>();
        threadSlickFileAttacher = new ThreadLocal<SlickFileAttacher>();
    }

    @Override
    public void onFinish(ITestContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConfigurationSuccess(ITestResult testResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConfigurationFailure(ITestResult testResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConfigurationSkip(ITestResult testResult) {
        // TODO Auto-generated method stub

    }
}
