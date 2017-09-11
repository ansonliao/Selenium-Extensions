package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.annotations.Edge;
import com.github.ansonliao.selenium.annotations.Headless;
import com.github.ansonliao.selenium.annotations.IgnoreFirefox;
import com.github.ansonliao.selenium.annotations.Incognito;
import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.factory.DriverManager;
import com.github.ansonliao.selenium.factory.DriverManagerFactory;
import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.utils.BrowserUtils;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hamcrest.generator.qdox.JavaDocBuilder;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

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

    /**
    private WebDriver driver;

    public SeleniumParallel(WebDriver driver) {
        this.driver = driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    @BeforeClass
    @Parameters({"browser"})
    public void beforeClass(String browser) {
        browserName = browser;
    }

    public boolean isIncognito(Method method) {
        isIncognito = method.isAnnotationPresent(Incognito.class) ? true : false;
        return isIncognito;
    }
    */

    @BeforeClass
    public void beforeClass(ITestClass iTestClass) {
        browserName = iTestClass.getXmlClass().getAllParameters()
                .get("browser").toString();
        driverManager = DriverManagerFactory.getManager(
                BrowserUtils.getBrowserByString(Optional.of(browserName)));
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

    public String getAuthors(String className, ITestNGMethod method) {
        logger.info("className = " + className);

        JavaDocBuilder builder = new JavaDocBuilder();
        JavaClass cls = (JavaClass) builder.getClassByName(className);
        List<DocletTag> authors = cls.getTagsByName("author");
        logger.info("authors = " + authors.toString());

        // get class authors as default author name
        String allAuthors = "";
        if (authors.size() != 0) {
            for (DocletTag author : authors) {
                if (author.getValue().trim().length() > 0) {
                    allAuthors += author.getValue() + " ";
                }
            }
        }

        // get method author
        List<JavaMethod> methods = cls.getMethods();
        logger.info("JavaMethod = " + methods.toString());
        JavaMethod mth = methods
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase(method.getMethodName()))
                .findFirst()
                .get();

        authors = mth.getTagsByName("author");
        if (authors.size() != 0) {
            allAuthors = "";
            for (DocletTag author : authors) {
                allAuthors += author.getValue() + " ";
            }
        }

        return allAuthors.trim();
    }

    @Edge
    @IgnoreFirefox
    public static void main(String[] args) {
        SeleniumParallel parallel = new SeleniumParallel();
//        System.out.println(parallel.getBrowsers());

    }
}
