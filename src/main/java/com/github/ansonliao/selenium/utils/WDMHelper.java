package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.exceptions.IllegalBrowserDriverName;
import com.google.common.collect.ImmutableMultiset;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.EdgeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.OperaDriverManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

public class WDMHelper {
    private static final Logger logger = LoggerFactory.getLogger(WDMHelper.class);
    private static boolean useTaobaoMirror = getConfigInstance().useTaobaoMirror();

    public static void downloadWebDriverBinary(String browserName) throws IllegalBrowserDriverName {
        String bn = browserName.toUpperCase();
        if (!browserList().contains(bn)) {
            throw new IllegalBrowserDriverName("Illegal webdriver browser name found: " + bn
                    + ", please provide the correct webdriver browser name: " + browserList());
        }

        switch (bn) {
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

    private static void downloadWebDriver(Platform platform, boolean taobaoMirror) throws IllegalBrowserDriverName {
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
                ChromeDriverManager.getInstance().setup();
                logger.info("Downloaded ChromeDriver from official mirror");
                break;
            case FIREFOX:
                FirefoxDriverManager.getInstance().setup();
                logger.info("Downloaded FirefoxDriver from official mirror");
                break;
            case EDGE:
                EdgeDriverManager.getInstance().setup();
                logger.info("Downloaded EdgeDriver from official mirror");
                break;
            case IE:
                InternetExplorerDriverManager.getInstance().setup();
                logger.info("Downloaded IEDriver from official mirror");
                break;
            case PHANTOMJS:
                PhantomJsDriverManager.getInstance().setup();
                logger.info("Downloaded PhantomJSDriver from official mirror");
                break;
            case OPERA:
                OperaDriverManager.getInstance().setup();
                logger.info("Downloaded OperaDriver from official mirror");
                break;
            default:
                ChromeDriverManager.getInstance().setup();
                logger.info("Downloaded ChromeDriver from official mirror");
                break;
        }
    }

    private static void downloadWDBinaryFromMirror(Platform platform) {
        switch (platform) {
            case CHROME:
                ChromeDriverManager.getInstance().useMirror().setup();
                logger.info("Downloaded ChromeDriver from mirror (Taobao)");
                break;
            case FIREFOX:
                FirefoxDriverManager.getInstance().useMirror().setup();
                logger.info("Downloaded FirefoxDriver from mirror (Taobao)");
                break;
            case PHANTOMJS:
                PhantomJsDriverManager.getInstance().useMirror().setup();
                logger.info("Downloaded PhantomJSDriver from mirror (Taobao)");
                break;
            case EDGE:
                EdgeDriverManager.getInstance().useMirror().setup();
                logger.info("Downloaded EdgeDriver from mirror (Taobao)");
                break;
            case IE:
                InternetExplorerDriverManager.getInstance().useMirror().setup();
                logger.info("Downloaded IEDriver from mirror (Taobao)");
                break;
            case OPERA:
                OperaDriverManager.getInstance().useMirror().setup();
                logger.info("Downloaded OperaDriver from mirror (Taobao)");
            default:
                ChromeDriverManager.getInstance().useMirror().setup();
                logger.info("Downloaded ChromeDriver from mirror (Taobao)");
                break;
        }
    }

    private enum Platform {
        CHROME, FIREFOX, PHANTOMJS, EDGE, IE, OPERA
    }

    private static List<String> browserList() {
        return ImmutableMultiset.of("CHROME", "FIREFOX", "EDGE", "INTERNETEXPLORER", "PHANTOMJS", "OPERA").asList();
    }

}
