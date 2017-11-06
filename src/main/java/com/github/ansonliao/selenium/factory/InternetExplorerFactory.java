package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternetExplorerFactory extends DriverManager {
    private static final Logger logger =
            LoggerFactory.getLogger(InternetExplorerFactory.class);
    private static InternetExplorerFactory instance = new InternetExplorerFactory();

    private InternetExplorerFactory() {
        super();
    }

    public synchronized static InternetExplorerFactory getInstance() {
        return instance;
    }

    @Override
    public WebDriver getDriver() {
        driver = new InternetExplorerDriver();
        return driver;
    }

    @Override
    public String getExportParameterKey() {
        return "webdriver.ie.driver";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
