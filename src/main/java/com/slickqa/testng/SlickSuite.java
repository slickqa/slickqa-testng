package com.slickqa.testng;

import com.slickqa.testng.annotations.SlickMetaData;
import org.testng.IInvokedMethod;
import org.testng.ISuiteListener;
import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.internal.ClassHelper;
import org.testng.xml.XmlTest;
import org.testng.xml.XmlClass;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import org.testng.annotations.Test;
import java.lang.annotation.Annotation;


public class SlickSuite implements ISuiteListener {

    public final static SlickTestNGController controller = SlickTestNGControllerFactory.getControllerInstance();

    @Override
    public void onFinish(ISuite suite) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStart(ISuite suite) {
        try {
            controller.initializeController();
            controller.createSuiteResults(suite.getAllMethods());
        } catch (Exception e) {
            System.out.println("exception: " + e.getMessage());
        }
    }

    public static SlickTestNGController getSlickTestNGController() {
        return controller;
    }
}
