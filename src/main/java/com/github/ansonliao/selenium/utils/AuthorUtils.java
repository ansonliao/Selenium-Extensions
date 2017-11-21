package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.annotations.Author;
import com.github.ansonliao.selenium.internal.Constants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AuthorUtils {
    private static final Logger logger = LoggerFactory.getLogger(AuthorUtils.class);
    private static JavaProjectBuilder javaProjectBuilder;

    static {
        javaProjectBuilder = new JavaProjectBuilder();
        javaProjectBuilder.addSourceTree(new File(
                Constants.PROJECT_ROOT_DIR
                        .concat(Constants.FILE_SEPARATOR)
                        .concat("src")));
    }

    public synchronized static List<String> getMethodAuthors(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        String className = clazz.getName();

        Set<String> classAuthors = Sets.newHashSet();
        Set<String> methodAuthors = Sets.newHashSet();

        // get class authors
        getClassAuthors(clazz)
                .ifPresent(authorList -> classAuthors.addAll(authorList));

        // get method authors
        javaProjectBuilder.getClassByName(className).getMethods().stream()
                .filter(m -> m.getName().equalsIgnoreCase(method.getName())).findAny()
                .ifPresent(m -> m.getTags().stream().filter(filterAuthorTag).findAny()
                        .ifPresent(tag -> methodAuthors.add(tag.getValue().trim())));

        getAuthorsFromAnnotation(method)
                .ifPresent(authorList -> authorList.forEach(methodAuthors::add));

        return methodAuthors.size() > 0
                ? Lists.newArrayList(methodAuthors)
                : Lists.newArrayList(classAuthors);
    }

    public synchronized static Optional<List<String>> getClassAuthors(Class<?> clazz) {
        String className = clazz.getName();
        JavaClass cls = javaProjectBuilder.getClassByName(className);
        List<String> authors = Lists.newArrayList();

        getAuthorsFromAnnotation(clazz)
                .ifPresent(authorList -> authorList.forEach(authors::add));

        cls.getTags().stream()
                .filter(filterAuthorTag).distinct()
                .map(DocletTag::getValue).map(String::trim).map(authors::add)
                .distinct().collect(Collectors.toList());

        return Optional.of(authors);
    }

    private static Predicate<DocletTag> filterAuthorTag = tag ->
            tag.getName().equals("author") || tag.getName().equals("author:");

    private synchronized static Optional<List<String>> getAuthorsFromAnnotation(Object obj) {
        Author author = obj instanceof Class<?>
                ? (Author) ((Class) obj).getAnnotation(Author.class)
                : (Author) ((Method) obj).getAnnotation(Author.class);

        if (author == null || author.value().length == 0) {
            return Optional.empty();
        }

        return Optional.of(Lists.newArrayList(author.value())
                .stream().distinct().collect(Collectors.toList()));
    }

}
