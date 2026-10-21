package io.github.ansonliao.selenium.testng;

import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class XmlSuiteBuilderTest {

    @Test
    public void setXmlSuiteGenerateInfoPopulatesSuiteMetadata() {
        XmlSuite suite = XmlSuiteBuilder.setXmlSuiteGenerateInfo(new XmlSuite());
        assertEquals(suite.getName(), "Selenium Web UI Test");
        assertEquals(suite.getParallel(), XmlSuite.ParallelMode.TESTS);
        assertFalse(suite.getPreserveOrder());
        assertEquals(suite.getThreadCount(),
                Runtime.getRuntime().availableProcessors());
        assertEquals(suite.getVerbose(), Integer.valueOf(2));
    }

    @Test
    public void addXmlSuiteListenersRegistersConfiguredListeners() {
        XmlSuite suite = XmlSuiteBuilder.addXmlSuiteListeners(new XmlSuite());
        List<String> listeners = suite.getListeners();
        assertEquals(listeners.size(), 2);
        assertTrue(listeners.contains(
                "io.github.ansonliao.selenium.testng.TestResultListener"));
        assertTrue(listeners.contains(
                "io.github.ansonliao.selenium.parallel.SeleniumParallelTestListener"));
    }
}
