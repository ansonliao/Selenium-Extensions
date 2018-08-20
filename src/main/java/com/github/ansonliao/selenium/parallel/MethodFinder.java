package com.github.ansonliao.selenium.parallel;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MethodFinder {
    private static Logger logger = LoggerFactory.getLogger(MethodFinder.class);

    public static List<Method> findAllAnnotatedTestMethodInClass(
            Class clazz) {
        logger.info("Find all @Test method in class: " + clazz.getName());
        return findMethodInClass(clazz).stream().distinct()
                .filter(method -> method.isAnnotationPresent(Test.class))
                .collect(toList());
    }

    public static List<Method> findMethodInClass(Class clazz) {
        logger.info("Find all declared methods in class: " + clazz.getName());
        return Lists.newArrayList(clazz.getDeclaredMethods()).parallelStream()
                .distinct().collect(toList());
    }

    public static List<Method> findTestNGMethodInClassByGroup(
            Class clazz, String groupName) {
        List<Method> methodList = Lists.newArrayList();

        if (clazz.isAnnotationPresent(Test.class)) {
            Test t = (Test) clazz.getAnnotation(Test.class);
            methodList.addAll(
                    Lists.newArrayList(
                            clazz.getDeclaredMethods()));

            if (t.groups().length > 0) {
                List<String> groups = Lists.newArrayList(t.groups());
                if (groups.contains(groupName)) {
                    return Lists.newArrayList(
                            clazz.getDeclaredMethods());
                }
            }
        }

        return Lists.newArrayList(clazz.getDeclaredMethods())
                .parallelStream()
                .filter(m -> m.isAnnotationPresent(Test.class))
                .filter(m ->
                        Lists.newArrayList(
                                m.getAnnotation(Test.class).groups())
                                .contains(groupName))
                .collect(toList());
    }
}
