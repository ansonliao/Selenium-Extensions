package com.github.ansonliao.selenium.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class TestGroupUtils {
    private static final Logger logger =
            LoggerFactory.getLogger(TestGroupUtils.class);

    public static List<String> getClassTestGroups(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Test.class)) {
            return Collections.emptyList();
        }

        Test annotation = clazz.getAnnotation(Test.class);
        if (annotation.groups().length == 0) {
            return Collections.emptyList();
        }

        return Lists.newArrayList(annotation.groups())
                .stream().collect(toSet())
                .stream().collect(toList());
    }

    public static List<String> getMethodTestGroups(Method method) {
        if (!getConfigInstance().testingTestGroups().isEmpty()) {
            return getConfigInstance().testingTestGroups().parallelStream()
                    .map(StringUtils::removeQuoteMark)
                    .collect(toList());
        }

        Set<String> classTestGroups =
                getClassTestGroups(method.getDeclaringClass())
                        .parallelStream().collect(toSet());

        if (!method.isAnnotationPresent(Test.class)) {
            return classTestGroups.parallelStream().collect(toList());
        }

        Test annotation = method.getAnnotation(Test.class);
        if (annotation.groups().length == 0) {
            return classTestGroups.parallelStream().collect(toList());
        }

        return Sets.union(
                classTestGroups,
                Lists.newArrayList(annotation.groups())
                        .parallelStream().collect(toSet()))
                .parallelStream().collect(toList());
    }

}
