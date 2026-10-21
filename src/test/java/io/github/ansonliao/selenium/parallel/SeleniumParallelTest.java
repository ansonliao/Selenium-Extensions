package io.github.ansonliao.selenium.parallel;

import io.github.ansonliao.selenium.testfixtures.ClassGroupFixture;
import io.github.ansonliao.selenium.testfixtures.DescriptionFixture;
import io.github.ansonliao.selenium.testfixtures.UndescribedFixture;
import io.github.ansonliao.selenium.testfixtures.UrlFixture;
import io.github.ansonliao.selenium.utils.AuthorFixture;
import io.github.ansonliao.selenium.utils.AuthorUtils;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class SeleniumParallelTest {

    private Method method(Class<?> clazz, String name) throws NoSuchMethodException {
        return clazz.getDeclaredMethod(name);
    }

    // ---------- findUrl / getUrl ----------

    @Test
    public void findUrlPrefersMethodLevelAnnotation() throws Exception {
        SeleniumParallel parallel = new SeleniumParallel();
        String url = parallel.findUrl(method(UrlFixture.class, "methodLevelUrl"));
        assertEquals(url, "https://method.example.com");
        assertEquals(parallel.getUrl(), "https://method.example.com");
    }

    @Test
    public void findUrlFallsBackToClassLevelAnnotation() throws Exception {
        SeleniumParallel parallel = new SeleniumParallel();
        String url = parallel.findUrl(method(UrlFixture.class, "inheritsClassLevelUrl"));
        assertEquals(url, "https://class.example.com");
    }

    @Test
    public void findUrlReturnsNullWhenNoAnnotationAnywhere() throws Exception {
        SeleniumParallel parallel = new SeleniumParallel();
        assertNull(parallel.findUrl(method(UndescribedFixture.class, "plain")));
        assertNull(parallel.getUrl());
    }

    // ---------- getTestGroups ----------

    @Test
    public void getTestGroupsReadsClassLevelGroups() {
        SeleniumParallel parallel = new SeleniumParallel();
        List<String> groups = parallel.getTestGroups(ClassGroupFixture.class);
        assertEquals(groups, List.of("smoke"));
    }

    @Test
    public void getTestGroupsMergesClassAndMethodGroups() throws Exception {
        SeleniumParallel parallel = new SeleniumParallel();
        List<String> groups = parallel.getTestGroups(
                method(ClassGroupFixture.class, "methodWithGroup"));
        assertEquals(groups.size(), 2);
        assertTrue(groups.contains("smoke"));
        assertTrue(groups.contains("regression"));
    }

    @Test
    public void getTestGroupsFallsBackToClassGroupsOnUngroupedMethod() throws Exception {
        SeleniumParallel parallel = new SeleniumParallel();
        List<String> groups = parallel.getTestGroups(
                method(ClassGroupFixture.class, "methodWithoutGroup"));
        assertEquals(groups, List.of("smoke"));
    }

    @Test
    public void getTestGroupsIsEmptyWithoutTestAnnotation() throws Exception {
        SeleniumParallel parallel = new SeleniumParallel();
        assertTrue(parallel.getTestGroups(UndescribedFixture.class).isEmpty());
        assertTrue(parallel.getTestGroups(method(UndescribedFixture.class, "plain")).isEmpty());
    }

    // ---------- getDescription ----------

    @Test
    public void getDescriptionTrimsClassAnnotationValue() {
        SeleniumParallel parallel = new SeleniumParallel();
        assertEquals(parallel.getDescription(DescriptionFixture.class),
                "class level description");
    }

    @Test
    public void getDescriptionTrimsMethodAnnotationValue() throws Exception {
        SeleniumParallel parallel = new SeleniumParallel();
        assertEquals(parallel.getDescription(method(DescriptionFixture.class, "described")),
                "method level description");
    }

    @Test
    public void getDescriptionIsNullWhenAnnotationAbsent() throws Exception {
        SeleniumParallel parallel = new SeleniumParallel();
        assertNull(parallel.getDescription(UndescribedFixture.class));
        assertNull(parallel.getDescription(method(DescriptionFixture.class, "notDescribed")));
    }

    // ---------- withBoldHTML ----------

    @Test
    public void withBoldHtmlWrapsNonBlankText() {
        SeleniumParallel parallel = new SeleniumParallel();
        assertEquals(parallel.withBoldHTML("hello"), "<b>hello</b>");
    }

    @Test
    public void withBoldHtmlDropsBlankText() {
        SeleniumParallel parallel = new SeleniumParallel();
        assertEquals(parallel.withBoldHTML("   "), "");
    }

    // ---------- getAuthors (deprecated delegate) ----------

    @Test
    @SuppressWarnings("deprecation")
    public void getAuthorsDelegatesToAuthorUtils() throws Exception {
        SeleniumParallel parallel = new SeleniumParallel();
        Method method = AuthorFixture.class.getDeclaredMethod("methodWithBoth");
        assertEquals(parallel.getAuthors(AuthorFixture.class.getName(), method),
                AuthorUtils.getMethodAuthors(method));
        assertFalse(parallel.getAuthors(AuthorFixture.class.getName(), method).isEmpty());
    }
}
