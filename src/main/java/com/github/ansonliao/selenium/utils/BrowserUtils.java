package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.annotations.Chrome;
import com.github.ansonliao.selenium.annotations.Edge;
import com.github.ansonliao.selenium.annotations.Firefox;
import com.github.ansonliao.selenium.annotations.IgnoreChrome;
import com.github.ansonliao.selenium.annotations.IgnoreEdge;
import com.github.ansonliao.selenium.annotations.IgnoreFirefox;
import com.github.ansonliao.selenium.annotations.IgnoreInternetExplorer;
import com.github.ansonliao.selenium.annotations.InternetExplorer;
import com.github.ansonliao.selenium.internal.platform.Browser;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class BrowserUtils {
    private static Logger logger = Logger.getLogger(BrowserUtils.class);

    public static synchronized Set<Browser> getClassSupportedBrowsers(Class clazz) {
        Set<Browser> enabledBrowsers = getClassBrowsers(clazz);
        Set<Browser> ignoredBrowsers = getClassIgnoredBrowsers(clazz);
        Set<Browser> s1 = new HashSet<>();
        s1.addAll(enabledBrowsers);
        s1.retainAll(ignoredBrowsers);
        enabledBrowsers.removeAll(s1);
        return enabledBrowsers;
    }

    public static synchronized Set<Browser> getClassBrowsers(Class clazz) {
        Set<Annotation> annotationSet = new HashSet<>(Arrays.asList(clazz.getAnnotations()));
        Set<Class<? extends Annotation>> annotations = new HashSet<>();
        annotationSet.forEach(annotation -> annotations.add(annotation.annotationType()));
        Set<Browser> browserTypes = new HashSet<>();
        if (annotations.contains(Chrome.class)) {
            browserTypes.add(Browser.CHROME);
        }
        if (annotations.contains(Firefox.class)) {
            browserTypes.add(Browser.FIREFOX);
        }
        if (annotations.contains(Edge.class)) {
            browserTypes.add(Browser.Edge);
        }
        if (annotations.contains(InternetExplorer.class)) {
            browserTypes.add(Browser.InternetExplorer);
        }

        return browserTypes;
    }

    public static Set<Browser> getClassIgnoredBrowsers(Class clazz) {
        Set<Annotation> annotationSet = new HashSet<>(Arrays.asList(clazz.getAnnotations()));
        Set<Class<? extends Annotation>> annotations = new HashSet<>();
        annotationSet.forEach(annotation -> annotations.add(annotation.annotationType()));
        Set<Browser> IgnoreBrowserTypes = new HashSet<>();
        if (annotations.contains(IgnoreChrome.class)) {
            IgnoreBrowserTypes.add(Browser.CHROME);
        }
        if (annotations.contains(IgnoreFirefox.class)) {
            IgnoreBrowserTypes.add(Browser.FIREFOX);
        }
        if (annotations.contains(IgnoreEdge.class)) {
            IgnoreBrowserTypes.add(Browser.Edge);
        }
        if (annotations.contains(IgnoreInternetExplorer.class)) {
            IgnoreBrowserTypes.add(Browser.InternetExplorer);
        }

        return IgnoreBrowserTypes;
    }

    public static Set<Browser> getMethodSupportedBrowsers(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        return getMethodSupportedBrowsers(clazz, method);
    }

    public static Set<Browser> getMethodSupportedBrowsers(Class<?> clazz, Method method) {
        Set<Browser> classSupportedBrowsers = getClassSupportedBrowsers(clazz);
        Set<Browser> classIgnoredBrowsers = getClassIgnoredBrowsers(clazz);
        Set<Browser> enabledBrowsers = getMethodBrowsers(method);
        Set<Browser> ignoredBrowsers = getMethodIgnoredBrowsers(method);
        Set<Browser> s1 = new HashSet<>();
        enabledBrowsers.addAll(classSupportedBrowsers);
        ignoredBrowsers.addAll(classIgnoredBrowsers);
        s1.addAll(enabledBrowsers);
        s1.retainAll(ignoredBrowsers);
        enabledBrowsers.removeAll(s1);
        return enabledBrowsers;
    }

    public static Set<Browser> getMethodBrowsers(Method method) {
        Set<Annotation> annotationSet = new HashSet<>(Arrays.asList(method.getAnnotations()));
        Set<Class<? extends Annotation>> annotations = new HashSet<>();
        Set<Browser> browserTypes = new HashSet<>();
        annotationSet.forEach(annotation -> annotations.add(annotation.annotationType()));
        if (annotations.contains(Chrome.class)) {
            browserTypes.add(Browser.CHROME);
        }
        if (annotations.contains(Firefox.class)) {
            browserTypes.add(Browser.FIREFOX);
        }
        if (annotations.contains(Edge.class)) {
            browserTypes.add(Browser.Edge);
        }
        if (annotations.contains(InternetExplorer.class)) {
            browserTypes.add(Browser.InternetExplorer);
        }

        return browserTypes;
    }

    public static Set<Browser> getMethodIgnoredBrowsers(Method method) {
        Set<Annotation> annotationSet = new HashSet<>(Arrays.asList(method.getAnnotations()));
        Set<Class<? extends Annotation>> annotations = new HashSet<>();
        Set<Browser> ignoredBrowserTypes = new HashSet<>();
        annotationSet.forEach(annotation -> annotations.add(annotation.annotationType()));
        if (annotations.contains(IgnoreChrome.class)) {
            ignoredBrowserTypes.add(Browser.CHROME);
        }
        if (annotations.contains(IgnoreFirefox.class)) {
            ignoredBrowserTypes.add(Browser.FIREFOX);
        }
        if (annotations.contains(IgnoreEdge.class)) {
            ignoredBrowserTypes.add(Browser.Edge);
        }
        if (annotations.contains(IgnoreInternetExplorer.class)) {
            ignoredBrowserTypes.add(Browser.InternetExplorer);
        }

        return ignoredBrowserTypes;
    }

    public synchronized static Browser getBrowserByString(Optional<String> optBrowser) {
        String browserName = optBrowser.orElse("CHROME");

        if (browserName.equalsIgnoreCase(Browser.CHROME.getName())) {
            return Browser.CHROME;
        } else if (browserName.equalsIgnoreCase(Browser.FIREFOX.getName())) {
            return Browser.FIREFOX;
        } else if (browserName.equalsIgnoreCase(Browser.Edge.getName())) {
            return Browser.Edge;
        } else {
            return Browser.InternetExplorer;
        }
    }
}
