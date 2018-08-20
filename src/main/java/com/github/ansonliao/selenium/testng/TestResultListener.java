package com.github.ansonliao.selenium.testng;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.factory.WDManager;
import com.github.ansonliao.selenium.internal.ScreenshotManager;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.IOException;
import java.util.Arrays;

import static com.github.ansonliao.selenium.utils.StringUtils.removeQuoteMark;
import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

public class TestResultListener extends TestListenerAdapter {

    private static Logger logger =
            LoggerFactory.getLogger(TestListenerAdapter.class);

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        super.onTestFailure(iTestResult);
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
        WDManager.quitAndClearDriver();
        ExtentTestManager.extentReport.flush();
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        super.onTestSkipped(iTestResult);
        logger.info("Test skipped: {} - {} - {}",
                getTestRealClass(iTestResult).getName(),
                getTestMethod(iTestResult).getMethodName(),
                getTestBrowser(iTestResult));
        ExtentTestManager.getExtentTest().log(Status.SKIP, "Test Skipped");
        WDManager.quitAndClearDriver();
        ExtentTestManager.extentReport.flush();
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        super.onTestSuccess(iTestResult);
        logger.info("Test succeeded: {} - {} - {}",
                getTestRealClass(iTestResult).getName(),
                getTestMethod(iTestResult).getMethodName(),
                getTestBrowser(iTestResult));
        ExtentTestManager.getExtentTest().log(Status.PASS, "Test Passed");
        WDManager.quitAndClearDriver();
        ExtentTestManager.extentReport.flush();
    }

    @Override
    public void onFinish(ITestContext iTestContext) {
        /**
         super.onFinish(iTestContext);

         // List of test results which we will delete later
         ArrayList<ITestResult> testsToBeRemoved = Lists.newArrayList();

         // Collect all id's from passed test
         Set<Integer> passedTestIds = Sets.newHashSet();
         iTestContext.getPassedTests().getAllResults().forEach(iTestResult -> {
         logger.info("Passed test: " + iTestResult.getName());
         passedTestIds.add(getTestId(iTestResult));
         });

         // Eliminate the repeat methods
         Set<Integer> skipTestIds = Sets.newHashSet();
         iTestContext.getSkippedTests().getAllResults().forEach(iTestResult -> {
         logger.info("Skip test: " + iTestResult.getName());

         // id = class + method + dataprovider
         int skipTestId = getTestId(iTestResult);
         if (skipTestIds.contains(skipTestId) || passedTestIds.contains(skipTestId)) {
         testsToBeRemoved.add(iTestResult);
         } else {
         skipTestIds.add(skipTestId);
         }
         });

         // Eliminate the repeat failed methods
         Set<Integer> failedTestIds = Sets.newHashSet();
         iTestContext.getFailedTests().getAllResults().forEach(iTestResult -> {
         logger.info("Failed test: " + iTestResult.getName());

         // id = class + method + dataprovider
         int failedTestId = getTestId(iTestResult);

         // If we saw this test as a failed test before we mark as to be
         // deleted
         // or delete this failed test if there is at least one passed
         // version
         if (failedTestIds.contains(failedTestId)
         || passedTestIds.contains(failedTestId)
         || skipTestIds.contains(failedTestId)) {
         testsToBeRemoved.add(iTestResult);
         } else {
         failedTestIds.add(failedTestId);
         }
         });

         // Finally delete all tests that are marked
         for (Iterator<ITestResult> iterator =
         iTestContext.getFailedTests().getAllResults().iterator();
         iterator.hasNext();) {
         ITestResult testResult = iterator.next();
         if (testsToBeRemoved.contains(testResult)) {
         logger.info("Remove repeat failed test: " + testResult.getName());
         iterator.remove();
         }
         }

         iTestContext.getFailedTests().getAllResults().forEach(iTestResult -> {
         if (testsToBeRemoved.contains(iTestResult)) {
         iTestContext.getFailedTests().getAllResults().remove(iTestResult);
         }
         });
         */
    }

    private Class<?> getTestRealClass(ITestResult iTestResult) {
        return iTestResult.getTestClass().getRealClass();
    }

    private ITestNGMethod getTestMethod(ITestResult iTestResult) {
        return iTestResult.getMethod();
    }

    private String getTestBrowser(ITestResult iTestResult) {
        return iTestResult.getTestContext().getCurrentXmlTest()
                .getAllParameters().get(removeQuoteMark(getConfigInstance().testngXmlBrowserParamKey()));
    }

    private int getTestId(ITestResult result) {
        int id = result.getTestClass().getName().hashCode();
        id = id + result.getMethod().getMethodName().hashCode();
        id = id + (result.getParameters() != null ? Arrays.hashCode(result.getParameters()) : 0);
        return id;
    }
}
