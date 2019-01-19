package com.github.ansonliao.selenium.testng;

import com.github.ansonliao.selenium.annotations.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.lang.reflect.Method;

public class TestNGRetry implements IRetryAnalyzer {
    private static final Logger logger =
            LoggerFactory.getLogger(TestNGRetry.class);
    private static int maxRetryCount;
    private int retryCount = 1;

    public static int getMaxRetryCount() {
        return maxRetryCount;
    }

    @Override
    public boolean retry(ITestResult iTestResult) {
        Class<?> clazz = iTestResult.getMethod().getTestClass().getRealClass();
        Method method = iTestResult.getMethod().getConstructorOrMethod()
                .getMethod();

        //ifPresent(Optional.ofNullable(method.getAnnotation(Retry.class)),
        //        retry -> {
        //            maxRetryCount = retry.maxRetry();
        //            maxRetryCount = maxRetryCount >= 0 ? maxRetryCount : 0; })
        //        .ifPresent(Optional.ofNullable(clzz.getAnnotation(Retry.class)),
        //                retry -> {
        //                    maxRetryCount = retry.maxRetry();
        //                    maxRetryCount = maxRetryCount >= 0 ? maxRetryCount : 0; })
        //        .orElse(() -> maxRetryCount = 0);

        if (method.isAnnotationPresent(Retry.class)) {
            maxRetryCount = method.getAnnotation(Retry.class).maxRetry();
            maxRetryCount = maxRetryCount >= 0 ? maxRetryCount : 0;
        } else if (clazz.isAnnotationPresent(Retry.class)) {
            maxRetryCount = clazz.getAnnotation(Retry.class).maxRetry();
            maxRetryCount = maxRetryCount >= 0 ? maxRetryCount : 0;
        } else {
            maxRetryCount = 0;
        }

        if (retryCount <= maxRetryCount) {
            logger.info("Retry for Class: [{}], Method: [{}], RETRY TIME = {}",
                    clazz.getName(), method.getName(), retryCount);
            retryCount++;
            return true;
        }

        // Support TestNG dataprovider retry
        if (maxRetryCount - retryCount == 1) {
            retryCount = 1;
        }

        return false;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
