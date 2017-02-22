package com.github.ansonliao.selenium.report.factory;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.Reporter;

/**
 * Created by ansonliao on 17/2/2017.
 */
public class ExtentTestManager {
    public static ThreadLocal<ExtentTest> extentTests = new ThreadLocal<>();
    public static ExtentReports extentReport = ExtentManager.getExtentReports();
    private static ExtentTest extentTest;

    public synchronized static ExtentTest getExtentTest() {
        return extentTests.get();
    }

    public synchronized static ExtentTest createTest(String name, String description, String browserType) {
        extentTest = extentReport.createTest(name, description).assignCategory(browserType);
        extentTests.set(extentTest);
        return getExtentTest();
    }

    public synchronized static ExtentTest createTest(String name, String description) {
        return createTest(name, description, String.valueOf(Thread.currentThread().getId()));
    }

    public synchronized static ExtentTest createTest(String name) {
        return createTest(name, "Sample Ttest");
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
