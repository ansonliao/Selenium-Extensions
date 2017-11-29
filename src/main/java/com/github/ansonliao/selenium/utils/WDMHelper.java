package com.github.ansonliao.selenium.utils;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.EdgeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.OperaDriverManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WDMHelper {
    private static final Logger logger = LoggerFactory.getLogger(WDMHelper.class);
    private static boolean useTaobaoMirror;

    static {
        useTaobaoMirror = SEConfig.getBoolean("wd.useTaobaoMirror");
    }

    public static void downloadWebDriverBinary(String browserName) {
        String bn = browserName.toUpperCase();

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

    private static void downloadWebDriver(Platform platform, boolean taobaoMirror) {
        if (taobaoMirror) {
            logger.info("Use TaoBao Mirror to download WebDriver binary");
            downloadWDBinaryFromTB(platform);
        } else {
            downloadWDBinary(platform);
        }
    }

    private static void downloadWDBinary(Platform platform) {
        switch (platform) {
            case CHROME:
                ChromeDriverManager.getInstance().setup();
                break;
            case FIREFOX:
                FirefoxDriverManager.getInstance().setup();
                break;
            case PHANTOMJS:
                PhantomJsDriverManager.getInstance().setup();
                break;
            case EDGE:
                EdgeDriverManager.getInstance().setup();
                break;
            case IE:
                InternetExplorerDriverManager.getInstance().setup();
                break;
            case OPERA:
                OperaDriverManager.getInstance().setup();
                break;
            default:
                ChromeDriverManager.getInstance().setup();
                break;
        }
    }

    private static void downloadWDBinaryFromTB(Platform platform) {
        switch (platform) {
            case CHROME:
                ChromeDriverManager.getInstance()
                        .useTaobaoMirror().setup();
                break;
            case FIREFOX:
                FirefoxDriverManager.getInstance()
                        .useTaobaoMirror().setup();
                break;
            case PHANTOMJS:
                PhantomJsDriverManager.getInstance()
                        .useTaobaoMirror().setup();
                break;
            case EDGE:
                EdgeDriverManager.getInstance()
                        .useTaobaoMirror().setup();
                break;
            case IE:
                InternetExplorerDriverManager.getInstance()
                        .useTaobaoMirror().setup();
                break;
            case OPERA:
                OperaDriverManager.getInstance()
                        .useTaobaoMirror().setup();
                break;
            default:
                ChromeDriverManager.getInstance()
                        .useTaobaoMirror().setup();
                break;
        }
    }

    private enum Platform {
        CHROME, FIREFOX, PHANTOMJS, EDGE, IE, OPERA
    }
}
