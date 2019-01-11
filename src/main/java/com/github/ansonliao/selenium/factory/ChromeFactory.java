package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
import static org.openqa.selenium.remote.BrowserType.CHROME;

/**
 * Chromedriver Capabilities & ChromeOptions:
 * http://chromedriver.chromium.org/capabilities
 */
public class ChromeFactory extends DriverManager {

    private static final Logger logger = LoggerFactory.getLogger(ChromeFactory.class);
    private static final String CAPS_PATH = "chrome." + DESIRED_CAPABILITIES_KEY;
    private static final String ARGS_PATH = "chrome." + CLI_ARGS_KEY;
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
        List<Object> argList = getCliArgs(CHROME);
        Map<String, Object> caps = getCaps(CHROME);
        // if (capsJsonElement != null) {
        //     if (isNodeExisted(capsJsonElement, ARGS_PATH)) {
        //         // retrieve chromedriver cli argument list
        //         argList = getGsonInstance().fromJson(
        //                 getJsonElement(capsJsonElement, ARGS_PATH).toString(), List.class);
        //     } else {
        //         logger.info(
        //                 "WebDriver Caps Json is not empty, but key : [{}] was not found in the caps json file.",
        //                 CLI_ARGS_KEY);
        //     }
        //     if (isNodeExisted(capsJsonElement, CAPS_PATH)) {
        //         // retrieve chromedriver desired capabilities
        //         caps = getGsonInstance().fromJson(
        //                 getJsonElement(capsJsonElement, CAPS_PATH).toString(), Map.class);
        //     } else {
        //         logger.info(
        //                 "WebDriver Caps Json is not empty, but key : [{}] was not found in the caps json file.",
        //                 CLI_ARGS_KEY);
        //     }
        // }
        if (isHeadless) {
            if (argList.parallelStream()
                    .filter(arg -> String.valueOf(arg).toLowerCase().contains("headless"))
                    .findFirst().get() == null) {
                argList.add("headless");
            }
        }
        if (isIncognito) {
            if (argList.parallelStream()
                    .filter(arg -> String.valueOf(arg).toLowerCase().contains("incognito"))
                    .findFirst().get() == null) {
                argList.add("incognito");
            }
        }
        // options.addArguments("--disable-gpu");
        // options.addArguments("--start-maximized");
        if (!caps.containsKey(CapabilityType.BROWSER_NAME)) {
            caps.put(CapabilityType.BROWSER_NAME, CHROME);
        }
        if (!caps.containsKey(CapabilityType.PLATFORM)) {
            caps.put(CapabilityType.PLATFORM, getPlatform().toString());
        }
        if (!caps.keySet().parallelStream().map(String::trim).map(String::toLowerCase)
                .collect(toList()).contains("tz")) {
            caps.put("tz", getTimezone());
        }
        argList.parallelStream()
                .map(String::valueOf)
                .map(String::trim)
                .map(String::toLowerCase)
                .distinct().collect(toList())
                .parallelStream()
                .forEach(options::addArguments);
        options.setExperimentalOption("prefs", caps);
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
            logger.info("Create RemoteWebDriver instance with Selenium Hub URL: {}",
                    SELENIUM_HUB_URL);
            remoteWebDriver = new RemoteWebDriver(new URL(SELENIUM_HUB_URL), capabilities);
        } catch (MalformedURLException e) {
            logger.error("Malformed URL found: {}", SELENIUM_HUB_URL);
            e.printStackTrace();
        }
        return remoteWebDriver;
    }

    @Override
    public String getExportParameterKey() {
        return removeQuoteMark(getConfigInstance().chromeDriverProperty());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
