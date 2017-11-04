package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.utils.SEConfig;
import com.google.common.base.Strings;
import io.github.bonigarcia.wdm.OperaDriverManager;
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
        if (!SEConfig.isKeyExisted(exportParameter)
                || Strings.isNullOrEmpty(SEConfig.getString(exportParameter))) {
            logger.info("OperaDriver export parameter was not found, {}: {}",
                    getExportParameterKey(), exportParameter);
            OperaDriverManager.getInstance().setup();

            // sometimes the webdriver binary download completed,
            // but export parameter was not set,
            // set it in case
            exportDriver(getExportParameterKey(),
                    OperaDriverManager.getInstance().getBinaryPath());
        }
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
