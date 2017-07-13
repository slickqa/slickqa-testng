package com.slickqa.testng;

import com.slickqa.client.SlickClient;
import com.slickqa.client.errors.SlickError;
import com.slickqa.client.model.Result;
import com.slickqa.testng.annotations.SlickMetaData;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;

import java.lang.reflect.Method;
import java.util.Date;

public class SlickResult implements IResultListener2  {

    private SlickTestNGController slickTestNGController;

    private ThreadLocal<Result> currentResult;

    //private ThreadLocal<SlickLogger> logger;

    private ThreadLocal<SlickFileAttacher> fileAttacher;

    private boolean triedToInitialize;

    public static String slickResultTestContextIdentifier = "slickResult";

    public SlickClient getSlickClient() {
        if(isUsingSlick()) {
            return getSlickTestNGController().getSlickClient();
        } else {
            return null;
        }
    }

    public boolean isUsingSlick() {
        boolean retval = false;

        SlickTestNGController controller = getSlickTestNGController();
        if(controller != null && controller.isUsingSlick()) {
            retval = true;
        }

        return retval;
    }

    private SlickTestNGController getSlickTestNGController() {
        if(!triedToInitialize) {
            slickTestNGController = SlickTestNGControllerFactory.getControllerInstance();
            triedToInitialize = true;
        }
        return slickTestNGController;
    }

    public Result getCurrentResult() {
        Result current = null;
        if(isUsingSlick()) {
            current = currentResult.get();
        }
        return current;
    }

    @Override
    public void onTestStart(ITestResult testResult) {
        //logger.set(new SlickResultLogger(this));
        //testResult.getTestContext().setAttribute("slickLogger", logger.get());
        Method testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
        if(isUsingSlick() && testMethod.getAnnotation(SlickMetaData.class) != null) {
            Result result = getSlickTestNGController().getOrCreateResultFor(testMethod);
            Result update = new Result();
            update.setStarted(new Date());
            update.setReason("");
            update.setRunstatus("RUNNING");
            try {
                result = getSlickClient().result(result.getId()).update(update);
                currentResult.set(getSlickClient().result(result.getId()).get());
            } catch (SlickError e) {
                e.printStackTrace();
                System.err.println("!! ERROR: Unable to set result to starting. !!");
                currentResult.set(null);
            }
        } else {
            currentResult.set(null);
        }
    }

    @Override
    public void beforeConfiguration(ITestResult testResult) {
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        if(isUsingSlick()) {
            Result result = getSlickTestNGController().getResultFor(testResult.getMethod().getConstructorOrMethod().getMethod());
            if (result != null) {
                //SlickLogger slickLogger = (SlickLogger) testResult.getTestContext().getAttribute("slickLogger");
                //slickLogger.flushLogs();
                Result update = new Result();
                update.setFinished(new Date());
                update.setStatus("PASS");
                update.setRunstatus("FINISHED");
                try {
                    getSlickClient().result(result.getId()).update(update);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!! ERROR: Unable to pass result !!");
                }
            }
        }
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        if(isUsingSlick()) {
            Result result = getSlickTestNGController().getResultFor(testResult.getMethod().getConstructorOrMethod().getMethod());
            if (result != null) {
                //SlickLogger slickLogger = (SlickLogger) testResult.getTestContext().getAttribute("slickLogger");
                //slickLogger.flushLogs();
                Result update = new Result();
                update.setFinished(new Date());
                update.setStatus("FAIL");
                update.setRunstatus("FINISHED");
                try {
                    getSlickClient().result(result.getId()).update(update);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!! ERROR: Unable to pass result !!");
                }
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        if(isUsingSlick()) {
            Result result = getSlickTestNGController().getResultFor(testResult.getMethod().getConstructorOrMethod().getMethod());
            if (result != null) {
                //SlickLogger slickLogger = (SlickLogger) testResult.getTestContext().getAttribute("slickLogger");
                //slickLogger.flushLogs();
                Result update = new Result();
                update.setFinished(new Date());
                update.setStatus("SKIPPED");
                update.setRunstatus("FINISHED");
                try {
                    getSlickClient().result(result.getId()).update(update);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!! ERROR: Unable to pass result !!");
                }
            }
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult testResult) {
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("SlickResult.onStart");
        triedToInitialize = false;
        slickTestNGController = null;
        currentResult = new ThreadLocal<>();
        //logger = new ThreadLocal<>();
        currentResult.set(null);
        context.setAttribute(slickResultTestContextIdentifier, this);
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
