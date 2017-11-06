package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperaFactory extends DriverManager {
    private static final Logger logger =
            LoggerFactory.getLogger(OperaFactory.class);
    private static OperaFactory instance = new OperaFactory();

    private OperaFactory() {
        super();
    }

    public synchronized static OperaFactory getInstance() {
        return instance;
    }

    @Override
    public WebDriver getDriver() {
        driver = new OperaDriver();
        return driver;
    }

    @Override
    public String getExportParameterKey() {
        return "webdriver.opera.driver";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
