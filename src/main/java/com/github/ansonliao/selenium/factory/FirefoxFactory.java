package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.utils.SEConfig;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.util.Strings;

public class FirefoxFactory extends DriverManager {
    private static final Logger logger =
            LoggerFactory.getLogger(FirefoxFactory.class);
    private static FirefoxFactory instance = new FirefoxFactory();

    private FirefoxFactory() {
        super();
        if (!SEConfig.isKeyExisted(exportParameter)
                || Strings.isNullOrEmpty(SEConfig.getString(exportParameter))) {
            logger.info("FirefoxDriver export parameter was not found, {}: {}",
                    getExportParameterKey(), exportParameter);
            FirefoxDriverManager.getInstance().setup();

            // sometimes the webdriver binary download completed,
            // but export parameter was not set,
            // set it in case
            exportDriver(getExportParameterKey(),
                    FirefoxDriverManager.getInstance().getBinaryPath());
        }
    }

    public synchronized static FirefoxFactory getInstance() {
        return instance;
    }

    @Override
    public WebDriver getDriver() {
        FirefoxBinary binary = new FirefoxBinary();
        FirefoxOptions options = new FirefoxOptions();
        if (isHeadless) {
            binary.addCommandLineOptions("--headless");
            options.setBinary(binary);
        }
        if (isIncognito) {
            options.addArguments("--private");
        }

        driver = new FirefoxDriver(options);
        return driver;
    }

    @Override
    public String getExportParameterKey() {
        return "webdriver.gecko.driver";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
