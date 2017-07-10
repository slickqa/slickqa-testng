package com.slickqa.testng;

/**
 * This is a factory for SlickTestNGController.  If you want to subclass and extend the default functionality to
 * override defaults you need to replace the Controller
 */
public class SlickTestNGControllerFactory {

    public static Class<? extends SlickTestNGController> ControllerClass = SlickTestNGController.class;

    public static SlickTestNGController INSTANCE = null;

    public static synchronized SlickTestNGController getControllerInstance() {
        if(SlickTestNGControllerFactory.INSTANCE == null) {
            try {
                INSTANCE = ControllerClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return SlickTestNGControllerFactory.INSTANCE;
    }
}