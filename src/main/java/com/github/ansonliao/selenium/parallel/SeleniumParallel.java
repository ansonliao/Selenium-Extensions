package com.github.ansonliao.selenium.parallel;

import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.annotations.Description;
import com.github.ansonliao.selenium.annotations.Headless;
import com.github.ansonliao.selenium.annotations.Incognito;
import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.factory.DriverManager;
import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class SeleniumParallel {
    protected static Logger logger = Logger.getLogger(SeleniumParallel.class);

    private WebDriver driver;
    protected DriverManager driverManager;
    protected String browserName;
    private boolean isIncognito;
    protected String url;

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

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public WebDriver getDriver() {
        return this.driver;
    }

    public WebDriver openUrl(String url) {
        getDriver().get(url);
        ExtentTestManager.getExtentTest().log(
                Status.INFO,
                "Test Start ==> Open Url: " + url);
        return getDriver();
    }

    protected String takeScreenShot(String imgPrefix) throws IOException {
        File scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);

        String destDir = Constants.SCREENSHOT_DIR
                .concat(Constants.FILE_SEPARATOR)
                .concat(this.getClass().getPackage().getName())
                .concat(Constants.FILE_SEPARATOR)
                .concat(this.getClass().getSimpleName())
                .concat(Constants.FILE_SEPARATOR)
                .concat(this.browserName)
                .concat(Constants.FILE_SEPARATOR)
                .concat(imgPrefix)
                .concat("_")
                .concat(String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()))
                .concat(".jpeg");
        MyFileUtils.copyFile(scrFile, new File(destDir));
        return destDir.replace(
                Constants.PROJECT_ROOT_DIR
                        .concat(Constants.FILE_SEPARATOR)
                        .concat("target")
                        .concat(Constants.FILE_SEPARATOR), "");
    }

    public List<String> getAuthors(String className, Method method) {
        logger.info("className = " + className);

        JavaDocBuilder builder = new JavaDocBuilder();
        JavaClass cls = builder.getClassByName(className);
        List<DocletTag> authors = Arrays.asList(cls.getTagsByName("author"));
        logger.info("authors = " + authors.toString());

        // get class authors as default author name
        Set<String> allAuthors = new HashSet<>();
        if (authors.size() != 0) {
            for (DocletTag author : authors) {
                if (author.getValue().trim().length() > 0) {
                    allAuthors.add(author.getValue().trim());
                }
            }
        }


        // get method author
        List<JavaMethod> methods = Arrays.asList(cls.getMethods());
        logger.info("JavaMethod = " + methods.toString());
        JavaMethod mth = null;
        for (JavaMethod m : methods) {
            System.out.println("JavaMethod: " + m.getName());
            if (m.getName().equalsIgnoreCase(method.getName())) {
                mth = m;
                break;
            }
        }

        authors = Arrays.asList(mth.getTagsByName("author"));
        if (authors.size() != 0) {
            allAuthors.clear();
            for (DocletTag author : authors) {
                allAuthors.add(author.getValue().trim());
            }
        }

        return allAuthors.stream().collect(Collectors.toList());
    }

    public List<String> getTestGroups(Object object) {
        Set<String> allGroups = new HashSet<>();
        Test t;

        if (object instanceof Class) {
            Class clazz = (Class) object;
            t = (Test) clazz.getAnnotation(Test.class);
            if (t != null && t.groups().length > 0) {
                allGroups.addAll(Arrays.asList(t.groups()));
            }
            return allGroups.stream().collect(Collectors.toList());
        } else {
            Class clazz = ((Method) object).getDeclaringClass();
            t = (Test) clazz.getAnnotation(Test.class);
            if (t != null && t.groups().length > 0) {
                allGroups.addAll(Arrays.asList(t.groups()));
            }

            t = ((Method) object).getAnnotation(Test.class);
            if (t != null && t.groups().length > 0) {
                allGroups.addAll(Arrays.asList(t.groups()));
            }
            return allGroups.stream().collect(Collectors.toList());
        }
    }

    public String getDescription(Object object) {
        String description = null;
        if (object instanceof Class) {
            Class clazz = (Class) object;
            if (clazz.isAnnotationPresent(Description.class)) {
                return ((Description) clazz.getAnnotation(Description.class))
                        .value().trim();
            }
        }
        if (object instanceof Method) {
            Method method = (Method) object;
            if (method.isAnnotationPresent(Description.class)) {
                return method.getAnnotation(Description.class)
                        .value().trim();
            }
        }

        return description;
    }

}
