package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.factory.InternetExplorerFactory;
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

    public synchronized static void downloadWebDriverBinary(String browserName) {
        String bn = browserName.toUpperCase();
        String parameterKey;

        switch (bn) {
            case "CHROME":
                ChromeDriverManager.getInstance().setup();
                //parameterKey = ChromeFactory.getInstance().getExportParameterKey();
                //WDMHelper.exportDriver(parameterKey, ChromeDriverManager.getInstance().getBinaryPath());
                break;
            case "FIREFOX":
                FirefoxDriverManager.getInstance().setup();
                //parameterKey = FirefoxFactory.getInstance().getExportParameterKey();
                //WDMHelper.exportDriver(parameterKey, FirefoxDriverManager.getInstance().getBinaryPath());
                break;
            case "PHANTOMJS":
                PhantomJsDriverManager.getInstance().setup();
                //parameterKey = PhantomJsFactory.getInstance().getExportParameterKey();
                //WDMHelper.exportDriver(parameterKey, PhantomJsDriverManager.getInstance().getBinaryPath());
                break;
            case "EDGE":
                EdgeDriverManager.getInstance().setup();
                //parameterKey = EdgeFactory.getInstance().getExportParameterKey();
                //WDMHelper.exportDriver(parameterKey, PhantomJsDriverManager.getInstance().getBinaryPath());
                break;
            case "INTERNETEXPLORER":
                InternetExplorerDriverManager.getInstance().setup();
                parameterKey = InternetExplorerFactory.getInstance().getExportParameterKey();
                //WDMHelper.exportDriver(parameterKey, InternetExplorerDriverManager.getInstance().getBinaryPath());
                break;
            case "OPERA":
                OperaDriverManager.getInstance().setup();
                //parameterKey = OperaFactory.getInstance().getExportParameterKey();
                //WDMHelper.exportDriver(parameterKey, OperaDriverManager.getInstance().getBinaryPath());
                break;
            default:
                ChromeDriverManager.getInstance().setup();
                //parameterKey = ChromeFactory.getInstance().getExportParameterKey();
                //WDMHelper.exportDriver(parameterKey, ChromeDriverManager.getInstance().getBinaryPath());
        }
    }

    public static void exportDriver(String key, String value) {
        logger.info("Export {} = {}", key, value);
        System.setProperty(key, value);
    }
}
