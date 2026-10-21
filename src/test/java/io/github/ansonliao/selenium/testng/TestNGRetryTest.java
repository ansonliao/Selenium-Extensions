package io.github.ansonliao.selenium.testng;

import io.github.ansonliao.selenium.testfixtures.RetryFixture;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestNGRetryTest {

    private ITestResult resultFor(Class<?> clazz, Method method) {
        ConstructorOrMethod com = new ConstructorOrMethod(method);
        ITestClass testClass = (ITestClass) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ITestClass.class},
                (proxy, m, args) -> "getRealClass".equals(m.getName()) ? clazz : null);
        ITestNGMethod testNgMethod = (ITestNGMethod) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ITestNGMethod.class},
                (proxy, m, args) -> {
                    switch (m.getName()) {
                        case "getConstructorOrMethod":
                            return com;
                        case "getTestClass":
                            return testClass;
                        default:
                            return null;
                    }
                });
        return (ITestResult) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ITestResult.class},
                (proxy, m, args) -> "getMethod".equals(m.getName()) ? testNgMethod : null);
    }

    @Test
    public void retriesUpToMaxRetryCountThenStops() throws Exception {
        Method method = RetryFixture.class.getDeclaredMethod("retriable");
        ITestResult result = resultFor(RetryFixture.class, method);

        TestNGRetry retry = new TestNGRetry();
        assertTrue(retry.retry(result));  // 1st attempt
        assertTrue(retry.retry(result));  // 2nd attempt
        assertFalse(retry.retry(result)); // exhausted
    }

    @Test
    public void noRetryAnnotationMeansNoRetry() throws Exception {
        Method method = RetryFixture.class.getDeclaredMethod("plain");
        ITestResult result = resultFor(RetryFixture.class, method);

        TestNGRetry retry = new TestNGRetry();
        assertFalse(retry.retry(result));
    }
}
