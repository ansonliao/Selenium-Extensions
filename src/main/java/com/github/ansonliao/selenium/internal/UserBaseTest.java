package com.github.ansonliao.selenium.internal;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.factory.DriverManagerFactory;
import com.github.ansonliao.selenium.parallel.SeleniumParallel;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import com.github.ansonliao.selenium.utils.TestGroupUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.testng.ITestResult.FAILURE;
import static org.testng.ITestResult.SKIP;
import static org.testng.ITestResult.SUCCESS;

public class UserBaseTest extends SeleniumParallel {
    protected ExtentTest extentTest;

    @BeforeClass
    public void beforeClass(ITestContext iTestContext) {
        browserName = iTestContext.getCurrentXmlTest().getAllParameters()
                .get(Constants.TESTNG_XML_BROWSER_PARAMETER_KEY)
                .toString().trim();
        driverManager = DriverManagerFactory.getManager(browserName);
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
                Variables.TESTING_TEST_GROUPS.isEmpty()
                        ? TestGroupUtils.getMethodTestGroups(method)
                        : Variables.TESTING_TEST_GROUPS);
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
