package com.github.ansonliao.selenium.testng;

import com.github.ansonliao.selenium.driver.SelenideManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import static com.github.ansonliao.selenium.utils.StringUtils.removeQuoteMark;
import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

public class TestResultListener extends TestListenerAdapter {

    private static final Logger logger =
            LoggerFactory.getLogger(TestResultListener.class);

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        super.onTestFailure(iTestResult);
        logger.info("Test failed: {} - {} - {}",
                getTestRealClass(iTestResult).getName(),
                getTestMethod(iTestResult).getMethodName(),
                getTestBrowser(iTestResult));
        SelenideManager.closeDriver();
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        super.onTestSkipped(iTestResult);
        logger.info("Test skipped: {} - {} - {}",
                getTestRealClass(iTestResult).getName(),
                getTestMethod(iTestResult).getMethodName(),
                getTestBrowser(iTestResult));
        SelenideManager.closeDriver();
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        super.onTestSuccess(iTestResult);
        logger.info("Test succeeded: {} - {} - {}",
                getTestRealClass(iTestResult).getName(),
                getTestMethod(iTestResult).getMethodName(),
                getTestBrowser(iTestResult));
        SelenideManager.closeDriver();
    }

    @Override
    public void onFinish(ITestContext iTestContext) {
        super.onFinish(iTestContext);
    }

    private Class<?> getTestRealClass(ITestResult iTestResult) {
        return iTestResult.getTestClass().getRealClass();
    }

    private ITestNGMethod getTestMethod(ITestResult iTestResult) {
        return iTestResult.getMethod();
    }

    private String getTestBrowser(ITestResult iTestResult) {
        if (iTestResult.getTestContext() == null
                || iTestResult.getTestContext().getCurrentXmlTest() == null) {
            return getConfigInstance().defaultBrowser();
        }
        String browser = iTestResult.getTestContext().getCurrentXmlTest()
                .getAllParameters().get(removeQuoteMark(getConfigInstance().testngXmlBrowserParamKey()));
        return browser == null ? getConfigInstance().defaultBrowser() : browser;
    }
}
