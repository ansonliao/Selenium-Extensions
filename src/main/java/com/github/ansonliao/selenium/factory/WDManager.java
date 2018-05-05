package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;

public class WDManager {

    private static ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<>();

    public synchronized static void setDriver(WebDriver driver) {
        webDriverThreadLocal.set(driver);
    }

    public synchronized static WebDriver getDriver() {
        return webDriverThreadLocal.get();
    }

    public synchronized static void quitDriver() {
        getDriver().quit();
    }

    public synchronized static void removeDriver() {
        webDriverThreadLocal.remove();
    }

    public synchronized static void quitAndClearDriver() {
        quitDriver();
        removeDriver();
    }
}
