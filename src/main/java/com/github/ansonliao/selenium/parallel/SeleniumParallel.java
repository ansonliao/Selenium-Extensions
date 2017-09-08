package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.annotations.*;
import com.github.ansonliao.selenium.factory.ChromeFactory;
import com.github.ansonliao.selenium.factory.DriverManager;
import com.github.ansonliao.selenium.factory.DriverManagerFactory;
import com.github.ansonliao.selenium.factory.FirefoxFactory;
import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.internal.platform.Browser;
import com.github.ansonliao.selenium.utils.BrowserUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Optional;

import static com.github.ansonliao.selenium.internal.platform.Browser.CHROME;

/**
 * Created by ansonliao on 17/2/2017.
 */

public class SeleniumParallel {
    protected static Logger logger = Logger.getLogger(SeleniumParallel.class);

    private WebDriver driver;
    protected DriverManager driverManager;
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

//    @BeforeClass
//    @Parameters({"browser"})
//    public void beforeClass(String browser) {
//        browserName = browser;
//    }
//
//    public boolean isIncognito(Method method) {
//        isIncognito = method.isAnnotationPresent(Incognito.class) ? true : false;
//        return isIncognito;
//    }

    @BeforeClass
    public void beforeClass(ITestClass iTestClass) {
        browserName = iTestClass.getXmlClass().getAllParameters().get("browser").toString();
        driverManager = DriverManagerFactory.getManager(BrowserUtils.getBrowserByString(Optional.of(browserName)));
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
        driverManager.isIncognito = method.isAnnotationPresent(Incognito.class) ? true : false;
        driverManager.isHeadless = method.isAnnotationPresent(Headless.class) ? true : false;
        driver = driverManager.getDriver();
        return driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebDriver openUrl(String url) {
        getDriver().get(url);
        return getDriver();
    }

    protected void takeScreenShot(String filePath) throws IOException {
        File scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);

        FileUtils.copyFile(scrFile, new File(
                filePath + Constants.FILE_SEPARATOR
                        + new Timestamp(System.currentTimeMillis()).getTime() + ".jpeg"));
    }

    @Edge
    @IgnoreFirefox
    public static void main(String[] args) {
        SeleniumParallel parallel = new SeleniumParallel();
//        System.out.println(parallel.getBrowsers());

    }
}
