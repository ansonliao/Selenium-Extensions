package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChromeFactory extends DriverManager {
    private static final Logger logger =
            LoggerFactory.getLogger(ChromeFactory.class);
    private static ChromeFactory instance = new ChromeFactory();

    private ChromeFactory() {
        super();
    }

    public synchronized static ChromeFactory getInstance() {
        return instance;
    }

    @Override
    public WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        if (isHeadless) {
            options.addArguments("--headless");
        }
        if (isIncognito) {
            options.addArguments("--incognito");
        }
        options.addArguments("--disable-gpu");

        driver = new ChromeDriver(options);
        return driver;
    }

    @Override
    public String getExportParameterKey() {
        return "webdriver.chrome.driver";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
