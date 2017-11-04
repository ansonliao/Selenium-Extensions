package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.parallel.ClassFinder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BrowserUtils {
    private static Logger logger = LoggerFactory.getLogger(BrowserUtils.class);

    public static List<String> getSupportedBrowsers() {
        return ClassFinder.getScanAllClassesResult()
                .getNamesOfAllClasses()
                .parallelStream()
                .filter(className ->
                        className.startsWith(Constants.BROWSER_ANNOTATION_PACKAGE))
                .map(ClassFinder::createClass)
                .filter(aClass -> aClass.isAnnotation())
                .map(aClass -> aClass.getSimpleName().toUpperCase())
                .filter(className ->
                        !className.startsWith(Constants.BROWSER_IGNORE_ANNOTATION_PREFIX))
                .collect(Collectors.toList());
    }

    public static List<String> getSupportedIgnoreBrowsers() {
        return ClassFinder.getScanAllClassesResult()
                .getNamesOfAllClasses()
                .parallelStream()
                .filter(className ->
                        className.startsWith(Constants.BROWSER_ANNOTATION_PACKAGE))
                .map(ClassFinder::createClass)
                .filter(aClass -> aClass.isAnnotation())
                .map(aClass -> aClass.getSimpleName().toUpperCase())
                .filter(className ->
                        className.startsWith(Constants.BROWSER_IGNORE_ANNOTATION_PREFIX))
                .map(className ->
                        className.substring(Constants.BROWSER_IGNORE_ANNOTATION_PREFIX.length()))
                .collect(Collectors.toList());
    }

    public static synchronized Set<String> getClassSupportedBrowsers(Class clazz) {
        Set<String> enabledBrowsers = getClassBrowsers(clazz);
        Set<String> ignoredBrowsers = getClassIgnoredBrowsers(clazz);
        return Sets.difference(
                enabledBrowsers,
                Sets.intersection(enabledBrowsers, ignoredBrowsers));
    }

    public static synchronized Set<String> getClassBrowsers(Class clazz) {
        Set<String> classBrowserList = filterBrowsersFromAnnotations(
                Lists.newArrayList(clazz.getAnnotations()));
        return Sets.intersection(
                classBrowserList,
                Sets.newHashSet(getSupportedBrowsers()));
    }

    public static Set<String> getClassIgnoredBrowsers(Class clazz) {
        return filterBrowsersIgnoredFromAnnotations(
                Lists.newArrayList(clazz.getAnnotations()));

    }

    public static Set<String> getMethodSupportedBrowsers(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        return getMethodSupportedBrowsers(clazz, method);
    }

    public static Set<String> getMethodSupportedBrowsers(Class<?> clazz, Method method) {
        Set<String> classSupportedBrowsers = getClassBrowsers(clazz);
        Set<String> classIgnoredBrowsers = getClassIgnoredBrowsers(clazz);
        Set<String> enabledBrowsers = getMethodBrowsers(method);
        Set<String> ignoredBrowsers = getMethodIgnoredBrowsers(method);

        enabledBrowsers = Sets.union(classSupportedBrowsers, enabledBrowsers);
        ignoredBrowsers = Sets.union(classIgnoredBrowsers, ignoredBrowsers);

        return Sets.difference(
                enabledBrowsers,
                Sets.intersection(enabledBrowsers, ignoredBrowsers))
                .parallelStream()
                .collect(Collectors.toSet());
    }

    public static Set<String> getMethodBrowsers(Method method) {
        Set<String> methodBrowserList = filterBrowsersFromAnnotations(
                Lists.newArrayList(method.getAnnotations()));
        return Sets.intersection(
                methodBrowserList,
                Sets.newHashSet(getSupportedBrowsers()))
                .parallelStream().collect(Collectors.toSet());
    }

    public static Set<String> getMethodIgnoredBrowsers(Method method) {
        return filterBrowsersIgnoredFromAnnotations(
                Lists.newArrayList(method.getAnnotations()));
    }

    public synchronized static Set<String> filterBrowsersFromAnnotations(List<Annotation> annotations) {
        return annotations.parallelStream()
                .map(annotation ->
                        annotation.annotationType().getSimpleName().toUpperCase())
                .filter(className ->
                        (!className.equals("TEST")
                                && !className.equals("INCOGNITO")
                                && !className.equals("HEADLESS")))
                .collect(Collectors.toSet());
    }

    public synchronized static Set<String> filterBrowsersIgnoredFromAnnotations(
            List<Annotation> annotations) {
        return annotations.parallelStream()
                .map(annotation ->
                        annotation.annotationType().getSimpleName().toUpperCase())
                .filter(className ->
                        className.startsWith(Constants.BROWSER_IGNORE_ANNOTATION_PREFIX))
                .map(className ->
                        className.substring(Constants.BROWSER_IGNORE_ANNOTATION_PREFIX.length()))
                .collect(Collectors.toSet());
    }
}
