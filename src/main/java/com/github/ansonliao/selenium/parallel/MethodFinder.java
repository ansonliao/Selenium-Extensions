package com.github.ansonliao.selenium.parallel;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;


public class MethodFinder {
    private static Logger logger = Logger.getLogger(MethodFinder.class);

    public static List<Method> findAllAnnotatedTestMethodInClass(Class clazz) {
        logger.info("Find all @Test method in class: " + clazz.getName());
        return findMethodInClass(clazz).stream().distinct()
                .filter(method -> method.isAnnotationPresent(Test.class))
                .collect(Collectors.toList());
    }

    public static List<Method> findMethodInClass(Class clazz) {
        logger.info("Find all declared methods in class: " + clazz.getName());
        return Lists.newArrayList(clazz.getDeclaredMethods()).stream()
                .distinct().collect(Collectors.toList());
    }

    public static List<Method> findTestNGMethodInClassByGroup(Class clazz, String groupName) {
        List<Method> methodList = Lists.newArrayList();

        if (clazz.isAnnotationPresent(Test.class)) {
            Test t = (Test) clazz.getAnnotation(Test.class);
            if (t.groups().length > 0) {
                List<String> groups = Lists.newArrayList(t.groups());
                if (groups.contains(groupName)) {
                    Lists.newArrayList(clazz.getDeclaredMethods())
                            .stream().filter(method -> method.isAnnotationPresent(Test.class))
                            .forEach(method -> methodList.add(method));
                    return methodList;
                }
            }
        } else {
            return Lists.newArrayList(clazz.getDeclaredMethods())
                    .stream()
                    .filter(m -> m.isAnnotationPresent(Test.class))
                    .filter(m ->
                            Lists.newArrayList(m.getAnnotation(Test.class).groups()).contains(groupName))
                    .collect(Collectors.toList());
        }

        return methodList;
    }
}
