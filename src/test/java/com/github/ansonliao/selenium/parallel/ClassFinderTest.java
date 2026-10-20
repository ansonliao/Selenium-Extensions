package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.json.JsonParser;
import com.github.ansonliao.selenium.json.JsonParserTest;
import com.github.ansonliao.selenium.utils.StringUtils;
import org.testng.annotations.Test;
import testcases.ClassFinderSample;
import testcases.Test_Demo;

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

    @Test
    public void findReturnsEveryClassOfAnExistingPackage() {
        List<Class<?>> classes = ClassFinder.find("com.github.ansonliao.selenium.json");
        List<String> found = classes.stream().map(Class::getName).toList();

        // The package exists in both the main and the test output directory;
        // whichever classpath entry comes first is scanned.
        assertFalse(found.isEmpty(), "found: " + found);
        assertTrue(classes.contains(JsonParserTest.class), "found: " + found);
        assertTrue(found.stream()
                .allMatch(name -> name.startsWith("com.github.ansonliao.selenium.json.")),
                "found: " + found);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void findRejectsAnUnknownPackage() {
        ClassFinder.find("com.github.ansonliao.selenium.no.such.package");
    }

    @Test
    public void findAllTestClassesInPackageKeepsClassesNamedLikeTests() {
        List<Class<?>> classes = ClassFinder.findAllTestClassesInPackage("testcases");

        assertTrue(classes.contains(Test_Demo.class));
        assertFalse(classes.contains(ClassFinderSample.class));
    }

    @Test
    public void findAllTestNGTestClassesRestrictsResultToGivenPackages() {
        List<Class<?>> classes = ClassFinder.findAllTestNGTestClasses("testcases");

        assertTrue(classes.contains(Test_Demo.class));
        assertTrue(classes.stream()
                .allMatch(clazz -> "testcases".equals(clazz.getPackage().getName())));
    }

    @Test
    public void getAllClassNamesExposesTheScannedClasspath() {
        List<String> names = ClassFinder.getAllClassNames();

        assertFalse(names.isEmpty());
        assertTrue(names.contains(JsonParser.class.getName()));
    }
}
