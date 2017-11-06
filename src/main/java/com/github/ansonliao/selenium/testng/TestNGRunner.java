package com.github.ansonliao.selenium.testng;

import com.github.lalyos.jfiglet.FigletFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;

import java.io.IOException;

import static java.util.Arrays.asList;

public class TestNGRunner {
    private static final Logger logger = LoggerFactory.getLogger(TestNGRunner.class);

    public static void Run() {
        XmlSuite xmlSuite = XmlSuiteBuilder.build();
        TestNGRunner.Run(xmlSuite);
    }

    public static void Run(XmlSuite xmlSuite) {
        TestNG testNG = new TestNG();
        testNG.setXmlSuites(asList(xmlSuite));

        logFigletFont("Test Start");
        testNG.run();

        logFigletFont("Test Completed");
    }

    private static void logFigletFont(String value) {
        try {
            logger.info("\n" + FigletFont.convertOneLine(value));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
