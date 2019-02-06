package com.slickqa.testng;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

@Plugin(name = "SlickLogAppender", category = "Core", elementType = "apender", printObject = true)
public class SlickLogAppender extends AbstractAppender {

    protected SlickLogAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout);
    }

    @Override
    public void append(LogEvent event) {
        System.out.println("message: " + event.getMessage().toString());
        try {
            SlickResultLogger slickResultLogger = SlickResult.getThreadSlickResultLogger();
            if (slickResultLogger != null) {
                if (event.getLevel() == Level.DEBUG) {
                    slickResultLogger.setLoggerName(event.getLoggerName());
                    slickResultLogger.debug(event.getMessage().getFormattedMessage());
                }
                if (event.getLevel() == Level.INFO) {
                    slickResultLogger.setLoggerName(event.getLoggerName());
                    slickResultLogger.info(event.getMessage().getFormattedMessage());
                }
                if (event.getLevel() == Level.WARN) {
                    slickResultLogger.setLoggerName(event.getLoggerName());
                    slickResultLogger.warn(event.getMessage().getFormattedMessage());
                }
                if (event.getLevel() == Level.ERROR) {
                    slickResultLogger.setLoggerName(event.getLoggerName());
                    slickResultLogger.error(event.getMessage().getFormattedMessage());
                }
                if (event.getLevel() == Level.TRACE) {
                    slickResultLogger.setLoggerName(event.getLoggerName());
                    slickResultLogger.trace(event.getMessage().getFormattedMessage());
                }
                slickResultLogger.flushLogs();
                slickResultLogger.setLoggerName(SlickResultLogger.defaultLoggerName);
            }
        } catch (Exception e) {
        }
    }
    
    @PluginFactory
    public static SlickLogAppender createAppender(@PluginAttribute("name") String name,
                                                  @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                  @PluginElement("Filter") final Filter filter,
                                                  @PluginAttribute("otherAttribute") String otherAttribute) {
        if (name == null) {
            LOGGER.error("No name provided for TestAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new SlickLogAppender(name, filter, layout);
    }
}