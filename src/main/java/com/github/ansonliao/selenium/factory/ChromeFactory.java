package com.github.ansonliao.selenium.factory;

import static com.github.ansonliao.selenium.utils.PlatformUtils.getPlatform;
import static org.openqa.selenium.remote.BrowserType.CHROME;

import com.github.ansonliao.selenium.utils.SEFilterUtils;
import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.util.Strings;

public class ChromeFactory extends DriverManager {

    private static final Logger logger =
            LoggerFactory.getLogger(ChromeFactory.class);
    private static ChromeFactory instance = new ChromeFactory();
    private ChromeOptions options = new ChromeOptions();

    private ChromeFactory() {
        super();
    }

    public synchronized static ChromeFactory getInstance() {
        return instance;
    }

    @Override
    public WebDriver getDriver() {
        if (isHeadless) {
            options.addArguments("--headless");
        }
        if (isIncognito) {
            options.addArguments("--incognito");
        }
        options.addArguments("--disable-gpu");
        options.addArguments("--start-maximized");

        driver = Strings.isNullOrEmpty(SELENIUM_HUB_URL)
                ? new ChromeDriver(options)
                : buildRemoteWebDriver();
        return driver;
    }

    @Override
    protected WebDriver buildRemoteWebDriver() {
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        capabilities.setCapability(CapabilityType.BROWSER_NAME, CHROME);
        capabilities.setCapability(CapabilityType.PLATFORM, getPlatform());
        capabilities.setCapability("tz", getTimezone());
        RemoteWebDriver remoteWebDriver = null;
        try {
            logger.info("Create RemoteWebDriver instance with Selenium Hub URL: {}", SELENIUM_HUB_URL);
            remoteWebDriver = new RemoteWebDriver(new URL(SELENIUM_HUB_URL), capabilities);
        } catch (MalformedURLException e) {
            logger.error("Malformed URL found: {}", SELENIUM_HUB_URL);
            e.printStackTrace();
        }
        return remoteWebDriver;
    }

    @Override
    public String getExportParameterKey() {
        return SEFilterUtils.getChromeDriverExportKey();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
