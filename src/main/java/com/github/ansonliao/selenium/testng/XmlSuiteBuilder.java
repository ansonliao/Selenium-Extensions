package com.github.ansonliao.selenium.testng;

import org.apache.log4j.Logger;
import org.testng.xml.XmlSuite;

public class XmlSuiteBuilder {
    private static final Logger logger = Logger.getLogger(XmlSuiteBuilder.class);

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
        logger.info(xmlSuite.toXml());

        return xmlSuite;
    }

    public static void printXmlSuite(XmlSuite xmlSuite) {
        System.out.println(xmlSuite.toXml());
    }

}
