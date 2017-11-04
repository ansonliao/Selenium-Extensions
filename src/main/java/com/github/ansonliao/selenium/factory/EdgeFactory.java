package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.utils.SEConfig;
import com.google.common.base.Strings;
import io.github.bonigarcia.wdm.EdgeDriverManager;
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
        if (!SEConfig.isKeyExisted(exportParameter)
                || Strings.isNullOrEmpty(SEConfig.getString(exportParameter))) {
            logger.info("EdgeDriver export parameter was not found, {}: {}",
                    getExportParameterKey(), exportParameter);
            EdgeDriverManager.getInstance().setup();

            // sometimes the webdriver binary download completed,
            // but export parameter was not set,
            // set it in case
            exportDriver(getExportParameterKey(),
                    EdgeDriverManager.getInstance().getBinaryPath());
        }
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
        return "webdriver.edge.driver";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
