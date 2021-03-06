package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.util.Strings;

import java.util.List;
import java.util.Map;

import static com.github.ansonliao.selenium.utils.CapsUtils.getCaps;
import static com.github.ansonliao.selenium.utils.CapsUtils.getCliArgs;
import static com.github.ansonliao.selenium.utils.StringUtils.removeQuoteMark;
import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.remote.BrowserType.FIREFOX;

/**
 * Selenium WebDriver desired capabilities:
 * https://github.com/SeleniumHQ/selenium/wiki/DesiredCapabilities
 * https://developer.mozilla.org/en-US/docs/Mozilla/Firefox/Headless_mode
 */
public class FirefoxFactory extends DriverManager {

    private static final Logger logger =
            LoggerFactory.getLogger(FirefoxFactory.class);
    private static FirefoxFactory instance = new FirefoxFactory();
    private FirefoxOptions options = new FirefoxOptions();

    private FirefoxFactory() {
        super();
    }

    public synchronized static FirefoxFactory getInstance() {
        return instance;
    }

    @Override
    public WebDriver getDriver() {
        List<Object> argList = getCliArgs(FIREFOX);
        Map<String, Object> caps = getCaps(FIREFOX);
        if (isHeadless) {
            if (argList.parallelStream().filter(arg -> String.valueOf(arg).toLowerCase().contains("headless"))
                    .map(String::valueOf).anyMatch(s -> Strings.isNotNullAndNotEmpty(s))) {
                argList.add("--headless");
            }
        }
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        caps.forEach((k, v) -> capabilities.setCapability(k, v));
        caps.forEach((k, v) -> options.setCapability(k, v));
        options.addArguments(argList.parallelStream()
                .map(String::valueOf)
                .map(String::trim)
                .map(String::toLowerCase)
                .distinct().collect(toList()));
        driver = Strings.isNullOrEmpty(SELENIUM_HUB_URL)
                ? new FirefoxDriver(options)
                : getDriver(capabilities, SELENIUM_HUB_URL);
        return driver;
    }

    @Override
    protected WebDriver buildRemoteWebDriver() {
        return null;
    }

    @Override
    public String getExportParameterKey() {
        return removeQuoteMark(getConfigInstance().firefoxDriverProperty());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
