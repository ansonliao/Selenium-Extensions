package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.utils.SEConfig;
import com.google.common.base.Strings;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhantomJsFactory extends DriverManager {
    private static final Logger logger =
            LoggerFactory.getLogger(PhantomJsFactory.class);
    private static PhantomJsFactory instance = new PhantomJsFactory();

    private PhantomJsFactory() {
        super();
        if (!SEConfig.isKeyExisted(exportParameter)
                || Strings.isNullOrEmpty(SEConfig.getString(exportParameter))) {
            logger.info("PhantomJsDriver export parameter was not found, {}: {}",
                    getExportParameterKey(), exportParameter);
            PhantomJsDriverManager.getInstance().setup();

            // sometimes the webdriver binary download completed,
            // but export parameter was not set,
            // set it in case
            exportDriver(getExportParameterKey(),
                    PhantomJsDriverManager.getInstance().getBinaryPath());
        }
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
    public String getExportParameterKey() {
        return "phantomjs.binary.path";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
