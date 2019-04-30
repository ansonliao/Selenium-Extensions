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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.github.ansonliao.selenium.utils.CapsUtils.getCaps;
import static com.github.ansonliao.selenium.utils.CapsUtils.getCliArgs;
import static com.github.ansonliao.selenium.utils.CapsUtils.getExtensions;
import static com.github.ansonliao.selenium.utils.PlatformUtils.getPlatform;
import static com.github.ansonliao.selenium.utils.StringUtils.removeQuoteMark;
import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.remote.BrowserType.CHROME;

/**
 * 1.
 * Chromedriver Capabilities & ChromeOptions:
 * http://chromedriver.chromium.org/capabilities
 * https://stackoverflow.com/a/46786163
 * 2.
 * About set the mobile emulator, please refer to:
 * http://chromedriver.chromium.org/mobile-emulation
 * 3.
 * ChromeDriver options argument list can refer to:
 * http://peter.sh/experiments/chromium-command-line-switches/
 * https://chromium.googlesource.com/chromium/src/+/master/chrome/common/chrome_switches.cc
 * https://chromium.googlesource.com/chromium/src/+/master/chrome/common/pref_names.cc
 *
 */
public class ChromeFactory extends DriverManager {

    private static final Logger logger = LoggerFactory.getLogger(ChromeFactory.class);
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
        List<Object> extensions = getExtensions(CHROME);
        Map<String, Object> caps = getCaps(CHROME);
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
        if (!caps.containsKey("platform")) {
            caps.put("platform", getPlatform().toString());
        }
        options.addArguments(argList.parallelStream()
                .map(String::valueOf)
                .map(String::trim)
                .map(String::toLowerCase)
                .distinct().collect(toList()));
        extensions.parallelStream()
                .map(String::valueOf)
                .map(File::new)
                .forEach(options::addExtensions);
        // options.setExperimentalOption("prefs", caps);
        // caps.forEach(options::setCapability);
        caps.forEach((k, v) -> options.setCapability(k, v));
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        driver = Strings.isNullOrEmpty(SELENIUM_HUB_URL)
                ? new ChromeDriver(options)
                : getDriver(capabilities, SELENIUM_HUB_URL);
        return driver;
    }

    @Override
    protected WebDriver buildRemoteWebDriver() {
        return null;
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
