package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.internal.platform.Platform;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ansonliao on 16/2/2017.
 */
public class InternetExplorerFactory implements IWebDriver {
    private static Logger logger = Logger.getLogger(InternetExplorerFactory.class);
    private static Map<Long, WebDriver> ieDriverMap = new HashMap<>();

    @Override
    public WebDriver getInstance() {
        long threadId = Thread.currentThread().getId();
        if (!ieDriverMap.containsKey(threadId)) {
            WebDriver driver = new ChromeDriver(initChromeCapabilities());
            ieDriverMap.put(threadId, driver);
        }
        return ieDriverMap.get(threadId);
    }

    private ChromeOptions initChromeCapabilities() {
        if (Platform.getOSType().equals(Platform.OSType.WINDOWS)) {
            System.setProperty("webdriver.chrome.driver", "chromedriver");
        } else {
            System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        }
        DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("incognito");
        return options;
    }
}
