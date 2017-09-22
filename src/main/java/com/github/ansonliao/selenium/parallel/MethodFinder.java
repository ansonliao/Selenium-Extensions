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
}
