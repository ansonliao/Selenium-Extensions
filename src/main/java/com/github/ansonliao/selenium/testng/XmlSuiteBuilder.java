package com.github.ansonliao.selenium.testng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.xml.XmlSuite;

public class XmlSuiteBuilder {
    private static final Logger logger = LoggerFactory.getLogger(XmlSuiteBuilder.class);

    public static XmlSuite build() {
        XmlSuite xmlSuite = new XmlSuite();
        xmlSuite.setName("Selenium Web UI Test");
        xmlSuite.setParallel(XmlSuite.ParallelMode.TESTS);
        xmlSuite.setPreserveOrder(false);
        xmlSuite.setThreadCount(Runtime.getRuntime().availableProcessors());
        xmlSuite.setVerbose(2);

        XmlTestBuilder.setXmlSuite(xmlSuite);
        XmlTestBuilder.build();
        xmlSuite = XmlTestBuilder.getXmlSuite();
        logger.info("\n" + xmlSuite.toXml());

        return xmlSuite;
    }

}
