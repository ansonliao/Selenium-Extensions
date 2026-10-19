package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.testfixtures.MethodFinderSample;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MethodFinderTest {

    @Test
    public void findMethodInClassReturnsAllDeclared() {
        List<Method> methods = MethodFinder.findMethodInClass(MethodFinderSample.class);
        // helper + two @Test methods
        assertTrue(methods.size() >= 3);
    }

    @Test
    public void findAllAnnotatedTestMethodInClassReturnsOnlyTestMethods() {
        List<Method> methods = MethodFinder.findAllAnnotatedTestMethodInClass(MethodFinderSample.class);
        assertEquals(methods.size(), 2);
        assertTrue(methods.stream().allMatch(m -> m.isAnnotationPresent(Test.class)));
    }

    @Test
    public void findTestNGMethodInClassByGroupFiltersByMethodGroup() {
        List<Method> methods = MethodFinder.findTestNGMethodInClassByGroup(
                MethodFinderSample.class, "smoke");
        assertEquals(methods.size(), 1);
        assertEquals(methods.get(0).getName(), "smokeTest");
    }

    @Test
    public void findTestNGMethodInClassByGroupReturnsEmptyForUnknownGroup() {
        List<Method> methods = MethodFinder.findTestNGMethodInClassByGroup(
                MethodFinderSample.class, "unknown");
        assertTrue(methods.isEmpty());
    }
}
