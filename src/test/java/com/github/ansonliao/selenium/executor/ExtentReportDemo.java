package com.github.ansonliao.selenium.executor;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class ExtentReportDemo {

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> parentTest = new ThreadLocal();
    private static ThreadLocal<ExtentTest> childTest = new ThreadLocal();
    private static ThreadLocal<ExtentTest> grandTest = new ThreadLocal();
    private static int counter = 1;

    @BeforeSuite
    public void beforeSuite() {
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("extentreport.html");
        htmlReporter.config().setTestViewChartLocation(ChartLocation.BOTTOM);
        htmlReporter.config().setChartVisibilityOnOpen(true);
        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setDocumentTitle("Demo Report");
        htmlReporter.config().setEncoding("utf-8");
        htmlReporter.config().setReportName("Demo Report");

        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    }

    @BeforeClass
    public void beforeClass() {
        ExtentTest parent = extent.createTest(getClass().getName());
        parentTest.set(parent);
    }

    @BeforeMethod
    public void beforeMethod(Method method) {
        ExtentTest child = parentTest.get().createNode(method.getName());
        childTest.set(child);
        ExtentTest grand = childTest.get().createNode("Chrome " + counter);
        grandTest.set(grand);
    }

    @Test
    public void f1() {
        grandTest.get().log(Status.PASS, "Test Passed");
    }

    @Test
    public void f2() {
        grandTest.get().log(Status.PASS, "Test Passed");
    }

    @AfterMethod
    public void afterMethod() {
        extent.flush();
        ++counter;
    }



}
