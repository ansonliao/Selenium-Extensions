package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;


public abstract class DriverManager {

    public boolean isHeadless = false;
    public boolean isIncognito = false;

    protected WebDriver driver;

    protected abstract void startService();

    protected abstract void stopService();

    protected abstract void createService();

    protected abstract String getDriverPath();

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public WebDriver getDriver() {
        if (driver == null) {
            startService();
            createService();
        }

        return driver;
    }

    public void setHandless(boolean isHeadless) {
        this.isHeadless = isHeadless;
    }

    public void setIncognito(boolean isIncognito) {
        this.isIncognito = isIncognito;
    }

    public boolean getIsHeadless() {
        return isHeadless;
    }

    public boolean getIsIncognito() {
        return isIncognito;
    }
}
