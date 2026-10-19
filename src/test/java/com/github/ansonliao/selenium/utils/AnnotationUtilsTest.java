package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.testfixtures.ChromeFixture;
import com.github.ansonliao.selenium.testfixtures.MethodFinderSample;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AnnotationUtilsTest {

    @Test
    public void getClassAnnotationsReturnsDeclaredAnnotations() {
        Set<Annotation> annotations = AnnotationUtils.getClassAnnotations(ChromeFixture.class);
        assertEquals(annotations.size(), 1);
        assertEquals(annotations.iterator().next().annotationType().getSimpleName(),
                "Chrome");
    }

    @Test
    public void getClassAnnotationsEmptyForUnannotatedClass() {
        assertTrue(AnnotationUtils.getClassAnnotations(StringUtils.class).isEmpty());
    }

    @Test
    public void getMethodAnnotationsReturnsDeclaredAnnotations() throws Exception {
        Method method = MethodFinderSample.class.getDeclaredMethod("smokeTest");
        Set<Annotation> annotations = AnnotationUtils.getMethodAnnotations(method);
        assertEquals(annotations.size(), 1);
        assertEquals(annotations.iterator().next().annotationType().getSimpleName(),
                "Test");
    }

    @Test
    public void getMethodAnnotationsEmptyForUnannotatedMethod() throws Exception {
        Method method = MethodFinderSample.class.getDeclaredMethod("helper");
        assertTrue(AnnotationUtils.getMethodAnnotations(method).isEmpty());
    }
}
