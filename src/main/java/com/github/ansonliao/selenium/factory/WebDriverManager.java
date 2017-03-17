package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.internal.platform.Platform;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ansonliao on 16/2/2017.
 */
public class WebDriverManager {
    private static Logger logger = Logger.getLogger(WebDriverManager.class);
    private static Map<Long, WebDriver> webDriverMap = new HashMap<>();

    public static class getInstance {
        public synchronized WebDriver ChromeInstance() {
            long threadId = getThreadId();
            if (!webDriverMap.containsKey(threadId)) {
                WebDriver driver = new ChromeDriver(initChromeCapabilities());
                webDriverMap.put(threadId, driver);
            }
            return webDriverMap.get(threadId);
        }

        public synchronized WebDriver FireFoxInstance() {
            long threadId = getThreadId();
            if (!webDriverMap.containsKey(threadId)) {
                WebDriver driver = new FirefoxDriver();
                webDriverMap.put(threadId, driver);
            }
            return webDriverMap.get(threadId);
        }

        private ChromeOptions initChromeCapabilities() {
            if (!Platform.getOSType().equals(Platform.OSType.WINDOWS)) {
                System.setProperty("webdriver.chrome.driver", "chromedriver");
            } else {
                System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
            }
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("incognito");
            return options;
        }

        private FirefoxOptions initFirefoxCapabilities() {
            if (!Platform.getOSType().equals(Platform.OSType.WINDOWS)) {
                System.setProperty("webdriver.firefox.marionette", "geckodriver");
            } else {
                System.setProperty("webdriver.firefox.marionette", "geckodriver.exe");
            }
            DesiredCapabilities capabilities = DesiredCapabilities.firefox();
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("incognito");
            return options;
        }

//        private Platform.OSType getOsType() {
//            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
//                return Platform.OSType.MAC;
//            } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
//                return Platform.OSType.LINUX;
//            } else {
//                return Platform.OSType.WINDOWS;
//            }
//        }

        private synchronized long getThreadId() {
            return Thread.currentThread().getId();
        }
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("os.name").toLowerCase());
    }

}
