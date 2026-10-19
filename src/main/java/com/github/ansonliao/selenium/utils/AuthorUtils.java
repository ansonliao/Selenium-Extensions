package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.annotations.Author;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves test authors from the {@link Author} annotation and from Javadoc
 * {@code @author} tags.
 *
 * <p>The source tree is parsed lazily, at most once. If it is unavailable
 * (for example when tests run from a packaged jar), annotation values are
 * still returned.</p>
 */
public final class AuthorUtils {

    private static final Logger logger = LoggerFactory.getLogger(AuthorUtils.class);
    private static final File SOURCE_ROOT =
            new File(System.getProperty("user.dir"), "src");
    private static final Object QDOX_LOCK = new Object();

    private AuthorUtils() {
    }

    /**
     * Returns the authors of a test method. Method-level authors (annotation
     * and Javadoc) take precedence; class-level authors are used as a fallback.
     * Duplicates are removed while preserving discovery order.
     */
    public static List<String> getMethodAuthors(Method method) {
        Set<String> authors = new LinkedHashSet<>();
        addAnnotationAuthors(method, authors);
        addJavadocMethodAuthors(method, authors);

        if (authors.isEmpty()) {
            addAnnotationAuthors(method.getDeclaringClass(), authors);
            addJavadocClassAuthors(method.getDeclaringClass(), authors);
        }
        return new ArrayList<>(authors);
    }

    /**
     * Returns the authors declared on a class, or {@link Optional#empty()}
     * when neither the annotation nor Javadoc declares one.
     */
    public static Optional<List<String>> getClassAuthors(Class<?> clazz) {
        Set<String> authors = new LinkedHashSet<>();
        addAnnotationAuthors(clazz, authors);
        addJavadocClassAuthors(clazz, authors);
        return authors.isEmpty()
                ? Optional.empty()
                : Optional.of(new ArrayList<>(authors));
    }

    private static void addAnnotationAuthors(Method method, Set<String> authors) {
        addAnnotationAuthors(method.getAnnotation(Author.class), authors);
    }

    private static void addAnnotationAuthors(Class<?> clazz, Set<String> authors) {
        addAnnotationAuthors(clazz.getAnnotation(Author.class), authors);
    }

    private static void addAnnotationAuthors(Author author, Set<String> authors) {
        if (author == null) {
            return;
        }
        for (String value : author.value()) {
            addAuthor(authors, value);
        }
    }

    private static void addJavadocMethodAuthors(Method method, Set<String> authors) {
        JavaClass javaClass = findJavaClass(method.getDeclaringClass().getName());
        if (javaClass == null) {
            return;
        }
        javaClass.getMethods().stream()
                .filter(candidate -> candidate.getName().equals(method.getName()))
                .findFirst()
                .ifPresent(candidate -> addAuthorTags(candidate, authors));
    }

    private static void addJavadocClassAuthors(Class<?> clazz, Set<String> authors) {
        JavaClass javaClass = findJavaClass(clazz.getName());
        if (javaClass == null) {
            return;
        }
        javaClass.getTags().stream()
                .filter(AuthorUtils::isAuthorTag)
                .forEach(tag -> addAuthor(authors, tag.getValue()));
    }

    private static void addAuthorTags(JavaMethod method, Set<String> authors) {
        method.getTags().stream()
                .filter(AuthorUtils::isAuthorTag)
                .forEach(tag -> addAuthor(authors, tag.getValue()));
    }

    private static JavaClass findJavaClass(String className) {
        JavaProjectBuilder builder = JavaProjectHolder.INSTANCE;
        if (builder == null) {
            return null;
        }
        synchronized (QDOX_LOCK) {
            try {
                return builder.getClassByName(className);
            } catch (RuntimeException e) {
                logger.warn("Unable to read Javadoc for {}", className, e);
                return null;
            }
        }
    }

    private static boolean isAuthorTag(DocletTag tag) {
        return "author".equals(tag.getName()) || "author:".equals(tag.getName());
    }

    private static void addAuthor(Set<String> authors, String value) {
        if (value == null) {
            return;
        }
        String author = value.trim();
        if (!author.isEmpty()) {
            authors.add(author);
        }
    }

    private static final class JavaProjectHolder {
        private static final JavaProjectBuilder INSTANCE = create();

        private static JavaProjectBuilder create() {
            if (!SOURCE_ROOT.isDirectory()) {
                logger.debug("Source root {} not found; Javadoc authors disabled",
                        SOURCE_ROOT.getAbsolutePath());
                return null;
            }
            try {
                JavaProjectBuilder builder = new JavaProjectBuilder();
                builder.addSourceTree(SOURCE_ROOT);
                return builder;
            } catch (RuntimeException e) {
                logger.warn("Unable to parse sources under {}; Javadoc authors disabled",
                        SOURCE_ROOT.getAbsolutePath(), e);
                return null;
            }
        }
    }
}
