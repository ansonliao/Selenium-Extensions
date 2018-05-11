package com.github.ansonliao.selenium.report.factory;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.annotations.Description;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Reporter;
import org.testng.util.Strings;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class ExtentTestManager {
    private static final Logger sfl4jLogger = LoggerFactory.getLogger(ExtentTestManager.class);
    public static ThreadLocal<ExtentTest> extentTests = new ThreadLocal<>();
    public static ExtentReports extentReport = ExtentManager.getExtentReports();
    private static ExtentTest extentTest;

    public static Map<String, ExtentTest> parentTests = Maps.newHashMap();
    public static Map<String, ExtentTest> childTests = Maps.newHashMap();
    public static ThreadLocal<ExtentTest> grandTests = new ThreadLocal<>();

    public synchronized static ExtentTest getExtentTest() {
        return grandTests.get();
    }

    public synchronized static ExtentTest createTest(
            String name, String description, String browserType) {
        extentTest = extentReport.createTest(name, description)
                .assignCategory(browserType);
        extentTests.set(extentTest);
        return extentTests.get();
    }

    public synchronized static ExtentTest createTest(String name, String description) {
        return createTest(name, description, String.valueOf(Thread.currentThread().getId()));
    }

    public synchronized static ExtentTest createTest(Method method, String browserName,
                                                     List<String> authors, List<String> groups, Object... parameters) {
        ExtentTest test;
        String description;
        Class clazz = method.getDeclaringClass();
        String className = clazz.getName();

        if (!parentTests.containsKey(className)) {
            description = clazz.isAnnotationPresent(Description.class)
                    ? ((Description) clazz.getAnnotation(Description.class)).value().trim()
                    : "";
            test = extentReport.createTest(className, description);
            parentTests.put(className, test);
        }

        String childNodeKey = String.join(".", clazz.getName(), method.getName());
        if (!childTests.containsKey(childNodeKey)) {
            description = method.isAnnotationPresent(Description.class)
                    ? (method.getAnnotation(Description.class)).value().trim()
                    : "";
            test = parentTests.get(className).createNode(method.getName(), description);
            if (authors != null && authors.size() > 0) {
                for (String author : authors) {
                    test.assignAuthor(author);
                }
            }
            childTests.put(childNodeKey, test);
        }

        String extTestNodeName = "";
        if (parameters != null && parameters.length != 0) {
            String paramStr = Lists.newArrayList(parameters).parallelStream()
                    .map(param ->
                            param instanceof String
                                    ? "\"".concat(param.toString()).concat("\"")
                                    : String.valueOf(param))
                    .collect(joining(", "));
            extTestNodeName = extTestNodeName.concat("Parames: ").concat(paramStr);
            sfl4jLogger.info("Create ExtentReport test node: {}", extTestNodeName);
        }

        sfl4jLogger.info("Create ExtentReport test node: {}.{} - {}, description: {}",
                className, method.getName(),
                browserName, Strings.isNullOrEmpty(extTestNodeName) ? "N/A" : extTestNodeName);
        test = childTests.get(childNodeKey).createNode(browserName, extTestNodeName);
        if (groups != null && groups.size() > 0) {
            for (String group : groups) {
                test.assignCategory(group);
            }
        }
        grandTests.set(test);

        return test;
    }

    public synchronized static ExtentTest createTest(String name) {
        return createTest(name, "Sample Test");
    }

    public synchronized static void logOutPut(String imgSrc, String headerName) {
        String imgPath = "<div class='col l4 m6 s12'>"
                + "<div class='card-panel'><h4 class='md-display-4'>"
                + headerName + "</h4><img src=" + imgSrc
                + " style=\"width:100%;height:100%;\"></div></div>";
        extentReport.setTestRunnerOutput(imgPath);
    }

    public synchronized static void logger(String message) {
        Reporter.log(message + "<br>", true);
        getExtentTest().log(Status.INFO, message + "<br>");
    }
}
