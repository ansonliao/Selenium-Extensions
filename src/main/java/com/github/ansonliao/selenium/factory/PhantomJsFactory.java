package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

public class PhantomJsFactory extends DriverManager {

    private static final Logger logger =
            LoggerFactory.getLogger(PhantomJsFactory.class);
    private static PhantomJsFactory instance = new PhantomJsFactory();

    private PhantomJsFactory() {
        super();
    }

    public synchronized static PhantomJsFactory getInstance() {
        return instance;
    }

    @Override
    public WebDriver getDriver() {
        driver = new PhantomJSDriver();
        return driver;
    }

    @Override
    protected WebDriver buildRemoteWebDriver() {
        return null;
    }

    @Override
    public String getExportParameterKey() {
        return getConfigInstance().phantomjsDriverProperty();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
