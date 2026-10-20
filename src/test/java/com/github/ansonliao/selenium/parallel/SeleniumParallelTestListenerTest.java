package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.driver.SelenideManager;
import com.github.ansonliao.selenium.testfixtures.UrlFixture;
import org.testng.IInvokedMethod;
import org.testng.ITestClass;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Covers the guard clauses of {@link SeleniumParallelTestListener}. The happy
 * path would start a real browser, so it is deliberately out of scope here.
 */
public class SeleniumParallelTestListenerTest {

    private <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{type}, handler));
    }

    private IInvokedMethod invokedMethod(boolean isTestMethod) {
        return proxy(IInvokedMethod.class, (p, m, args) ->
                "isTestMethod".equals(m.getName()) ? isTestMethod : null);
    }

    private ITestResult resultWithStatus(int status) {
        return proxy(ITestResult.class, (p, m, args) ->
                "getStatus".equals(m.getName()) ? status : null);
    }

    @Test
    public void beforeInvocationSkipsConfigurationMethods() {
        new SeleniumParallelTestListener().beforeInvocation(
                invokedMethod(false), resultWithStatus(ITestResult.SUCCESS));
        assertFalse(SelenideManager.hasDriver());
    }

    @Test
    public void afterInvocationSkipsConfigurationMethods() {
        new SeleniumParallelTestListener().afterInvocation(
                invokedMethod(false), resultWithStatus(ITestResult.FAILURE));
        assertFalse(SelenideManager.hasDriver());
    }

    @Test
    public void afterInvocationSkipsNonFailingTests() {
        new SeleniumParallelTestListener().afterInvocation(
                invokedMethod(true), resultWithStatus(ITestResult.SUCCESS));
        assertFalse(SelenideManager.hasDriver());
    }

    @Test
    public void afterInvocationSkipsFailureWhenNoBrowserWasStarted() {
        new SeleniumParallelTestListener().afterInvocation(
                invokedMethod(true), resultWithStatus(ITestResult.FAILURE));
        assertFalse(SelenideManager.hasStartedDriver());
    }

    @Test
    public void suiteLifecycleCallbacksAreSafe() {
        SeleniumParallelTestListener listener = new SeleniumParallelTestListener();

        // onStart only logs the suite name; a real ISuite cannot be proxied
        // here because TestNG's ISuite API references optional Guice types.
        listener.onFinish(null);
        listener.onAfterClass(null);
    }

    @Test
    public void onBeforeClassCreatesScreenshotFolderForResolvedBrowser() {
        ITestClass testClass = proxy(ITestClass.class, (p, m, args) ->
                "getRealClass".equals(m.getName()) ? UrlFixture.class : null);

        new SeleniumParallelTestListener().onBeforeClass(testClass);

        File expected = new File(System.getProperty("user.dir"),
                String.join(File.separator, "target", "screenshots",
                        UrlFixture.class.getPackage().getName(),
                        UrlFixture.class.getSimpleName(), "Chrome"));
        assertTrue(expected.isDirectory(), "expected " + expected + " to exist");
    }

    @AfterMethod
    public void cleanup() {
        deleteRecursively(new File(System.getProperty("user.dir"), "target/screenshots"));
    }

    private void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}
