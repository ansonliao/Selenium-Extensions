package com.github.ansonliao.selenium.factory;

import com.aventstack.extentreports.ExtentTest;
import com.github.ansonliao.selenium.parallel.SeleniumParallel;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

/**
 * Created by ansonliao on 17/2/2017.
 */
public class UserBaseTest extends SeleniumParallel {
    public ExtentTest extentTest;

    @BeforeMethod
    public void beforeMethod(Method method) {
        url = findUrl(method);
        startWebDriver(method);
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult iTestResult) {
//        if (iTestResult.getStatus() == ITestResult.SUCCESS) {
//            extentTest.log(Status.PASS, iTestResult.getMethod().getMethodName() + " Test Passed");
//        }
        getDriver().quit();
//        ExtentTestManager.extentReport.flush();
    }

    public ExtentTest getExtentTest() {
        return extentTest;
    }
}
