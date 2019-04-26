package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.testng.util.Strings;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static com.github.ansonliao.selenium.utils.StringUtils.removeQuoteMark;
import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

public abstract class DriverManager {

    protected static final String SELENIUM_HUB_URL = retrieveSeleniumHubUrl();
    protected static final String CAPS_JSON_FILE = "caps/caps.json";
    public WebDriver driver;
    public String exportParameter = getExportParameterKey();
    // protected static JsonElement capsJsonElement = getCaps();
    public Logger logger = getLogger();
    protected boolean isHeadless = false;
    protected boolean isIncognito = false;

    public static String retrieveSeleniumHubUrl() {
        String url = removeQuoteMark(getConfigInstance().seleniumHubUrl());
        if (Strings.isNullOrEmpty(url)) {
            return "";
        }

        if (!url.endsWith("wd/hub")) {
            return url.endsWith("/") ? url + "wd/hub" : url + "/wd/hub";
        }
        return url;
    }

    public abstract WebDriver getDriver();

    public WebDriver getDriver(DesiredCapabilities capabilities, String seleniumHubUrl) {
        RemoteWebDriver remoteWebDriver = null;
        try {
            logger.info("Create RemoteWebDriver instance with Selenium Hub URL: {}",
                    seleniumHubUrl);
            remoteWebDriver = new RemoteWebDriver(new URL(seleniumHubUrl), capabilities);
        } catch (MalformedURLException e) {
            logger.error("Malformed URL found: {}", SELENIUM_HUB_URL);
            e.printStackTrace();
        }
        return remoteWebDriver;
    }

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

    public void exportDriver(String key, String value) {
        logger.info("Export {} as {}", key, value);
        System.setProperty(key, value);
    }

    // protected static BufferedReader getJsonReader() {
    //     BufferedReader reader;
    //     try {
    //         reader = new BufferedReader(new FileReader(CAPS_JSON_FILE));
    //     } catch (FileNotFoundException e) {
    //         reader = null;
    //     }
    //     return reader;
    // }

    protected String getTimezone() {
        DateFormat dbFormatter = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm aa zzzz");
        return dbFormatter.getTimeZone().getID();
    }

}
