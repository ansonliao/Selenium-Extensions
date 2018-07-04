package com.github.ansonliao.selenium.parallel;

import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.annotations.Description;
import com.github.ansonliao.selenium.annotations.Headless;
import com.github.ansonliao.selenium.annotations.Incognito;
import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.factory.DriverManager;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    protected static Logger logger =
            LoggerFactory.getLogger(SeleniumParallel.class);
    protected static JavaProjectBuilder javaProjectBuilder =
            new JavaProjectBuilder();
    private static final String FILE_SEPARATOR = File.separator;
    private static final String PROJECT_ROOT_DIR = System.getProperty("user.dir");
    private static final String SCREENSHOT_DIR = PROJECT_ROOT_DIR
            .concat(FILE_SEPARATOR)
            .concat("target")
            .concat(FILE_SEPARATOR)
            .concat("screenshots");

    private WebDriver driver;
    protected DriverManager driverManager;
    protected String browserName;
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
        driverManager.setIncognito(method.isAnnotationPresent(Incognito.class) ? true : false);
        driverManager.setHeadless(method.isAnnotationPresent(Headless.class) ? true : false);
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
                withBoldHTML("Test Start"));
        ExtentTestManager.getExtentTest().log(Status.INFO,
                withBoldHTML("Open URL: ") + url);
        return driver;
    }

    protected String takeScreenShot(String imgPrefix) throws IOException {
        File scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);

        String destDir = SCREENSHOT_DIR
                .concat(FILE_SEPARATOR)
                .concat(this.getClass().getPackage().getName())
                .concat(FILE_SEPARATOR)
                .concat(this.getClass().getSimpleName())
                .concat(FILE_SEPARATOR)
                .concat(this.browserName)
                .concat(FILE_SEPARATOR)
                .concat(imgPrefix)
                .concat("_")
                .concat(String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()))
                .concat(".jpeg");
        MyFileUtils.copyFile(scrFile, new File(destDir));
        return destDir.replace(
                PROJECT_ROOT_DIR
                        .concat(FILE_SEPARATOR)
                        .concat("target")
                        .concat(FILE_SEPARATOR), "");
    }

    public List<String> getAuthors(String className, Method method) {
        logger.info("Get authors of Class = " + className);

        javaProjectBuilder.addSourceTree(new File(
                PROJECT_ROOT_DIR
                        .concat(FILE_SEPARATOR)
                        .concat("src")));
        JavaClass cls = javaProjectBuilder.getClassByName(className);
        List<DocletTag> authors = cls.getTags().stream()
                .filter(tag -> tag.getName().equals("author") || tag.getName().equals("author:"))
                .collect(Collectors.toList());
        logger.info("Class: {}, authors: {} ", className, authors.toString());

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
        List<JavaMethod> methods = cls.getMethods();
        logger.info("Get authors of method = " + methods.toString());
        JavaMethod mth = null;
        for (JavaMethod m : methods) {
            if (m.getName().equalsIgnoreCase(method.getName())) {
                mth = m;
                break;
            }
        }

        authors = mth.getTags().stream()
                .filter(tag -> tag.getName().equals("author") || tag.getName().equals("author:"))
                .collect(Collectors.toList());
        if (authors.size() != 0) {
            allAuthors.clear();
            for (DocletTag author : authors) {
                if (author.getValue().trim().length() > 0) {
                    allAuthors.add(author.getValue().trim());
                }
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

    public String withBoldHTML(String s) {
        return !s.trim().isEmpty()
                ? "<b>" + s + "</b>"
                : "";
    }

}
