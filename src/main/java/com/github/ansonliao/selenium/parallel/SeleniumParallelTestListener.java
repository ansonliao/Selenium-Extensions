package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.factory.DriverManagerFactory;
import com.github.ansonliao.selenium.factory.WDManager;
import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import com.github.ansonliao.selenium.utils.AuthorUtils;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import com.github.ansonliao.selenium.utils.SEConfig;
import com.github.ansonliao.selenium.utils.TestGroupUtils;
import com.github.ansonliao.selenium.utils.WDMHelper;
import com.google.common.base.Strings;
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
import java.util.stream.Collectors;

import static com.github.ansonliao.selenium.factory.WDManager.getDriver;
import static com.github.ansonliao.selenium.factory.WDManager.setDriver;

public class SeleniumParallelTestListener implements IClassListener,
        IInvokedMethodListener, ISuiteListener {
    private static final Logger logger =
            LoggerFactory.getLogger(SeleniumParallelTestListener.class);

    @Override
    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        String browserName = iInvokedMethod.getTestMethod().getXmlTest()
                .getParameter(Constants.TESTNG_XML_BROWSER_PARAMETER_KEY);
        setDriver(DriverManagerFactory.getManager(browserName).getDriver());

        Method method =
                iInvokedMethod.getTestMethod().getConstructorOrMethod().getMethod();

        List<String> groups = TestGroupUtils.getMethodTestGroups(method);
        if (SEConfig.getBoolean("addBrowserGroupToReport")) {
            groups.add(browserName);
        }

        ExtentTestManager.createTest(method, browserName,
                AuthorUtils.getMethodAuthors(method), groups);

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
        List<String> browserList = iSuite.getXmlSuite().getTests().stream()
                .map(xmlTest -> xmlTest.getParameter(Constants.TESTNG_XML_BROWSER_PARAMETER_KEY))
                .distinct().collect(Collectors.toList());
        browserList.forEach(WDMHelper::downloadWebDriverBinary);
        logger.info("Completed WebDriver binary download: {}", browserList);
    }

    @Override
    public void onFinish(ISuite iSuite) {

    }

    @Override
    public void onBeforeClass(ITestClass iTestClass) {
        String browserName =
                iTestClass.getXmlTest().getParameter(Constants.TESTNG_XML_BROWSER_PARAMETER_KEY);
        MyFileUtils.createScreenshotFolderForBrowser(
                iTestClass.getRealClass(),
                browserName);
    }

    @Override
    public void onAfterClass(ITestClass iTestClass) {

    }

    private void openRemoteURL(String url) {
        if (!Strings.isNullOrEmpty(url)) {
            getDriver().get(url);
        }
    }
}
