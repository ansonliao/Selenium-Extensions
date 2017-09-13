package com.github.ansonliao.selenium.report.factory;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.Reporter;

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
        return extentTests.get();
    }

    public synchronized static ExtentTest getChildExtentTest(String key) {
        return childTests.get(key);
    }

    public synchronized static ExtentTest getGrandTest() {
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

    public synchronized static ExtentTest createTest(String name) {
        return createTest(name, "Sample Test");
    }

    /**
     *
     * @param name: java class full name included package name
     * @return
     */
    public synchronized static ExtentTest createParentTest(String name, String description) {
        ExtentTest test = null;
        if (!parentTests.containsKey(name)) {
            if (description != null && description.length() > 0) {
                test = extentReport.createTest(name, description);
            } else {
                test = extentReport.createTest(name);
            }
            parentTests.put(name, test);
        }
        return parentTests.get(name);
    }

    /**
     *
     * @param name: test method name of java class, without class name, and package name
     * @param description
     * @param groups
     * @param authors
     * @return
     */
    public synchronized static ExtentTest createChildTest(String parentKey,
            String name, String description, List<String> groups, List<String> authors) {
        if (!parentTests.containsKey(parentKey)) {
            ExtentTest parentTest = parentTests.get(parentKey);
            ExtentTest test = parentTest.createNode(name, description);
            if (groups != null && groups.size() > 0) {
                groups.stream().filter(group -> group.trim().length() > 0)
                        .forEach(group -> test.assignCategory(group));
            }
            if (authors != null && authors.size() > 0) {
                authors.stream().filter(author -> author.trim().length() > 0)
                        .forEach(author -> test.assignAuthor(author));
            }

            childTests.put(name, test);
        }

        return childTests.get(name);
    }

    /**
     *
     * @param parentKey
     * @param name: browser name
     * @param description
     * @param groups
     * @param authors
     * @return
     */
    public synchronized static ExtentTest createGrandTest(String parentKey,
            String name, String description, List<String> groups, List<String> authors) {
        ExtentTest parentTest = childTests.get(parentKey);
        ExtentTest test = parentTest.createNode(name, description);
        if (groups != null && groups.size() > 0) {
            groups.stream().filter(group -> group.trim().length() > 0)
                    .forEach(group -> test.assignCategory(group));
        }
        if (authors != null && authors.size() > 0) {
            authors.stream().filter(author -> author.trim().length() > 0)
                    .forEach(author -> test.assignAuthor(author));
        }

        grandTests.set(test);
        return test;
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
