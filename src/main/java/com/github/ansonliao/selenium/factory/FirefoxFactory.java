package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.util.Strings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.github.ansonliao.selenium.utils.CapsUtils.CLI_ARGS_KEY;
import static com.github.ansonliao.selenium.utils.CapsUtils.DESIRED_CAPABILITIES_KEY;
import static com.github.ansonliao.selenium.utils.CapsUtils.getCaps;
import static com.github.ansonliao.selenium.utils.CapsUtils.getCliArgs;
import static com.github.ansonliao.selenium.utils.PlatformUtils.getPlatform;
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
    private static final String CAPS_PATH = "firefox." + DESIRED_CAPABILITIES_KEY;
    private static final String ARGS_PATH = "firefox." + CLI_ARGS_KEY;
    private FirefoxBinary binary = new FirefoxBinary();
    private FirefoxOptions options = new FirefoxOptions();
    private static FirefoxFactory instance = new FirefoxFactory();

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
        // if (capsJsonElement != null) {
        //     if (!isNodeExisted(capsJsonElement, ARGS_PATH)) {
        //         argList = getGsonInstance().fromJson(
        //                 getJsonElement(capsJsonElement, ARGS_PATH).toString(), List.class);
        //     } else {
        //         logger.info(
        //                 "WebDriver Caps Json is not empty, but key : [{}] was not found in the caps json file.",
        //                 CLI_ARGS_KEY);
        //     }
        // }
        // if (isNodeExisted(capsJsonElement, CAPS_PATH)) {
        //     caps = getGsonInstance().fromJson(
        //             getJsonElement(capsJsonElement, CAPS_PATH).toString(), Map.class);
        // } else {
        //     logger.info(
        //             "WebDriver Caps Json is not empty, but key : [{}] was not found in the caps json file.",
        //             CLI_ARGS_KEY);
        // }

        // if (isHeadless) {
        //     binary.addCommandLineOptions("--headless");
        //     options.setBinary(binary);
        // }
        // if (isIncognito) {
        //     options.addArguments("--private");
        // }
        if (isHeadless) {
            if (argList.parallelStream().filter(arg -> String.valueOf(arg).toLowerCase().contains("headless"))
                    .findFirst().get() == null)
                argList.add("--headless");
        }
        argList.parallelStream()
                .map(String::valueOf)
                .map(String::trim)
                .map(String::toLowerCase)
                .distinct().collect(toList())
                .parallelStream()
                .forEach(binary::addCommandLineOptions);
        options.setBinary(binary);
        // argList.parallelStream()
        //         .map(String::valueOf)
        //         .map(String::trim)
        //         .map(String::toLowerCase)
        //         .distinct().collect(toList())
        //         .parallelStream()
        //         .forEach(options::addArguments);

        // options.addArguments("", "");

        driver = Strings.isNullOrEmpty(SELENIUM_HUB_URL)
                ? new FirefoxDriver(options)
                : buildRemoteWebDriver();
        return driver;
    }

    @Override
    protected WebDriver buildRemoteWebDriver() {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, capabilities);
        capabilities.setCapability(CapabilityType.BROWSER_NAME, FIREFOX);
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
        return removeQuoteMark(getConfigInstance().firefoxDriverProperty());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
