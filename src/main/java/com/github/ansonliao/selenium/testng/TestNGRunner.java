package com.github.ansonliao.selenium.testng;

import org.apache.log4j.Logger;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;

import static java.util.Arrays.asList;

public class TestNGRunner {
    private static final Logger logger = Logger.getLogger(TestNGRunner.class);

    public static void Run() {
        XmlSuite xmlSuite = XmlSuiteBuilder.build();
        TestNG testNG = new TestNG();
        testNG.setXmlSuites(asList(xmlSuite));
        testNG.run();
    }

    public static void Run(XmlSuite xmlSuite) {
        TestNG testNG = new TestNG();
        testNG.setXmlSuites(asList(xmlSuite));
        testNG.run();
    }
}
