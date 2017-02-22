package com.github.ansonliao.selenium.internal;

import com.github.ansonliao.selenium.parallel.SeleniumParallel;
import com.github.ansonliao.selenium.utils.BrowserUtils;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by ansonliao on 16/2/2017.
 */
public class BaseTestCase extends SeleniumParallel {
    private WebDriver webDriver;

    @BeforeMethod
    public void beforeMethod(Method method) {
        System.out.println(BrowserUtils.getMethodSupportedBrowsers(method));
        Annotation[] annotations = method.getDeclaredAnnotations();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult iTestResult) throws IOException {
        takeScreenshotOnFailure(iTestResult);
    }

    private void takeScreenshotOnFailure(ITestResult iTestResult) throws IOException {
        String dest = "";
        if (iTestResult.getStatus() == ITestResult.FAILURE) {
            File scrFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File(dest));
        }
    }
}
