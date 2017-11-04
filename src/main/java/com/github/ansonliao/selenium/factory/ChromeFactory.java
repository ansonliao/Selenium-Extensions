package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.utils.SEConfig;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.util.Strings;

public class ChromeFactory extends DriverManager {
    private static final Logger logger =
            LoggerFactory.getLogger(ChromeFactory.class);
    private static ChromeFactory instance = new ChromeFactory();

    private ChromeFactory() {
        super();
        if (!SEConfig.isKeyExisted(exportParameter)
                || Strings.isNullOrEmpty(SEConfig.getString(exportParameter))) {
            logger.info("ChromeDriver export parameter was not found, {}: {}",
                    getExportParameterKey(), exportParameter);
            ChromeDriverManager.getInstance().setup();

            // sometimes the webdriver binary download completed,
            // but export parameter was not set,
            // set it in case
            exportDriver(getExportParameterKey(),
                    ChromeDriverManager.getInstance().getBinaryPath());
        }
    }

    public synchronized static ChromeFactory getInstance() {
        return instance;
    }

    @Override
    public WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        if (isHeadless) {
            options.addArguments("--headless");
        }
        if (isIncognito) {
            options.addArguments("--incognito");
        }
        options.addArguments("--disable-gpu");

        driver = new ChromeDriver(options);
        return driver;
    }

    @Override
    public String getExportParameterKey() {
        return "webdriver.chrome.driver";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
