package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.annotations.Edge;
import com.github.ansonliao.selenium.annotations.IgnoreFirefox;
import com.github.ansonliao.selenium.annotations.Incognito;
import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.factory.ChromeFactory;
import com.github.ansonliao.selenium.factory.FirefoxFactory;
import com.github.ansonliao.selenium.internal.platform.Browser;
import com.github.ansonliao.selenium.internal.platform.Platform;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import java.lang.reflect.Method;

/**
 * Created by ansonliao on 17/2/2017.
 */

public class SeleniumParallel {
    public WebDriver driver;
    protected String browserName;
    private boolean isIncognito;
    protected String url;

//    private WebDriver driver;
//
//    public SeleniumParallel(WebDriver driver) {
//        this.driver = driver;
//    }
//
//    public WebDriver getDriver() {
//        return driver;
//    }

    @BeforeClass
    @Parameters({"browser"})
    public void beforeClass(String browser) {
        browserName = browser;
    }

    public boolean isIncognito(Method method) {
        isIncognito = method.isAnnotationPresent(Incognito.class) ? true : false;
        return isIncognito;
    }

    public String findUrl(Method method) {
        if (method.isAnnotationPresent(URL.class)) {
            url = method.getAnnotation(URL.class).value();
        } else if (method.getDeclaringClass().isAnnotationPresent(URL.class)) {
            url = method.getDeclaringClass().getAnnotation(URL.class).value();
        } else {
            url = null;
        }

        return url;

    }

    public String getUrl() {
        return url;
    }

    public WebDriver startWebDriver(Method method) {
        Browser browserType = parseBrowserType();
        isIncognito(method);
        switch (browserType) {
            case CHROME:
                driver = isIncognito ? new ChromeFactory().getInstance() : new ChromeFactory().getIncognitoInstance();
                break;
            case FIREFOX:
                driver = isIncognito ? new FirefoxFactory().getInstance() : new FirefoxFactory().getIncognitoInstance();
                break;
            case Edge:
                // add code here
                break;
            case InternetExplorer:
                // add code here
                break;
            default:
                driver = isIncognito ? new ChromeFactory().getInstance() : new ChromeFactory().getIncognitoInstance();
                break;
        }
        return driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    private Browser parseBrowserType() {
        if (browserName.equalsIgnoreCase("CHROME")) {
            return Browser.CHROME;
        } else if (browserName.equalsIgnoreCase("FIREFOX")) {
            return Browser.FIREFOX;
        } else if (browserName.equalsIgnoreCase("EDGE")) {
            return Browser.Edge;
        } else {
            return Browser.InternetExplorer;
        }
    }

    public WebDriver openUrl(String url) {
        getDriver().get(url);
        return getDriver();
    }

    @Edge
    @IgnoreFirefox
    public static void main(String[] args) {
        SeleniumParallel parallel = new SeleniumParallel();
//        System.out.println(parallel.getBrowsers());

    }
}
