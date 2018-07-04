package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

public class EdgeFactory extends DriverManager {

    private static final Logger logger =
            LoggerFactory.getLogger(EdgeFactory.class);
    private static EdgeFactory instance = new EdgeFactory();

    private EdgeFactory() {
        super();
    }

    public synchronized static EdgeFactory getInstance() {
        return instance;
    }

    @Override
    public WebDriver getDriver() {
        driver = new EdgeDriver();
        return driver;
    }

    @Override
    protected WebDriver buildRemoteWebDriver() {
        return null;
    }

    @Override
    public String getExportParameterKey() {
        return getConfigInstance().edgeDriverProperty();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
