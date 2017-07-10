package com.slickqa.testng;

import org.testng.ISuiteListener;
import org.testng.ISuite;
import org.testng.internal.ClassHelper;
import org.testng.xml.XmlTest;
import org.testng.xml.XmlClass;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Iterator;
import org.testng.annotations.Test;
import java.lang.annotation.Annotation;


public class SlickSuite implements ISuiteListener {

    public final SlickTestNGController controller = SlickTestNGControllerFactory.getControllerInstance();

    @Override
    public void onFinish(ISuite suite) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStart(ISuite suite) {
        controller.createSuiteResults(suite.getXmlSuite().getTests());
    }
}
