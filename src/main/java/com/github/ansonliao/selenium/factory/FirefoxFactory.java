package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirefoxFactory extends DriverManager {
    private static final Logger logger =
            LoggerFactory.getLogger(FirefoxFactory.class);
    private static FirefoxFactory instance = new FirefoxFactory();

    private FirefoxFactory() {
        super();
    }

    public synchronized static FirefoxFactory getInstance() {
        return instance;
    }

    @Override
    public WebDriver getDriver() {
        FirefoxBinary binary = new FirefoxBinary();
        FirefoxOptions options = new FirefoxOptions();
        if (isHeadless) {
            binary.addCommandLineOptions("--headless");
            options.setBinary(binary);
        }
        if (isIncognito) {
            options.addArguments("--private");
        }

        driver = new FirefoxDriver(options);
        return driver;
    }

    @Override
    public String getExportParameterKey() {
        return "webdriver.gecko.driver";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
