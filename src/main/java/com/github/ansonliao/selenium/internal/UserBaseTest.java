package com.github.ansonliao.selenium.internal;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.factory.DriverManagerFactory;
import com.github.ansonliao.selenium.parallel.SeleniumParallel;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import com.github.ansonliao.selenium.utils.BrowserUtils;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.testng.ITestResult.FAILURE;
import static org.testng.ITestResult.SKIP;
import static org.testng.ITestResult.SUCCESS;


public class UserBaseTest extends SeleniumParallel {
    protected ExtentTest extentTest;

    @BeforeClass
    public void beforeClass(ITestContext iTestContext) {
        browserName = iTestContext.getCurrentXmlTest().getAllParameters()
                .get("browser").toString().trim();
        driverManager = DriverManagerFactory.getManager(
                BrowserUtils.getBrowserByString(Optional.of(browserName)));
        MyFileUtils.createScreenshotFolderForBrowser(this.getClass(), browserName);
    }

    @BeforeMethod
    public void beforeMethod(Method method) {
        url = findUrl(method);
        setDriver(driverManager.getDriver());
        ExtentTestManager.createTest(
                method,
                browserName,
                getAuthors(this.getClass().getName(), method),
                getTestGroups(method));
        extentTest = ExtentTestManager.getExtentTest();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult iTestResult) throws IOException {

        if (iTestResult.getStatus() == SUCCESS) {
            ExtentTestManager.getExtentTest().log(Status.PASS, "Test Passed");
        }
        if (iTestResult.getStatus() == FAILURE) {
            String imgPrefix = takeScreenShot(iTestResult.getMethod().getMethodName());
            ExtentTestManager.getExtentTest().fail(iTestResult.getThrowable());
            ExtentTestManager.getExtentTest().fail(
                    "Screenshot: ",
                    MediaEntityBuilder.createScreenCaptureFromPath(imgPrefix).build());
            ExtentTestManager.getExtentTest().log(Status.FAIL, "Test Failed");
        }
        if (iTestResult.getStatus() == SKIP) {
            ExtentTestManager.getExtentTest().log(Status.SKIP, "Test Skipped");
        }

        driverManager.quitDriver();
        ExtentTestManager.extentReport.flush();
    }

    public ExtentTest getExtentTest() {
        return extentTest;
    }
}
