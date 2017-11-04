package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

public abstract class DriverManager {
    protected boolean isHeadless = false;
    protected boolean isIncognito = false;
    public WebDriver driver;
    public String exportParameter = getExportParameterKey();
    public Logger logger = getLogger();

    public abstract WebDriver getDriver();

    public abstract String getExportParameterKey();

    public abstract Logger getLogger();

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    public void setHeadless(boolean isHeadless) {
        this.isHeadless = isHeadless;
    }

    public void setIncognito(boolean isIncognito) {
        this.isIncognito = isIncognito;
    }

    public void exportDriver(String key, String value) {
        logger.info("Export {} as {}", key, value);
        System.setProperty(key, value);
    }
}
