package com.github.ansonliao.selenium.internal;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.factory.DriverManagerFactory;
import com.github.ansonliao.selenium.parallel.SeleniumParallel;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import com.github.ansonliao.selenium.utils.SEConfig;
import com.github.ansonliao.selenium.utils.TestGroupUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import static org.testng.ITestResult.FAILURE;
import static org.testng.ITestResult.SKIP;
import static org.testng.ITestResult.SUCCESS;

public class UserBaseTest extends SeleniumParallel {
    protected ExtentTest extentTest;

    @BeforeTest(alwaysRun = true)
    public void beforeTest(ITestContext iTestContext) {
        browserName = iTestContext.getCurrentXmlTest().getAllParameters()
                .get(Constants.TESTNG_XML_BROWSER_PARAMETER_KEY)
                .toString().trim();
        driverManager = DriverManagerFactory.getManager(browserName);
    }

    @BeforeClass
    public void beforeClass(ITestContext iTestContext) {
        MyFileUtils.createScreenshotFolderForBrowser(
                this.getClass(), browserName);
    }

    @BeforeMethod
    public void beforeMethod(Method method) {
        url = findUrl(method);
        setDriver(driverManager.getDriver());
        List<String> groups = Variables.TESTING_TEST_GROUPS.isEmpty()
                ? TestGroupUtils.getMethodTestGroups(method)
                : Variables.TESTING_TEST_GROUPS;

        if (SEConfig.getBoolean("se.addBrowserGroupToReport")) {
            groups.add(browserName);
        }

        ExtentTestManager.createTest(
                method,
                browserName,
                getAuthors(this.getClass().getName(), method),
                groups);
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
        //getDriver().quit();
        ExtentTestManager.extentReport.flush();
    }

    public ExtentTest getExtentTest() {
        return extentTest;
    }
}
