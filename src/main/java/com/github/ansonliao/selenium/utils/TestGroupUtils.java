package com.github.ansonliao.selenium.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestGroupUtils {
    private static final Logger logger = Logger.getLogger(TestGroupUtils.class);

    public static List<String> getClassTestGroups(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Test.class)) {
            return Collections.emptyList();
        }

        Test annotation = clazz.getAnnotation(Test.class);
        if (annotation.groups().length == 0) {
            return Collections.emptyList();
        }

        return Lists.newArrayList(annotation.groups())
                .stream().collect(Collectors.toSet())
                .stream().collect(Collectors.toList());
    }

    public static List<String> getMethodTestGroups(Method method) {
        Set<String> classTestGroups =
                getClassTestGroups(method.getDeclaringClass())
                        .parallelStream().collect(Collectors.toSet());

        if (!method.isAnnotationPresent(Test.class)) {
            return classTestGroups.parallelStream().collect(Collectors.toList());
        }

        Test annotation = method.getAnnotation(Test.class);
        if (annotation.groups().length == 0) {
            return classTestGroups.parallelStream().collect(Collectors.toList());
        }

        return Sets.union(
                classTestGroups,
                Lists.newArrayList(annotation.groups())
                        .parallelStream().collect(Collectors.toSet()))
                .parallelStream().collect(Collectors.toList());
    }

}
