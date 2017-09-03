package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.internal.platform.Platform;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ansonliao on 16/2/2017.
 */
public class FirefoxFactory implements IWebDriver {
    private static Logger logger = Logger.getLogger(FirefoxFactory.class);
    private static Map<Long, WebDriver> firefoxDriverMap = new HashMap<>();
    private ThreadLocal<WebDriver> firefoxDrivers = new ThreadLocal<>();
    private boolean isIncognito = false;

    private static final String FIREFOX_INCOGNITO_PROPERTY = "browser.private.browsing.autostart";
    private static final String FIREFOX_DRIVER_PROPERTY = "webdriver.gecko.driver";

    @Override
    public WebDriver getInstance() {
        if (firefoxDrivers.get() == null) {
            WebDriver driver = new FirefoxDriver(initialCaps());
            firefoxDrivers.set(driver);
        }
        return firefoxDrivers.get();
    }

    public WebDriver getIncognitoInstance() {
        isIncognito = true;
        return getInstance();
    }

    public void setIncognito(boolean isIncognito) {
        this.isIncognito = isIncognito;
    }

    private FirefoxProfile initialCaps() {
        FirefoxProfile firefoxProfile = new FirefoxProfile();
        switch (Platform.getOSType()) {
            case MAC:
                System.setProperty(
                        FIREFOX_DRIVER_PROPERTY,
                        Constants.FIREFOX_DRIVER_64BIT_MAC_PATH);
                break;
            case UNIX:
                System.setProperty(
                        FIREFOX_DRIVER_PROPERTY,
                        Constants.FIREFOX_DRIVER_64BIT_LINUX_PATH);
                break;
            case WINDOWS:
                System.setProperty(
                        FIREFOX_DRIVER_PROPERTY,
                        Constants.FIREFOX_DRIVER_64BIT_WINDOWS_PATH);
            default:
                System.setProperty(
                        FIREFOX_DRIVER_PROPERTY,
                        Constants.FIREFOX_DRIVER_64BIT_MAC_PATH);
                break;
        }
        firefoxProfile.setPreference(FIREFOX_INCOGNITO_PROPERTY, isIncognito);
        return firefoxProfile;
    }
}
