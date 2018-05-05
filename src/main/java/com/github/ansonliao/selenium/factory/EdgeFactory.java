package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.utils.SEFilterUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public String getExportParameterKey() {
        return SEFilterUtils.getEdgeDriverExportKey();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
