package com.github.ansonliao.selenium.testng;

import com.github.ansonliao.selenium.utils.TestNGListenerUtils;
import com.google.common.collect.ImmutableMultiset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.xml.XmlSuite;

public class XmlSuiteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(XmlSuiteBuilder.class);
    private static final List<String> defaultListeners = ImmutableMultiset.of(
            "com.github.ansonliao.selenium.testng.TestResultListener",
            "com.github.ansonliao.selenium.parallel.SeleniumParallelTestListener").asList();

    public static XmlSuite build() {
        XmlSuite xmlSuite = new XmlSuite();

        // set generate xml suite information
        xmlSuite = setXmlSuiteGenerateInfo(xmlSuite);

        // listeners
        xmlSuite = addXmlSuiteListeners(xmlSuite);

        // add xml tests
        // XmlTestBuilder.setXmlSuite(xmlSuite);
        // XmlTestBuilder.build();
        // xmlSuite = XmlTestBuilder.getXmlSuite();
        xmlSuite = addXmlSuiteTests(xmlSuite);
        logger.info("\n" + xmlSuite.toXml());

        return xmlSuite;
    }

    public static XmlSuite setXmlSuiteGenerateInfo(XmlSuite suite) {
        suite.setName("Selenium Web UI Test");
        suite.setParallel(XmlSuite.ParallelMode.TESTS);
        suite.setPreserveOrder(false);
        suite.setThreadCount(Runtime.getRuntime().availableProcessors());
        suite.setVerbose(2);
        return suite;
    }

    public static XmlSuite addXmlSuiteListeners(XmlSuite suite) {
        List<String> listeners = TestNGListenerUtils.getTestNGListeners();
        if (listeners.size() != 0) {
            logger.info("TestNG Listeners found: {}", listeners);
            listeners.forEach(suite::addListener);
            return suite;
        }

        List<String> defaultListeners = TestNGListenerUtils.getDefaultTestNGListeners();
        logger.info("No TestNG Listener found, add default listeners: {}", defaultListeners);
        defaultListeners.forEach(suite::addListener);
        return suite;
    }

    public static XmlSuite addXmlSuiteTests(XmlSuite suite) {
        XmlTestBuilder.setXmlSuite(suite);
        XmlTestBuilder.build();
        return XmlTestBuilder.getXmlSuite();
    }

}
