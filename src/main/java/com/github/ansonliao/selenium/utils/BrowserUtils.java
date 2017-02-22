package com.github.ansonliao.selenium.utils;


import com.github.ansonliao.selenium.annotations.*;
import com.github.ansonliao.selenium.internal.platform.Platform;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ansonliao on 20/2/2017.
 */
public class BrowserUtils {

    public static synchronized Set<Platform.BrowserType> getClassSupportedBrowsers(Class clazz) {
        Set<Platform.BrowserType> enabledBrowsers = getClassBrowsers(clazz);
        Set<Platform.BrowserType> ignoredBrowsers = getClassIgnoredBrowsers(clazz);
        Set<Platform.BrowserType> s1 = new HashSet<>();
        s1.addAll(enabledBrowsers);
        s1.retainAll(ignoredBrowsers);
        enabledBrowsers.removeAll(s1);
        return enabledBrowsers;
    }

    public static synchronized Set<Platform.BrowserType> getClassBrowsers(Class clazz) {
        Set<Annotation> annotationSet = new HashSet<>(Arrays.asList(clazz.getAnnotations()));
        Set<Class<? extends Annotation>> annotations = new HashSet<>();
        annotationSet.forEach(annotation -> annotations.add(annotation.annotationType()));
        Set<Platform.BrowserType> browserTypes = new HashSet<>();
        if (annotations.contains(Chrome.class)) {
            browserTypes.add(Platform.BrowserType.CHROME);
        }
        if (annotations.contains(Firefox.class)) {
            browserTypes.add(Platform.BrowserType.FIREFOX);
        }
        if (annotations.contains(Edge.class)) {
            browserTypes.add(Platform.BrowserType.Edge);
        }
        if (annotations.contains(InternetExplorer.class)) {
            browserTypes.add(Platform.BrowserType.InternetExplorer);
        }

        return browserTypes;
    }

    public static Set<Platform.BrowserType> getClassIgnoredBrowsers(Class clazz) {
        Set<Annotation> annotationSet = new HashSet<>(Arrays.asList(clazz.getAnnotations()));
        Set<Class<? extends Annotation>> annotations = new HashSet<>();
        annotationSet.forEach(annotation -> annotations.add(annotation.annotationType()));
        Set<Platform.BrowserType> IgnoreBrowserTypes = new HashSet<>();
        if (annotations.contains(IgnoreChrome.class)) {
            IgnoreBrowserTypes.add(Platform.BrowserType.CHROME);
        }
        if (annotations.contains(IgnoreFirefox.class)) {
            IgnoreBrowserTypes.add(Platform.BrowserType.FIREFOX);
        }
        if (annotations.contains(IgnoreEdge.class)) {
            IgnoreBrowserTypes.add(Platform.BrowserType.Edge);
        }
        if (annotations.contains(IgnoreInternetExplorer.class)) {
            IgnoreBrowserTypes.add(Platform.BrowserType.InternetExplorer);
        }

        return IgnoreBrowserTypes;
    }

    public static Set<Platform.BrowserType> getMethodSupportedBrowsers(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        return getMethodSupportedBrowsers(clazz, method);
    }

    public static Set<Platform.BrowserType> getMethodSupportedBrowsers(Class<?> clazz, Method method) {
        Set<Platform.BrowserType> classSupportedBrowsers = getClassSupportedBrowsers(clazz);
        Set<Platform.BrowserType> classIgnoredBrowsers = getClassIgnoredBrowsers(clazz);
        Set<Platform.BrowserType> enabledBrowsers = getMethodBrowsers(method);
        Set<Platform.BrowserType> ignoredBrowsers = getMethodIgnoredBrowsers(method);
        Set<Platform.BrowserType> s1 = new HashSet<>();
        enabledBrowsers.addAll(classSupportedBrowsers);
        ignoredBrowsers.addAll(classIgnoredBrowsers);
        s1.addAll(enabledBrowsers);
        s1.retainAll(ignoredBrowsers);
        enabledBrowsers.removeAll(s1);
        return enabledBrowsers;
    }

    public static Set<Platform.BrowserType> getMethodBrowsers(Method method) {
        Set<Annotation> annotationSet = new HashSet<>(Arrays.asList(method.getAnnotations()));
        Set<Class<? extends Annotation>> annotations = new HashSet<>();
        Set<Platform.BrowserType> browserTypes = new HashSet<>();
        annotationSet.forEach(annotation -> annotations.add(annotation.annotationType()));
        if (annotations.contains(Chrome.class)) {
            browserTypes.add(Platform.BrowserType.CHROME);
        }
        if (annotations.contains(Firefox.class)) {
            browserTypes.add(Platform.BrowserType.FIREFOX);
        }
        if (annotations.contains(Edge.class)) {
            browserTypes.add(Platform.BrowserType.Edge);
        }
        if (annotations.contains(InternetExplorer.class)) {
            browserTypes.add(Platform.BrowserType.InternetExplorer);
        }

        return browserTypes;
    }

    public static Set<Platform.BrowserType> getMethodIgnoredBrowsers(Method method) {
        Set<Annotation> annotationSet = new HashSet<>(Arrays.asList(method.getAnnotations()));
        Set<Class<? extends Annotation>> annotations = new HashSet<>();
        Set<Platform.BrowserType> ignoredBrowserTypes = new HashSet<>();
        annotationSet.forEach(annotation -> annotations.add(annotation.annotationType()));
        if (annotations.contains(IgnoreChrome.class)) {
            ignoredBrowserTypes.add(Platform.BrowserType.CHROME);
        }
        if (annotations.contains(IgnoreFirefox.class)) {
            ignoredBrowserTypes.add(Platform.BrowserType.FIREFOX);
        }
        if (annotations.contains(IgnoreEdge.class)) {
            ignoredBrowserTypes.add(Platform.BrowserType.Edge);
        }
        if (annotations.contains(IgnoreInternetExplorer.class)) {
            ignoredBrowserTypes.add(Platform.BrowserType.InternetExplorer);
        }

        return ignoredBrowserTypes;
    }
}
