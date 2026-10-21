package io.github.ansonliao.selenium.testng;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RetryListener implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation iTestAnnotation, Class aClass,
                          Constructor constructor, Method method) {
        Class<? extends IRetryAnalyzer> retryAnalyzer = iTestAnnotation.getRetryAnalyzerClass();
        if (retryAnalyzer == null) {
            iTestAnnotation.setRetryAnalyzer(TestNGRetry.class);
        }
    }
}
