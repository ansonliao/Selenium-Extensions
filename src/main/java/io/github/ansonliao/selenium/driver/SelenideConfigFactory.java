package io.github.ansonliao.selenium.driver;

import com.codeborne.selenide.SelenideConfig;
import io.github.ansonliao.selenium.annotations.Headless;
import io.github.ansonliao.selenium.annotations.Incognito;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.github.ansonliao.selenium.utils.CapsUtils.getCaps;
import static io.github.ansonliao.selenium.utils.CapsUtils.getCliArgs;
import static io.github.ansonliao.selenium.utils.CapsUtils.getExtensions;
import static io.github.ansonliao.selenium.utils.StringUtils.isNotNullAndNotEmpty;
import static io.github.ansonliao.selenium.utils.StringUtils.isNullOrEmpty;
import static io.github.ansonliao.selenium.utils.StringUtils.removeQuoteMark;
import static io.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

/**
 * Builds an independent {@link SelenideConfig} for a single test invocation.
 * Instance based configuration keeps parallel browser runs isolated.
 */
final class SelenideConfigFactory {

    static final String CHROME = "chrome";
    static final String FIREFOX = "firefox";
    static final String EDGE = "edge";
    static final String IE = "ie";

    private static final Logger logger =
            LoggerFactory.getLogger(SelenideConfigFactory.class);

    private SelenideConfigFactory() {
    }

    static SelenideConfig create(String browserName, Method method) {
        String resolvedBrowser = isNullOrEmpty(browserName)
                ? getConfigInstance().defaultBrowser()
                : browserName;
        String browser = normalizeBrowser(resolvedBrowser);
        boolean headless = method.isAnnotationPresent(Headless.class)
                || method.getDeclaringClass().isAnnotationPresent(Headless.class);
        boolean incognito = method.isAnnotationPresent(Incognito.class)
                || method.getDeclaringClass().isAnnotationPresent(Incognito.class);

        if (IE.equals(browser) && (headless || incognito)) {
            logger.warn("{} does not support headless/incognito mode; ignoring",
                    browserName);
        }

        SelenideConfig config = new SelenideConfig()
                .browser(browser)
                .headless(headless && !IE.equals(browser))
                .screenshots(false)
                .savePageSource(false)
                .reportsFolder("target/screenshots");

        String hubUrl = normalizeHubUrl(
                removeQuoteMark(getConfigInstance().seleniumHubUrl()));
        if (isNotNullAndNotEmpty(hubUrl)) {
            config.remote(hubUrl);
        }

        config.browserCapabilities(buildCapabilities(
                resolvedBrowser.toUpperCase(Locale.ROOT), browser, incognito));
        return config;
    }

    private static MutableCapabilities buildCapabilities(
            String browserKey, String browser, boolean incognito) {
        List<Object> args = new ArrayList<>(getCliArgs(browserKey));
        Map<String, Object> caps = getCaps(browserKey);

        if (incognito) {
            String argument = incognitoArgument(browser);
            if (argument != null) {
                args.add(argument);
            }
        }

        List<String> cliArgs = args.stream()
                .map(String::valueOf)
                .map(String::trim)
                .filter(arg -> !arg.isEmpty())
                .distinct()
                .toList();

        MutableCapabilities options;
        switch (browser) {
            case CHROME:
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments(cliArgs);
                extensionsAsFiles(browserKey).forEach(chromeOptions::addExtensions);
                options = chromeOptions;
                break;
            case FIREFOX:
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments(cliArgs);
                options = firefoxOptions;
                break;
            case EDGE:
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments(cliArgs);
                options = edgeOptions;
                break;
            default:
                options = new InternetExplorerOptions();
                break;
        }
        caps.forEach(options::setCapability);
        return options;
    }

    private static List<File> extensionsAsFiles(String browserKey) {
        return getExtensions(browserKey).stream()
                .map(String::valueOf)
                .map(File::new)
                .toList();
    }

    private static String incognitoArgument(String browser) {
        switch (browser) {
            case CHROME:
                return "--incognito";
            case FIREFOX:
                return "-private";
            case EDGE:
                return "--inprivate";
            default:
                return null;
        }
    }

    static String normalizeBrowser(String browserName) {
        if (isNullOrEmpty(browserName)) {
            throw new IllegalArgumentException("Browser name must not be empty");
        }
        switch (browserName.trim().toUpperCase(Locale.ROOT)) {
            case "CHROME":
                return CHROME;
            case "FIREFOX":
                return FIREFOX;
            case "EDGE":
                return EDGE;
            case "IE":
            case "INTERNETEXPLORER":
            case "INTERNET EXPLORER":
                return IE;
            default:
                throw new IllegalArgumentException(
                        "No such browser webdriver: " + browserName);
        }
    }

    static String normalizeHubUrl(String url) {
        if (isNullOrEmpty(url)) {
            return "";
        }
        // Trim and drop a single trailing slash so a complete hub URL that ends
        // with one is not doubled (e.g. "http://host:4444/wd/hub/").
        String normalized = url.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.toLowerCase(Locale.ROOT).endsWith("wd/hub")) {
            return normalized;
        }
        return normalized + "/wd/hub";
    }
}
