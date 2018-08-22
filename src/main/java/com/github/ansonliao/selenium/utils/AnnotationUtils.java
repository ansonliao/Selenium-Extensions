package com.github.ansonliao.selenium.utils;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

public class AnnotationUtils {
    private static Logger logger = LoggerFactory.getLogger(AnnotationUtils.class);

    public static synchronized Set<Annotation> getClassAnnotations(Class clazz) {
        return Sets.newHashSet(clazz.getAnnotations());
    }

    public static synchronized Set<Annotation> getMethodAnnotations(Method method) {
        return Sets.newHashSet(method.getAnnotations());
    }
}
