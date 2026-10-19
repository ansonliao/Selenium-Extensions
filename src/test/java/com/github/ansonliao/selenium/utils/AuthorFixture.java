package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.annotations.Author;

/**
 * Fixture used by {@link AuthorUtilsTest}.
 *
 * @author ClassJavadocAuthor
 */
@Author({"ClassAnnotationAuthor"})
public class AuthorFixture {

    /**
     * Method with both annotation and Javadoc authors.
     *
     * @author MethodJavadocAuthor
     */
    @Author({"MethodAnnotationAuthor"})
    public void methodWithBoth() {
    }

    /**
     * Method without its own authors, so the class authors are used.
     */
    public void methodWithClassFallback() {
    }
}
