package io.github.ansonliao.selenium.testng;

import io.github.ansonliao.selenium.driver.SelenideManager;
import io.github.ansonliao.selenium.testfixtures.UrlFixture;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Exercises {@link TestResultListener} with JDK proxies, so no TestNG runtime
 * and no browser are required.
 */
public class TestResultListenerTest {

    private <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{type}, handler));
    }

    /**
     * Builds a minimal {@link ITestResult}. {@code context} may be null to
     * simulate a result reported outside an XML test.
     */
    private ITestResult resultFor(Method method, ITestContext context) {
        ITestClass testClass = proxy(ITestClass.class, (p, m, args) ->
                "getRealClass".equals(m.getName()) ? method.getDeclaringClass() : null);

        ITestNGMethod testNgMethod = proxy(ITestNGMethod.class, (p, m, args) -> {
            switch (m.getName()) {
                case "getMethodName":
                    return method.getName();
                case "getConstructorOrMethod":
                    return new ConstructorOrMethod(method);
                case "getTestClass":
                    return testClass;
                default:
                    return null;
            }
        });

        return proxy(ITestResult.class, (p, m, args) -> {
            switch (m.getName()) {
                case "getMethod":
                    return testNgMethod;
                case "getTestClass":
                    return testClass;
                case "getTestContext":
                    return context;
                default:
                    return null;
            }
        });
    }

    private ITestContext contextWithBrowser(String browser) {
        XmlSuite suite = new XmlSuite();
        suite.setName("unit-suite");
        XmlTest xmlTest = new XmlTest(suite);
        xmlTest.setName("Selenium Test - " + browser);
        if (browser != null) {
            xmlTest.addParameter("browser", browser);
        }
        return proxy(ITestContext.class, (p, m, args) ->
                "getCurrentXmlTest".equals(m.getName()) ? xmlTest : null);
    }

    @Test
    public void onTestSuccessRecordsResultAndReleasesDriver() throws Exception {
        TestResultListener listener = new TestResultListener();
        ITestResult result = resultFor(UrlFixture.class.getDeclaredMethod("methodLevelUrl"), null);

        listener.onTestSuccess(result);

        assertEquals(listener.getPassedTests().size(), 1);
        assertTrue(listener.getFailedTests().isEmpty());
        assertFalse(SelenideManager.hasDriver());
    }

    @Test
    public void onTestFailureRecordsResultAndReleasesDriver() throws Exception {
        TestResultListener listener = new TestResultListener();
        ITestResult result = resultFor(
                UrlFixture.class.getDeclaredMethod("methodLevelUrl"), contextWithBrowser("Firefox"));

        listener.onTestFailure(result);

        assertEquals(listener.getFailedTests().size(), 1);
        assertTrue(listener.getPassedTests().isEmpty());
        assertFalse(SelenideManager.hasDriver());
    }

    @Test
    public void onTestSkippedRecordsResultAndReleasesDriver() throws Exception {
        TestResultListener listener = new TestResultListener();
        ITestResult result = resultFor(UrlFixture.class.getDeclaredMethod("methodLevelUrl"), null);

        listener.onTestSkipped(result);

        assertEquals(listener.getSkippedTests().size(), 1);
        assertFalse(SelenideManager.hasDriver());
    }

    @Test
    public void onTestSuccessFallsBackToDefaultBrowserWhenParameterMissing() throws Exception {
        TestResultListener listener = new TestResultListener();
        ITestResult result = resultFor(
                UrlFixture.class.getDeclaredMethod("inheritsClassLevelUrl"),
                contextWithBrowser(null));

        listener.onTestSuccess(result);

        assertEquals(listener.getPassedTests().size(), 1);
    }

    @Test
    public void onFinishIsANoOp() {
        TestResultListener listener = new TestResultListener();
        listener.onFinish(contextWithBrowser("Chrome"));
        assertTrue(listener.getPassedTests().isEmpty());
    }
}
