package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.utils.SEConfig;
import com.google.common.base.Strings;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
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
        if (!SEConfig.isKeyExisted(exportParameter)
                || Strings.isNullOrEmpty(SEConfig.getString(exportParameter))) {
            logger.info("IEDriver export parameter was not found, {}: {}",
                    getExportParameterKey(), exportParameter);
            InternetExplorerDriverManager.getInstance().setup();

            // sometimes the webdriver binary download completed,
            // but export parameter was not set,
            // set it in case
            exportDriver(getExportParameterKey(),
                    InternetExplorerDriverManager.getInstance().getBinaryPath());
        }
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
