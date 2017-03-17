package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.internal.platform.Platform;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ansonliao on 16/2/2017.
 */
public class ChromeFactory implements IWebDriver {
    private static Logger logger = Logger.getLogger(ChromeFactory.class);

    private static Map<Long, WebDriver> chromeDriverMap = new HashMap<>();
    private ThreadLocal<WebDriver> chromeDrivers = new ThreadLocal<>();
    private boolean isIncognito = false;

    private static final String CHROME_DRIVER_PROPERTY = "webdriver.chrome.driver";

    @Override
    public WebDriver getInstance() {
        if (chromeDrivers.get() == null) {
            WebDriver driver = new ChromeDriver(initialCaps());
            chromeDrivers.set(driver);
        }

        return chromeDrivers.get();
    }

    public WebDriver getIncognitoInstance() {
        this.isIncognito = true;
        return getInstance();
    }

    public void setIncognito(boolean isIncognito) {
        this.isIncognito = isIncognito;
    }

    private DesiredCapabilities initialCaps() {
        if (!Platform.getOSType().equals(Platform.OSType.WINDOWS)) {
            System.setProperty(CHROME_DRIVER_PROPERTY, "chromedriver");
        } else {
            System.setProperty(CHROME_DRIVER_PROPERTY, "chromedriver.exe");
        }
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        ChromeOptions options = new ChromeOptions();
        if (isIncognito) {
            options.addArguments("incognito");
        }
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        return capabilities;
    }

}
