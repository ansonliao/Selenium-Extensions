package com.github.ansonliao.selenium.driver;

import com.codeborne.selenide.SelenideDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class SelenideManagerTest {

    @AfterMethod
    public void cleanup() {
        SelenideManager.closeDriver();
    }

    @Test
    public void noDriverBeforeCreate() {
        SelenideManager.closeDriver();
        assertFalse(SelenideManager.hasDriver());
    }

    @Test
    public void getSelenideDriverThrowsWhenUnbound() {
        SelenideManager.closeDriver();
        assertThrows(IllegalStateException.class, SelenideManager::getSelenideDriver);
    }

    @Test
    public void createBindsDriverToThread() throws Exception {
        Method method = Fixture.class.getDeclaredMethod("plain");
        SelenideDriver driver = SelenideManager.createDriver("chrome", method);
        assertNotNull(driver);
        assertTrue(SelenideManager.hasDriver());
        assertNotNull(SelenideManager.getSelenideDriver());
    }

    @Test
    public void closeRemovesDriver() throws Exception {
        Method method = Fixture.class.getDeclaredMethod("plain");
        SelenideManager.createDriver("chrome", method);
        SelenideManager.closeDriver();
        assertFalse(SelenideManager.hasDriver());
    }

    @Test
    public void driversAreIsolatedPerThread() throws Exception {
        Method method = Fixture.class.getDeclaredMethod("plain");
        SelenideDriver mainDriver = SelenideManager.createDriver("chrome", method);
        try {
            final SelenideDriver[] otherDriver = new SelenideDriver[1];
            Thread worker = new Thread(() -> {
                try {
                    otherDriver[0] = SelenideManager.createDriver("firefox", method);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    SelenideManager.closeDriver();
                }
            });
            worker.start();
            worker.join();

            assertNotNull(otherDriver[0]);
            assertNotSame(otherDriver[0], mainDriver);
        } finally {
            SelenideManager.closeDriver();
        }
    }

    static class Fixture {
        @Test
        public void plain() {
        }
    }
}
