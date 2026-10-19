package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.driver.SelenideManager;
import com.github.ansonliao.selenium.internal.ScreenshotManager;
import com.github.ansonliao.selenium.report.AllureReport;
import com.github.ansonliao.selenium.utils.AuthorUtils;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import com.github.ansonliao.selenium.utils.TestGroupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IClassListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.github.ansonliao.selenium.utils.StringUtils.isNotNullAndNotEmpty;
import static com.github.ansonliao.selenium.utils.StringUtils.isNullOrEmpty;
import static com.github.ansonliao.selenium.utils.StringUtils.removeQuoteMark;
import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

public class SeleniumParallelTestListener
        implements IClassListener, IInvokedMethodListener, ISuiteListener {

    private static final Logger logger =
            LoggerFactory.getLogger(SeleniumParallelTestListener.class);
    private static final String TESTNG_BROWSER_PARAM_KEY =
            removeQuoteMark(getConfigInstance().testngXmlBrowserParamKey());

    @Override
    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        if (!iInvokedMethod.isTestMethod()) {
            return;
        }

        String browserName = iInvokedMethod.getTestMethod().getXmlTest() == null
                ? null
                : iInvokedMethod.getTestMethod().getXmlTest()
                        .getParameter(TESTNG_BROWSER_PARAM_KEY);
        if (isNullOrEmpty(browserName)) {
            browserName = getConfigInstance().defaultBrowser();
        }

        Method method =
                iInvokedMethod.getTestMethod().getConstructorOrMethod().getMethod();

        SelenideManager.createDriver(browserName, method);

        List<String> groups = TestGroupUtils.getMethodTestGroups(method);
        if (getConfigInstance().addBrowserGroupToReport()) {
            groups.add(browserName);
        }

        AllureReport.describeTest(method, browserName,
                AuthorUtils.getMethodAuthors(method), groups);

        // open url if URL annotation had value
        Optional.ofNullable(method.getAnnotation(URL.class))
                .ifPresent(url -> openRemoteURL(url.value().trim()));
    }

    @Override
    public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        if (!iInvokedMethod.isTestMethod()
                || iTestResult.getStatus() != ITestResult.FAILURE
                || !SelenideManager.hasStartedDriver()) {
            return;
        }

        String browserName = iInvokedMethod.getTestMethod().getXmlTest() == null
                ? null
                : iInvokedMethod.getTestMethod().getXmlTest()
                        .getParameter(TESTNG_BROWSER_PARAM_KEY);
        if (isNullOrEmpty(browserName)) {
            browserName = getConfigInstance().defaultBrowser();
        }

        ScreenshotManager screenshotManager = new ScreenshotManager();
        String imgPath = screenshotManager.capture(
                iTestResult.getTestClass().getRealClass(),
                iTestResult.getMethod().getMethodName(),
                browserName);
        AllureReport.attachScreenshot(imgPath);
        logger.info("Attached failure screenshot: {}", imgPath);
    }

    @Override
    public void onStart(ISuite iSuite) {
        logger.info("Starting TestNG suite: {}", iSuite.getName());
    }

    @Override
    public void onFinish(ISuite iSuite) {
    }

    @Override
    public void onBeforeClass(ITestClass iTestClass) {
        String browserName = iTestClass.getXmlTest() == null
                ? null
                : iTestClass.getXmlTest().getParameter(TESTNG_BROWSER_PARAM_KEY);
        if (isNullOrEmpty(browserName)) {
            browserName = getConfigInstance().defaultBrowser();
        }
        MyFileUtils.createScreenshotFolderForBrowser(
                iTestClass.getRealClass(), browserName);
    }

    @Override
    public void onAfterClass(ITestClass iTestClass) {
    }

    private void openRemoteURL(String url) {
        if (isNotNullAndNotEmpty(url)) {
            SelenideManager.getSelenideDriver().open(url);
            AllureReport.step("Open URL: " + url);
        }
    }
}
