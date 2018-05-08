package com.slickqa.testng;

import com.slickqa.client.SlickClient;
import com.slickqa.client.errors.SlickError;
import com.slickqa.client.impl.SlickClientImpl;
import com.slickqa.client.model.LogEntry;
import com.slickqa.client.model.Result;
import com.slickqa.testng.annotations.SlickMetaData;
import org.apache.logging.log4j.LogManager;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;
import java.util.Arrays;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Date;

public class SlickResult implements IResultListener2  {
    private static ThreadLocal<String> threadCurrentResultId;
    private static ThreadLocal<SlickClient> threadSlickClient;
    private static ThreadLocal<SlickResultLogger> threadSlickResultLogger;
    private static ThreadLocal<SlickFileAttacher> threadSlickFileAttacher;
    public  static String RUNNING = "RUNNING";
    private String PASS = "PASS";
    private String FINISHED = "FINISHED";
    private String FAIL = "FAIL";
    private String BROKEN_TEST = "BROKEN_TEST";
    private String SKIPPED = "SKIPPED";

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(SlickResult.class);

    public static boolean isUsingSlick() {
        if(SlickSuite.getSlickTestNGController() != null) {
            return SlickSuite.getSlickTestNGController().isUsingSlick();
        }
        return false;
    }

    @Override
    public void beforeConfiguration(ITestResult testResult) {
    }

    @Override
    public void onTestStart(ITestResult testResult) {

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
                StringWriter sw = new StringWriter();
                cause.printStackTrace(new PrintWriter(sw));
                update.setReason(sw.toString());
                try {
                    SlickSuite.getSlickTestNGController().updateResultFor(result.getId(), update);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!! ERROR: Unable to fail result !!");
                }
            }
            SlickMetaData metaData = testResult.getMethod().getConstructorOrMethod().getMethod().getAnnotation(SlickMetaData.class);
            if(metaData != null && metaData.triageNote() != null && !"".equals(metaData.triageNote())) {
                threadSlickResultLogger.get().debug(metaData.triageNote());

                String triageNote = metaData.triageNote();
                LogEntry triageNoteEntry = new LogEntry();
                triageNoteEntry.setLoggerName("slick.note");
                triageNoteEntry.setLevel("WARN");
                triageNoteEntry.setEntryTime(new Date());
                triageNoteEntry.setMessage(metaData.triageNote());

                SlickResultLogger triageLogger = new SlickResultLogger(threadCurrentResultId.get());
                triageLogger.setLoggerName("slick.note");
                triageLogger.addLogEntry(triageNoteEntry);
                triageLogger.flushLogs();

            }
            threadSlickResultLogger.get().flushLogs();
        }
        else {
            logger.debug("Not logging to slick");
        }
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        if(isUsingSlick() && threadSlickResultLogger != null) {
            Throwable cause = testResult.getThrowable();
            if (threadSlickResultLogger.get() == null) {
                Method testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
                String testPlanName = testResult.getMethod().getXmlTest().getName();
                Result result = SlickSuite.getSlickTestNGController().getOrCreateResultFor(testMethod, testPlanName);
                threadCurrentResultId.set(result.getId());
                threadSlickResultLogger.set(new SlickResultLogger(threadCurrentResultId.get()));
                threadSlickFileAttacher.set(new SlickFileAttacher(threadCurrentResultId.get()));
            }
            threadSlickResultLogger.get().debug("Test was skipped!");
            Result result = SlickSuite.getSlickTestNGController().getResultFor(testResult.getMethod().getConstructorOrMethod().getMethod());
            if (result != null) {
                Result update = new Result();
                update.setFinished(new Date());
                update.setStatus(SKIPPED);
                update.setRunstatus(FINISHED);
                StringWriter sw = new StringWriter();
                cause.printStackTrace(new PrintWriter(sw));
                update.setReason(sw.toString());
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
        cleanupThreadLocal();
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

    public static void setThreadSlickClient(SlickClient slickClient) {
        threadSlickClient.set(slickClient);
    }

    public static void setThreadCurrentResultId(String resultId) {
        threadCurrentResultId.set(resultId);
    }

    public static void setThreadSlickResultLogger(SlickResultLogger slickResultLogger) {
        threadSlickResultLogger.set(slickResultLogger);
    }

    public static void setThreadSlickFileAttacher(SlickFileAttacher slickFileAttacher) {
        threadSlickFileAttacher.set(slickFileAttacher);
    }
}
