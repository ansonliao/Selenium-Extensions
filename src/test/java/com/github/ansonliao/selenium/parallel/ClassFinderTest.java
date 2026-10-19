package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.utils.StringUtils;
import org.testng.annotations.Test;
import testcases.ClassFinderSample;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ClassFinderTest {

    @Test
    public void createClassResolvesExistingClass() {
        assertEquals(ClassFinder.createClass("java.lang.String"), String.class);
    }

    @Test
    public void createClassReturnsNullForUnknownClass() {
        assertEquals(ClassFinder.createClass("no.such.Klass"), null);
    }

    @Test
    public void findAnnotatedTestClassesKeepsOnlyPrefixedPackages() {
        List<Class<?>> classes = Arrays.asList(StringUtils.class, ClassFinderSample.class);
        Set<Class<?>> filtered = ClassFinder.findAnnotatedTestClasses(classes);
        assertTrue(filtered.contains(ClassFinderSample.class));
        assertFalse(filtered.contains(StringUtils.class));
    }
}
