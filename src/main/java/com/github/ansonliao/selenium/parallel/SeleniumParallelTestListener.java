package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.factory.DriverManagerFactory;
import com.github.ansonliao.selenium.factory.WebDriverManager;
import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.internal.Variables;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import com.github.ansonliao.selenium.utils.AuthorUtils;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import com.github.ansonliao.selenium.utils.SEConfig;
import com.github.ansonliao.selenium.utils.TestGroupUtils;
import com.github.ansonliao.selenium.utils.WDMHelper;
import org.testng.IClassListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.List;

public class SeleniumParallelTestListener implements
        IClassListener, IInvokedMethodListener, ISuiteListener {

    @Override
    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        String browserName = iInvokedMethod.getTestMethod().getXmlTest()
                .getParameter(Constants.TESTNG_XML_BROWSER_PARAMETER_KEY);
        WebDriverManager.setDriver(
                DriverManagerFactory.getManager(browserName).getDriver());

        Method method = iInvokedMethod.getTestMethod().getConstructorOrMethod().getMethod();

        List<String> groups = Variables.TESTING_TEST_GROUPS.isEmpty()
                ? TestGroupUtils.getMethodTestGroups(method)
                : Variables.TESTING_TEST_GROUPS;
        if (SEConfig.getBoolean("addBrowserGroupToReport")) {
            groups.add(browserName);
        }

        ExtentTestManager.createTest(method, browserName, AuthorUtils.getMethodAuthors(method), groups);
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
        iSuite.getXmlSuite().getTests().stream()
                .map(xmlTest -> xmlTest.getParameter(Constants.TESTNG_XML_BROWSER_PARAMETER_KEY))
                .forEach(WDMHelper::downloadWebDriverBinary);
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
}
