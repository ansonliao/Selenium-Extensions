package io.github.ansonliao.selenium.testng;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.testng.Assert.assertEquals;

public class RetryListenerTest {

    /** A retry analyzer that never retries, used to prove an existing value is kept. */
    public static class NeverRetry implements IRetryAnalyzer {
        @Override
        public boolean retry(ITestResult result) {
            return false;
        }
    }

    private static final class AnnotationStub implements InvocationHandler {
        private Class<? extends IRetryAnalyzer> retryAnalyzer;

        AnnotationStub(Class<? extends IRetryAnalyzer> initial) {
            this.retryAnalyzer = initial;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "getRetryAnalyzerClass":
                    return retryAnalyzer;
                case "setRetryAnalyzer":
                    retryAnalyzer = (Class<? extends IRetryAnalyzer>) args[0];
                    return null;
                default:
                    return defaultValue(method);
            }
        }

        private Object defaultValue(Method method) {
            Class<?> returnType = method.getReturnType();
            if (method.getName().equals("hashCode")) {
                return 0;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == int.class || returnType == long.class || returnType == short.class) {
                return 0;
            }
            return null;
        }

        ITestAnnotation proxy() {
            return (ITestAnnotation) Proxy.newProxyInstance(
                    RetryListenerTest.class.getClassLoader(),
                    new Class[]{ITestAnnotation.class},
                    this);
        }
    }

    @Test
    public void transformInstallsTestNGRetryWhenNoneConfigured() {
        AnnotationStub stub = new AnnotationStub(null);

        new RetryListener().transform(stub.proxy(), RetryListenerTest.class, null, null);

        assertEquals(stub.retryAnalyzer, TestNGRetry.class);
    }

    @Test
    public void transformKeepsUserConfiguredAnalyzer() {
        AnnotationStub stub = new AnnotationStub(NeverRetry.class);

        new RetryListener().transform(stub.proxy(), RetryListenerTest.class, null, null);

        assertEquals(stub.retryAnalyzer, NeverRetry.class);
    }

    @Test
    public void transformIsIdempotent() {
        AnnotationStub stub = new AnnotationStub(null);
        RetryListener listener = new RetryListener();

        listener.transform(stub.proxy(), RetryListenerTest.class, null, null);
        listener.transform(stub.proxy(), RetryListenerTest.class, null, null);

        assertEquals(stub.retryAnalyzer, TestNGRetry.class);
    }
}
