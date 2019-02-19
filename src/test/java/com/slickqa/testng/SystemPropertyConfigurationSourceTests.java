package com.slickqa.testng;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class SystemPropertyConfigurationSourceTests {

    public static final String TEST_PROPERTY_NAME = "slick.unittest.configuration";

    @Test
    public void testGetConfigurationEntrySingleParameter() {
        SlickConfigurationSource config = new SystemPropertyConfigurationSource();
        String expected = "foo";
        assertNull(config.getConfigurationEntry(TEST_PROPERTY_NAME));
        System.setProperty(TEST_PROPERTY_NAME, expected);
        String actual = config.getConfigurationEntry(TEST_PROPERTY_NAME);
        assertEquals(expected, actual,
                 "Configuration property '" + TEST_PROPERTY_NAME + "' should have been equal to \"" +
                      expected + "\", was: " + actual);
    }
}
