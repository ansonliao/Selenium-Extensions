package com.github.ansonliao.selenium.utils;

import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AnnotationUtils {
    private static Logger logger = Logger.getLogger(AnnotationUtils.class);

    public static synchronized Set<Annotation> getClassAnnotations(Class clazz) {
        return new HashSet<>(Arrays.asList(clazz.getAnnotations()));
    }

    public static synchronized Set<Annotation> getMethodAnnotations(Method method) {
        return new HashSet<>(Arrays.asList(method.getAnnotations()));
    }
}
