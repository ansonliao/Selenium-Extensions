package com.github.ansonliao.selenium.testng;

import com.google.common.collect.Multimap;

import com.github.ansonliao.selenium.testfixtures.MethodFilterFixture;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Covers the filtering pipeline of {@link TestNGFilter}. The static discovery
 * state is seeded per test, see {@link TestNGFilterState}.
 */
public class TestNGFilterTest {

    private TestNGFilterState state;

    @BeforeMethod
    public void seedFilterState() {
        state = TestNGFilterState.seed(MethodFilterFixture.class);
    }

    @AfterMethod
    public void restoreFilterState() {
        state.close();
    }

    private Set<String> methodNames(Multimap<Class<?>, Method> map, Class<?> clazz) {
        return map.get(clazz).stream().map(Method::getName).collect(Collectors.toSet());
    }

    @Test
    public void filterClassesByTestingClassListReturnsSeededClassesWhenNoFilterGiven() {
        List<Class<?>> classes = TestNGFilter.filterClassesByTestingClassList();

        assertEquals(classes, List.of(MethodFilterFixture.class));
    }

    @Test
    public void filterTestNGMethodByTestGroupsCollectsEveryEnabledTestMethod() {
        Multimap<Class<?>, Method> filtered = TestNGFilter.filterTestNGMethodByTestGroups();

        assertEquals(filtered.get(MethodFilterFixture.class).size(), 4);
        assertEquals(methodNames(filtered, MethodFilterFixture.class),
                Set.of("unannotated", "chromeOnly", "notForChrome", "smoke"));
    }

    @Test
    public void filterTestNGMethodByBrowsersIsNoOpWithoutBrowserFilter() {
        TestNGFilter.filterTestNGMethodByTestGroups();

        Multimap<Class<?>, Method> filtered = TestNGFilter.filterTestNGMethodByBrowsers();

        assertEquals(filtered.get(MethodFilterFixture.class).size(), 4);
    }

    @Test
    public void browserMapHonoursIgnoreAnnotationsAndDefaultBrowser() {
        TestNGFilter.filterTestNGMethodByTestGroups();

        Map<String, Map<Class<?>, List<Method>>> browserMap =
                TestNGFilter.getBrowser2TestNGClass2TestNGMethodMap();

        assertEquals(browserMap.keySet(), Set.of("CHROME"));
        List<Method> chromeMethods = browserMap.get("CHROME").get(MethodFilterFixture.class);
        assertEquals(chromeMethods.size(), 3);
        Set<String> names = chromeMethods.stream().map(Method::getName).collect(Collectors.toSet());
        assertTrue(names.contains("unannotated"));
        assertTrue(names.contains("chromeOnly"));
        assertTrue(names.contains("smoke"));
        assertFalse(names.contains("notForChrome"));
    }

    @Test
    public void runAppliesEveryFilterAndReturnsBrowserMap() {
        Map<String, Map<Class<?>, List<Method>>> browserMap = TestNGFilter.run();

        assertTrue(browserMap.containsKey("CHROME"));
        assertEquals(browserMap.get("CHROME").get(MethodFilterFixture.class).size(), 3);
    }
}
