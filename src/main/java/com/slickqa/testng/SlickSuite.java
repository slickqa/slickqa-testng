package com.slickqa.testng;

import org.apache.logging.log4j.LogManager;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGMethod;

import java.util.ArrayList;
import java.util.List;


public class SlickSuite implements ISuiteListener {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(SlickSuite.class);

    public final static SlickTestNGController controller = SlickTestNGControllerFactory.getControllerInstance();

    @Override
    public void onFinish(ISuite suite) {
        controller.resetController();
    }

    @Override
    public void onStart(ISuite suite) {
        try {
            List<ITestNGMethod> testNGMethods = suite.getAllMethods();
            List<String> testPlanNames = new ArrayList<String>();
            if (testNGMethods != null) {
                for (ITestNGMethod method : testNGMethods) {
                    String testPlan = method.getXmlTest().getName();
                    if (testPlan != null) {
                        if (!testPlanNames.contains(testPlan)) {
                            testPlanNames.add(testPlan);
                        }
                    }
                }
            }
            controller.initializeController(testPlanNames);
            controller.createSuiteResults(suite.getAllMethods());
        } catch (Exception e) {
            logger.error("exception: " + e.getMessage());
        }
    }

    public static SlickTestNGController getSlickTestNGController() {
        return controller;
    }
}
