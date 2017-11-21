package com.github.ansonliao.selenium.testng;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.factory.WebDriverManager;
import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.internal.ScreenshotManager;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.IOException;


public class TestResultListener extends TestListenerAdapter {

    private static Logger logger = LoggerFactory.getLogger(TestListenerAdapter.class);

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        Class<?> clazz = getTestRealClass(iTestResult);
        ITestNGMethod method = getTestMethod(iTestResult);
        String browserName = getTestBrowser(iTestResult);

        ScreenshotManager screenshotManager = new ScreenshotManager();
        String imgPrefix = screenshotManager.capture(clazz, method.getMethodName(), browserName);
        ExtentTestManager.getExtentTest().fail(iTestResult.getThrowable());

        try {
            ExtentTestManager.getExtentTest().fail(
                    "Screenshot: ",
                    MediaEntityBuilder.createScreenCaptureFromPath(imgPrefix).build());
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Test fail: {} - {} - {}, screenshot - {}",
                clazz.getName(), method.getMethodName(), browserName, imgPrefix);

        ExtentTestManager.getExtentTest().log(Status.FAIL, "Test Failed");
        WebDriverManager.getDriver().quit();
        ExtentTestManager.extentReport.flush();
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        logger.info("Test skipped: {} - {} - {}",
                getTestRealClass(iTestResult),
                getTestMethod(iTestResult),
                getTestBrowser(iTestResult));
        ExtentTestManager.getExtentTest().log(Status.SKIP, "Test Skipped");
        WebDriverManager.getDriver().quit();
        ExtentTestManager.extentReport.flush();
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        logger.info("Test succeeded: {} - {} - {}",
                getTestRealClass(iTestResult),
                getTestMethod(iTestResult),
                getTestBrowser(iTestResult));
        ExtentTestManager.getExtentTest().log(Status.PASS, "Test Passed");
        WebDriverManager.getDriver().quit();
        ExtentTestManager.extentReport.flush();
    }

    @Override
    public void onFinish(ITestContext iTestContext) {

    }

    private Class<?> getTestRealClass(ITestResult iTestResult) {
        return iTestResult.getTestClass().getRealClass();
    }

    private ITestNGMethod getTestMethod(ITestResult iTestResult) {
        return iTestResult.getMethod();
    }

    private String getTestBrowser(ITestResult iTestResult) {
        return iTestResult.getTestContext().getCurrentXmlTest()
                .getAllParameters().get(Constants.TESTNG_XML_BROWSER_PARAMETER_KEY);
    }
}
