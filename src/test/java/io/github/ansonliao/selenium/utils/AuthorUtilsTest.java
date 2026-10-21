package io.github.ansonliao.selenium.utils;

import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AuthorUtilsTest {

    @Test
    public void shouldMergeMethodAnnotationAndJavadocAuthors()
            throws NoSuchMethodException {
        Method method = AuthorFixture.class.getMethod("methodWithBoth");
        List<String> authors = AuthorUtils.getMethodAuthors(method);
        assertTrue(authors.contains("MethodAnnotationAuthor"));
        assertTrue(authors.contains("MethodJavadocAuthor"));
        assertFalse(authors.contains("ClassAnnotationAuthor"));
    }

    @Test
    public void shouldFallBackToClassAuthors() throws NoSuchMethodException {
        Method method = AuthorFixture.class.getMethod("methodWithClassFallback");
        List<String> authors = AuthorUtils.getMethodAuthors(method);
        assertTrue(authors.contains("ClassAnnotationAuthor"));
        assertTrue(authors.contains("ClassJavadocAuthor"));
    }

    @Test
    public void shouldReturnEmptyAuthorsForUnknownSource()
            throws NoSuchMethodException {
        Method method = String.class.getMethod("length");
        assertTrue(AuthorUtils.getMethodAuthors(method).isEmpty());
    }

    @Test
    public void shouldReturnEmptyOptionalWhenClassHasNoAuthors() {
        assertTrue(AuthorUtils.getClassAuthors(AuthorUtilsTest.class).isEmpty());
    }
}
