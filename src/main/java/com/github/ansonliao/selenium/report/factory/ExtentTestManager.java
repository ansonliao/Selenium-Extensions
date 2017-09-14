package com.github.ansonliao.selenium.report.factory;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.annotations.Description;
import org.testng.Reporter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExtentTestManager {
    public static ThreadLocal<ExtentTest> extentTests = new ThreadLocal<>();
    public static ExtentReports extentReport = ExtentManager.getExtentReports();
    private static ExtentTest extentTest;

    public static Map<String, ExtentTest> parentTests = new HashMap<>();
    public static Map<String, ExtentTest> childTests = new HashMap<>();
    public static ThreadLocal<ExtentTest> grandTests = new ThreadLocal<>();

    public synchronized static ExtentTest getExtentTest() {
        return grandTests.get();
    }

    public synchronized static ExtentTest createTest(
            String name, String description, String browserType) {
        extentTest = extentReport.createTest(name, description).assignCategory(browserType);
        extentTests.set(extentTest);
        return getExtentTest();
    }


    public synchronized static ExtentTest createTest(String name, String description) {
        return createTest(name, description, String.valueOf(Thread.currentThread().getId()));
    }

    public synchronized static ExtentTest createTest(Method method, String browserName,
                                                     List<String> authors, List<String> groups) {
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

        if (!childTests.containsKey(method.getName())) {
            description = method.isAnnotationPresent(Description.class)
                    ? (method.getAnnotation(Description.class)).value().trim()
                    : "";
            test = parentTests.get(className).createNode(method.getName(), description);
            if (authors != null && authors.size() > 0) {
                for (String author : authors) {
                    test.assignAuthor(author);
                }
            }
            if (groups != null && groups.size() > 0) {
                for (String group : groups) {
                    test.assignCategory(group);
                }
            }
            childTests.put(method.getName(), test);
        }

        test = childTests.get(method.getName()).createNode(browserName);
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
