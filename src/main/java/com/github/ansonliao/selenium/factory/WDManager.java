package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.driver.SelenideManager;
import org.openqa.selenium.WebDriver;

/**
 * Compatibility facade for raw Selenium access. Selenide owns the browser
 * lifecycle and creates one driver per thread.
 */
public class WDManager {

    public synchronized static WebDriver getDriver() {
        return SelenideManager.getWebDriver();
    }

    public synchronized static void quitAndClearDriver() {
        SelenideManager.closeDriver();
    }
}
