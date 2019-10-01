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
import org.testng.annotations.Test;
import org.testng.util.Strings;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class ExtentTestManager {
    private static final Logger sfl4jLogger = LoggerFactory.getLogger(ExtentTestManager.class);
    public static ThreadLocal<ExtentTest> extentTests = new ThreadLocal<>();
    public static ExtentReports extentReport = ExtentManager.getExtentReports();
    public static Map<String, ExtentTest> parentTests = Maps.newHashMap();
    public static Map<String, ExtentTest> childTests = Maps.newHashMap();
    public static ThreadLocal<ExtentTest> grandTests = new ThreadLocal<>();
    private static ExtentTest extentTest;

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
        String description = "N/A";
        Class clazz = method.getDeclaringClass();
        String className = clazz.getName();

        if (!parentTests.containsKey(className)) {
            if (clazz.isAnnotationPresent(Test.class)) {
                Test var1 = (Test) clazz.getAnnotation(Test.class);
                if (Strings.isNotNullAndNotEmpty(var1.description().trim())) {
                    description = var1.description().trim();
                }
            } else if (clazz.isAnnotationPresent(Description.class)) {
                Description var2 = (Description) clazz.getAnnotation(Description.class);
                if (Strings.isNotNullAndNotEmpty(var2.value().trim())) {
                    description = var2.value().trim();
                }
            }

            sfl4jLogger.info("Create ExtentReport test node: {} - {}, description: {}",
                    className, browserName, description);
            test = extentReport.createTest(className, description);
            parentTests.put(className, test);
        }

        String childNodeKey = String.join(".", clazz.getName(), method.getName());
        if (!childTests.containsKey(childNodeKey)) {
            // The first priority source of description is @Test, then @Description, the last is the class level
            if (method.isAnnotationPresent(Test.class)) {
                if (Strings.isNotNullAndNotEmpty(method.getAnnotation(Test.class).description().trim())) {
                    description = method.getAnnotation(Test.class).description().trim();
                } else {
                    if (method.isAnnotationPresent(Description.class)) {
                        if (Strings.isNotNullAndNotEmpty(method.getAnnotation(Description.class).value().trim())) {
                            description = method.getAnnotation(Description.class).value().trim();
                        }
                    }
                }
            }

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
            extTestNodeName = extTestNodeName.concat("Params: ").concat(paramStr);
            // sfl4jLogger.info("Create ExtentReport test node: {}", extTestNodeName);
        }

        sfl4jLogger.info("Create ExtentReport test node: {}.{} - {}, description: {}",
                className,
                Strings.isNotNullAndNotEmpty(extTestNodeName)
                        ? String.format("%s[%s]", method.getName(), extTestNodeName)
                        : method.getName(),
                browserName, description);
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
