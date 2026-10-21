package io.github.ansonliao.selenium.report;

import com.codeborne.selenide.logevents.SelenideLogger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class SelenideAllureStepsTest {

    @BeforeMethod
    public void cleanUp() {
        SelenideAllureSteps.uninstall();
    }

    @AfterMethod
    public void tearDown() {
        SelenideAllureSteps.uninstall();
    }

    @Test
    public void shouldInstallListenerOnlyOnce() {
        SelenideAllureSteps.install();
        SelenideAllureSteps.install();
        assertTrue(SelenideLogger.hasListener(SelenideAllureSteps.LISTENER_NAME));
    }

    @Test
    public void shouldUninstallListener() {
        SelenideAllureSteps.install();
        SelenideAllureSteps.uninstall();
        assertFalse(SelenideLogger.hasListener(SelenideAllureSteps.LISTENER_NAME));
    }
}
