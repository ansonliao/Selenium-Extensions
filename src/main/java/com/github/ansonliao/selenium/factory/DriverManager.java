package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.utils.SEConfig;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.testng.util.Strings;

public abstract class DriverManager {

    protected boolean isHeadless = false;
    protected boolean isIncognito = false;
    protected static final String SELENIUM_HUB_URL = retrieveSeleniumHubUrl();
    public WebDriver driver;
    public String exportParameter = getExportParameterKey();
    public Logger logger = getLogger();

    public abstract WebDriver getDriver();

    protected abstract WebDriver buildRemoteWebDriver();

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

    public static String retrieveSeleniumHubUrl() {
        String url = SEConfig.getString(Constants.SELENIUM_HUB_URL);
        if (Strings.isNullOrEmpty(url)) {
            return "";
        }

        if (!url.endsWith("wd/hub")) {
            return url.endsWith("/") ? url + "wd/hub" : url + "/wd/hub";
        }
        return url;
    }

    public void exportDriver(String key, String value) {
        logger.info("Export {} as {}", key, value);
        System.setProperty(key, value);
    }

    protected String getTimezone() {
        DateFormat dbFormatter = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm aa zzzz");
        return dbFormatter.getTimeZone().getID();
    }

}
