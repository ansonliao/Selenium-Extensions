package com.github.ansonliao.selenium.utils;

import com.google.common.collect.ImmutableMultiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;
import static io.github.bonigarcia.wdm.WebDriverManager.chromedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.edgedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver;
import static io.github.bonigarcia.wdm.WebDriverManager.iedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.operadriver;
import static io.github.bonigarcia.wdm.WebDriverManager.phantomjs;

public class WDMHelper {
    private static final Logger logger = LoggerFactory.getLogger(WDMHelper.class);
    private static final List<String> BROWSER_LIST =
            ImmutableMultiset.of("CHROME", "FIREFOX", "EDGE", "INTERNETEXPLORER", "PHANTOMJS", "OPERA").asList();
    private static boolean useTaobaoMirror = getConfigInstance().useTaobaoMirror();

    public static void downloadWebDriverBinary(String browserName) {
        if (!BROWSER_LIST.parallelStream().anyMatch(s -> s.equalsIgnoreCase(browserName))) {
            throw new IllegalArgumentException("Illegal webdriver browser name was found: " + browserName
                    + ", please provide the correct webdriver browser name: " + browserList());
        }

        switch (browserName.toUpperCase()) {
            case "CHROME":
                downloadWebDriver(Platform.CHROME, useTaobaoMirror);
                break;
            case "FIREFOX":
                downloadWebDriver(Platform.FIREFOX, useTaobaoMirror);
                break;
            case "PHANTOMJS":
                downloadWebDriver(Platform.PHANTOMJS, useTaobaoMirror);
                break;
            case "EDGE":
                downloadWebDriver(Platform.EDGE, useTaobaoMirror);
                break;
            case "INTERNETEXPLORER":
                downloadWebDriver(Platform.IE, useTaobaoMirror);
                break;
            case "OPERA":
                downloadWebDriver(Platform.OPERA, useTaobaoMirror);
                break;
            default:
                downloadWebDriver(Platform.CHROME, useTaobaoMirror);
        }
    }

    public static void exportDriver(String key, String value) {
        logger.info("Export {} = {}", key, value);
        System.setProperty(key, value);
    }

    private static void downloadWebDriver(Platform platform, boolean taobaoMirror) {
        if (taobaoMirror) {
            logger.info("Use TaoBao Mirror to download WebDriver binary.");
            downloadWDBinaryFromMirror(platform);
        } else {
            downloadWDBinary(platform);
        }
    }

    private static void downloadWDBinary(Platform platform) {
        switch (platform) {
            case CHROME:
                chromedriver().setup();
                logger.info("Downloaded ChromeDriver from official mirror");
                break;
            case FIREFOX:
                firefoxdriver().setup();
                logger.info("Downloaded FirefoxDriver from official mirror");
                break;
            case EDGE:
                edgedriver().setup();
                logger.info("Downloaded EdgeDriver from official mirror");
                break;
            case IE:
                iedriver().setup();
                logger.info("Downloaded IEDriver from official mirror");
                break;
            case PHANTOMJS:
                phantomjs().setup();
                logger.info("Downloaded PhantomJSDriver from official mirror");
                break;
            case OPERA:
                operadriver().setup();
                logger.info("Downloaded OperaDriver from official mirror");
                break;
            default:
                chromedriver().setup();
                logger.info("Downloaded ChromeDriver from official mirror");
                break;
        }
    }

    private static void downloadWDBinaryFromMirror(Platform platform) {
        switch (platform) {
            case CHROME:
                chromedriver().useMirror().setup();
                logger.info("Downloaded ChromeDriver from mirror (Taobao)");
                break;
            case FIREFOX:
                firefoxdriver().useMirror().setup();
                logger.info("Downloaded FirefoxDriver from mirror (Taobao)");
                break;
            case PHANTOMJS:
                phantomjs().useMirror().setup();
                logger.info("Downloaded PhantomJSDriver from mirror (Taobao)");
                break;
            case EDGE:
                edgedriver().useMirror().setup();
                logger.info("Downloaded EdgeDriver from mirror (Taobao)");
                break;
            case IE:
                iedriver().useMirror().setup();
                logger.info("Downloaded IEDriver from mirror (Taobao)");
                break;
            case OPERA:
                operadriver().useMirror().setup();
                logger.info("Downloaded OperaDriver from mirror (Taobao)");
                break;
            default:
                chromedriver().useMirror().setup();
                logger.info("Downloaded ChromeDriver from mirror (Taobao)");
                break;
        }
    }

    private static List<String> browserList() {
        return Arrays.asList("CHROME", "FIREFOX", "EDGE", "INTERNETEXPLORER", "PHANTOMJS", "OPERA");
    }

    private enum Platform {
        CHROME, FIREFOX, PHANTOMJS, EDGE, IE, OPERA
    }
}
