package com.slickqa.testng;

import com.slickqa.testng.annotations.SlickMetaData;
import org.apache.logging.log4j.LogManager;
import org.testng.IInvokedMethod;
import org.testng.ISuiteListener;
import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.internal.ClassHelper;
import org.testng.xml.XmlTest;
import org.testng.xml.XmlClass;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import org.testng.annotations.Test;
import java.lang.annotation.Annotation;


public class SlickSuite implements ISuiteListener {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(SlickSuite.class);

    public final static SlickTestNGController controller = SlickTestNGControllerFactory.getControllerInstance();

    @Override
    public void onFinish(ISuite suite) {
        // TODO Auto-generated method stub
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
