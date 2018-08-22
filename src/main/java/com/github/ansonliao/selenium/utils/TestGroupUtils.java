package com.github.ansonliao.selenium.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class TestGroupUtils {
    private static final Logger logger =
            LoggerFactory.getLogger(TestGroupUtils.class);

    public static List<String> getClassTestGroups(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Test.class)) {
            return emptyList();
        }

        Test annotation = clazz.getAnnotation(Test.class);
        if (annotation.groups().length == 0) {
            return emptyList();
        }

        return Arrays.stream(annotation.groups()).distinct().collect(toList());
    }

    public static List<String> getMethodTestGroups(Method method) {
        if (!getConfigInstance().testingTestGroups().isEmpty()) {
            return getConfigInstance().testingTestGroups().parallelStream()
                    .map(StringUtils::removeQuoteMark)
                    .distinct().collect(toList());
        }

        Set<String> classTestGroups = Sets.newHashSet(getClassTestGroups(method.getDeclaringClass()));

        if (!method.isAnnotationPresent(Test.class)) {
            return Lists.newArrayList(classTestGroups);
        }

        Test annotation = method.getAnnotation(Test.class);
        if (annotation.groups().length == 0) {
            return Lists.newArrayList(classTestGroups);
        }

        return Sets.union(classTestGroups, Arrays.stream(annotation.groups()).collect(toSet()))
                .parallelStream().collect(toList());
    }

}
