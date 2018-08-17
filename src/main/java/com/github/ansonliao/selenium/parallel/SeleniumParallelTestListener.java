package com.github.ansonliao.selenium.parallel;

import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.annotations.Headless;
import com.github.ansonliao.selenium.annotations.Incognito;
import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.exceptions.IllegalBrowserDriverName;
import com.github.ansonliao.selenium.factory.DriverManager;
import com.github.ansonliao.selenium.factory.DriverManagerFactory;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import com.github.ansonliao.selenium.utils.AuthorUtils;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import com.github.ansonliao.selenium.utils.TestGroupUtils;
import com.github.ansonliao.selenium.utils.WDMHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IClassListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestResult;
import org.testng.util.Strings;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.github.ansonliao.selenium.factory.WDManager.getDriver;
import static com.github.ansonliao.selenium.factory.WDManager.setDriver;
import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;
import static java.util.stream.Collectors.toList;

public class SeleniumParallelTestListener implements IClassListener,
        IInvokedMethodListener, ISuiteListener {
    private static final Logger logger =
            LoggerFactory.getLogger(SeleniumParallelTestListener.class);

    @Override
    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        String browserName = iInvokedMethod.getTestMethod().getXmlTest()
                .getParameter(getConfigInstance().testngXmlBrowserParamKey());

        Method method =
                iInvokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
        DriverManager driverManager =
                DriverManagerFactory.getManager(browserName);
        driverManager.setHeadless(method.isAnnotationPresent(Headless.class));
        driverManager.setIncognito(method.isAnnotationPresent(Incognito.class));
        setDriver(driverManager.getDriver());

        List<String> groups = TestGroupUtils.getMethodTestGroups(method);
        if (getConfigInstance().addBrowserGroupToReport()) {
            groups.add(browserName);
        }

        ExtentTestManager.createTest(
                method, browserName, AuthorUtils.getMethodAuthors(method),
                groups, iTestResult.getParameters());

        // open url if URL annotation had value
        Optional.ofNullable(method.getAnnotation(URL.class))
                .ifPresent(url -> openRemoteURL(url.value().trim()));
    }

    @Override
    public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {

    }

    @Override
    public void onStart(ISuite iSuite) {
        /** TODO:
         * until WDM 1.7.2, download WebDriver binary only works at single thread,
         * upgrade WebDriver binary to Async with multi-threads once WDM support
         * multi-thread download
         */
        final String SELENIUM_HUB_URL = getConfigInstance().seleniumHubUrl();
        if (Strings.isNullOrEmpty(SELENIUM_HUB_URL)) {
            List<String> browserList = iSuite.getXmlSuite().getTests().stream()
                    .map(xmlTest -> xmlTest.getParameter(getConfigInstance().testngXmlBrowserParamKey()))
                    .map(browserName -> browserName.replace("\"", ""))
                    .distinct().collect(toList());

            browserList.parallelStream().forEach(WDMHelper::downloadWebDriverBinary);
            logger.info("Completed WebDriver binary download: {}", browserList);
            return;
        }
        logger.info("Selenium Hub found: [{}], WebDriver binaries will not be downloaded.", SELENIUM_HUB_URL);
    }

    @Override
    public void onFinish(ISuite iSuite) {

    }

    @Override
    public void onBeforeClass(ITestClass iTestClass) {
        String browserName =
                iTestClass.getXmlTest().getParameter(getConfigInstance().testngXmlBrowserParamKey());
        MyFileUtils.createScreenshotFolderForBrowser(iTestClass.getRealClass(), browserName);
    }

    @Override
    public void onAfterClass(ITestClass iTestClass) {

    }

    private void openRemoteURL(String url) {
        if (Strings.isNotNullAndNotEmpty(url)) {
            getDriver().get(url);
            ExtentTestManager.getExtentTest().log(
                    Status.INFO,
                    String.format("Open URL: %s", url));
        }
    }
}
