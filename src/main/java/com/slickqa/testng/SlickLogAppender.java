package com.slickqa.testng;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Created by slambson on 7/26/17.
 */
public class SlickLogAppender extends AppenderBase<ILoggingEvent> {

    public void append(ILoggingEvent event) {
        try {
            SlickResultLogger slickResultLogger = SlickResult.getThreadSlickResultLogger();
            if (slickResultLogger != null) {
                if (event.getLevel() == Level.DEBUG) {
                    slickResultLogger.setLoggerName(event.getLoggerName());
                    slickResultLogger.debug(event.getMessage());
                }
                if (event.getLevel() == Level.INFO) {
                    slickResultLogger.setLoggerName(event.getLoggerName());
                    slickResultLogger.info(event.getMessage());
                }
                if (event.getLevel() == Level.WARN) {
                    slickResultLogger.setLoggerName(event.getLoggerName());
                    slickResultLogger.warn(event.getMessage());
                }
                if (event.getLevel() == Level.ERROR) {
                    slickResultLogger.setLoggerName(event.getLoggerName());
                    slickResultLogger.error(event.getMessage());
                }
                if (event.getLevel() == Level.TRACE) {
                    slickResultLogger.setLoggerName(event.getLoggerName());
                    slickResultLogger.trace(event.getMessage());
                }
                slickResultLogger.flushLogs();
                slickResultLogger.setLoggerName(SlickResultLogger.defaultLoggerName);
            }
        } catch (Exception e) {
            System.out.println("!! ERROR: post logger message to Slick: " + e.getMessage());
            System.out.println(e.getStackTrace().toString());
        }
    }
}