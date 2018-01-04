package com.github.ansonliao.selenium.testng;

import com.github.ansonliao.selenium.utils.TestNGListenerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.xml.XmlSuite;

import java.util.List;

public class XmlSuiteBuilder {
    private static final Logger logger = LoggerFactory.getLogger(XmlSuiteBuilder.class);

    public static XmlSuite build() {
        XmlSuite xmlSuite = new XmlSuite();
        xmlSuite.setName("Selenium Web UI Test");
        xmlSuite.setParallel(XmlSuite.ParallelMode.TESTS);
        xmlSuite.setPreserveOrder(false);
        xmlSuite.setThreadCount(Runtime.getRuntime().availableProcessors());
        xmlSuite.setVerbose(2);

        // listeners
        List<String> listeners = TestNGListenerUtils.getDefaultTestNGListners();
        if (listeners.size() != 0) {
            logger.info("TestNG Listeners found: {}", listeners);
            listeners.forEach(xmlSuite::addListener);
        } else {
            List<String> defaultListeners = TestNGListenerUtils.getDefaultTestNGListners();
            logger.info("No TestNG Listener found, add default listeners: ", defaultListeners);
            defaultListeners.forEach(xmlSuite::addListener);
        }

        XmlTestBuilder.setXmlSuite(xmlSuite);
        XmlTestBuilder.build();
        xmlSuite = XmlTestBuilder.getXmlSuite();
        logger.info("\n" + xmlSuite.toXml());

        return xmlSuite;
    }

}
