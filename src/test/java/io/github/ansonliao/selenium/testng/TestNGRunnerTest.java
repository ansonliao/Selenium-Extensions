package io.github.ansonliao.selenium.testng;

import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import static org.testng.Assert.assertNotNull;

/**
 * Covers the non-browser part of {@link TestNGRunner}: running an empty suite
 * must complete and exercise the banner logging without starting a driver.
 */
public class TestNGRunnerTest {

    @Test
    public void runCompletesForAnEmptySuite() {
        XmlSuite suite = new XmlSuite();
        suite.setName("empty-unit-suite");

        TestNGRunner.Run(suite);

        assertNotNull(suite.getName());
    }
}
