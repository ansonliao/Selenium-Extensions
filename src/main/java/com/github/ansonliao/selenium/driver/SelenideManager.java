package com.github.ansonliao.selenium.driver;

import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.Method;

/**
 * Holds one {@link SelenideDriver} per thread. Selenide owns the browser
 * lifecycle; this class only binds the instance to the executing TestNG thread.
 */
public final class SelenideManager {

    private static final ThreadLocal<SelenideDriver> DRIVER = new ThreadLocal<>();

    private SelenideManager() {
    }

    public static SelenideDriver createDriver(String browserName, Method method) {
        closeDriver();
        SelenideConfig config = SelenideConfigFactory.create(browserName, method);
        SelenideDriver driver = new SelenideDriver(config);
        DRIVER.set(driver);
        return driver;
    }

    public static SelenideDriver getSelenideDriver() {
        SelenideDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "No Selenide driver is bound to the current thread");
        }
        return driver;
    }

    /**
     * Returns the underlying Selenium driver, or {@code null} when no browser
     * has been started on this thread yet.
     */
    public static WebDriver getWebDriver() {
        SelenideDriver driver = DRIVER.get();
        return driver == null ? null : driver.getWebDriver();
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static boolean hasStartedDriver() {
        SelenideDriver driver = DRIVER.get();
        return driver != null && driver.hasWebDriverStarted();
    }

    public static void closeDriver() {
        SelenideDriver driver = DRIVER.get();
        if (driver == null) {
            return;
        }
        try {
            if (driver.hasWebDriverStarted()) {
                driver.close();
            }
        } finally {
            DRIVER.remove();
        }
    }
}
