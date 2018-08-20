package com.github.ansonliao.selenium.testng;

import com.github.ansonliao.selenium.parallel.ClassFinder;
import com.github.ansonliao.selenium.parallel.MethodFinder;
import com.github.ansonliao.selenium.utils.BrowserUtils;
import com.github.ansonliao.selenium.utils.StringUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.ansonliao.selenium.utils.StringUtils.removeQuoteMark;
import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class TestNGFilter {
    private static final Logger logger = LoggerFactory.getLogger(TestNGFilter.class);
    private static List<Class<?>> testngClasses = Lists.newArrayList();
    private static Multimap<Class<?>, Method> testNGClass2MethodMap = HashMultimap.create();

    static {
        String[] testingPackageNames = getConfigInstance().testingPackageNames().parallelStream()
                .map(StringUtils::removeQuoteMark).collect(toList())
                .toArray(new String[getConfigInstance().testingPackageNames().size()]);
        testngClasses = ClassFinder.findAllTestNGTestClasses(testingPackageNames);
    }

    public static Map<String, Map<Class<?>, List<Method>>> run() {
        filterClassesByTestingClassList();
        filterTestNGMethodByTestGroups();
        filterTestNGMethodByBrowsers();
        return getBrowser2TestNGClass2TestNGMethodMap();
    }

    // filter testng classes by given testng class list
    public static List<Class<?>> filterClassesByTestingClassList() {
        if (!getConfigInstance().testingTestNGClasses().isEmpty()) {
            testngClasses = testngClasses.parallelStream()
                    .filter(aClass ->
                            getConfigInstance().testingTestNGClasses()
                                    .parallelStream()
                                    .map(StringUtils::removeQuoteMark)
                                    .map(String::toUpperCase)
                                    .collect(toList())
                                    .contains(aClass.getCanonicalName().toUpperCase()))
                    .distinct()
                    .collect(toList());
            return testngClasses;
        }
        return testngClasses;
    }

    /**
     * @return A map that key = testng class, value = list of testng method of the testng class
     */
    public static Multimap<Class<?>, Method> filterTestNGMethodByTestGroups() {
        if (getConfigInstance().testingTestGroups().isEmpty()) {
            testngClasses.stream()
                    .forEach(aClass ->
                            MethodFinder.findAllAnnotatedTestMethodInClass(aClass).stream()
                                    .filter(method -> method.getAnnotation(Test.class).enabled())
                                    .forEach(method -> testNGClass2MethodMap.put(aClass, method)));

            return testNGClass2MethodMap;
        }

        testngClasses.stream().forEach(aClass -> {
            List<Method> methodList = getConfigInstance().testingTestGroups()
                    .parallelStream()
                    .map(StringUtils::removeQuoteMark)
                    .map(group -> MethodFinder.findTestNGMethodInClassByGroup(aClass, group))
                    .flatMap(Collection::stream)
                    .distinct()
                    .filter(method -> method.getAnnotation(Test.class).enabled())
                    .collect(toList());
            if (methodList.size() > 0) {
                methodList.forEach(method -> testNGClass2MethodMap.put(aClass, method));
            }
        });

        return testNGClass2MethodMap;
    }

    // filter testng method by given test browser list
    public static Multimap<Class<?>, Method> filterTestNGMethodByBrowsers() {
        if (!getConfigInstance().testingBrowserNames().isEmpty()) {
            logger.info("Detected testing browser filter to: {}", getConfigInstance().testingBrowserNames());
            Set<String> browserFilters = getConfigInstance().testingBrowserNames()
                    .parallelStream().map(String::toUpperCase).map(StringUtils::removeQuoteMark).collect(toSet());
            Multimap<Class<?>, Method> resultMap = HashMultimap.create(testNGClass2MethodMap);
            testNGClass2MethodMap.keySet().stream().forEach(aClass ->
                    testNGClass2MethodMap.get(aClass).stream().forEach(method -> {
                        Sets.SetView result =
                                Sets.intersection(BrowserUtils.getMethodSupportedBrowsers(method), browserFilters);
                        if (result.size() == 0) {
                            if (testNGClass2MethodMap.get(aClass).contains(method)) {
                                resultMap.get(aClass).remove(method);
                            }
                        }
                    }));
            testNGClass2MethodMap.clear();
            testNGClass2MethodMap = HashMultimap.create(resultMap);
            return resultMap;
        }
        return testNGClass2MethodMap;
    }

    public static Map<String, Map<Class<?>, List<Method>>> getBrowser2TestNGClass2TestNGMethodMap() {
        Map<String, Map<Class<?>, List<Method>>> browserTestingMap = new HashMap<>();
        Multimap<String, Method> browserTestngMethodMap = HashMultimap.create();

        if (!getConfigInstance().runByBrowsers().isEmpty()) {
            // only fetch support browsers
            List<String> browsers =
                    Sets.intersection(
                            getConfigInstance().runByBrowsers().parallelStream()
                                    .map(StringUtils::removeQuoteMark)
                                    .map(String::toUpperCase).collect(toSet()),
                            Sets.newHashSet(BrowserUtils.getSupportedBrowsers()))
                            .parallelStream().collect(toList());

            logger.info("Run Tests by browsers: {}", browsers);

            if (browsers.isEmpty()) {
                return browserTestingMap;
            }

            browsers.parallelStream().map(String::trim).forEach(browserName ->
                    testNGClass2MethodMap.keySet().forEach(aClass ->
                            testNGClass2MethodMap.get(aClass).forEach(method -> {
                                Set<String> ignoreBrowsers = BrowserUtils.getMethodIgnoredBrowsers(method);
                                if (ignoreBrowsers.isEmpty() || !ignoreBrowsers.contains(browserName)) {
                                    if (!browserTestingMap.containsKey(browserName)) {
                                        browserTestingMap.put(browserName, new HashMap<>());
                                    }
                                    if (!browserTestingMap.get(browserName).containsKey(aClass)) {
                                        browserTestingMap.get(browserName).put(aClass, new ArrayList<>());
                                    }
                                    if (!browserTestingMap.get(browserName).get(aClass).contains(method)) {
                                        browserTestingMap.get(browserName).get(aClass).add(method);
                                    }
                                } else {
                                    String msg = "Class: [{}], Method: [{}] ignore browser [{}]"
                                            + " execute as @Ignore{} found";
                                    logger.info(msg,
                                            aClass.getCanonicalName(), method.getName(),
                                            browserName,
                                            browserName.substring(0, 1).toUpperCase()
                                                    + browserName.substring(1).toLowerCase());
                                }
                            })));
            return browserTestingMap;
        }

        testNGClass2MethodMap.keySet().forEach(aClass ->
                testNGClass2MethodMap.get(aClass).forEach(method -> {
                    // add default browser if the testng method without any browser annotations
                    Set<String> methodSupportedBrowsers = addIncludedDefaultBrowser(method);
                    methodSupportedBrowsers.forEach(browserName ->
                            browserTestngMethodMap.put(browserName, method));
                    methodSupportedBrowsers.forEach(browserName -> {
                        if (!browserTestingMap.containsKey(browserName)) {
                            browserTestingMap.put(browserName, new HashMap<>());
                        }
                        if (!browserTestingMap.get(browserName).containsKey(aClass)) {
                            browserTestingMap.get(browserName).put(aClass, new ArrayList<>());
                        }
                        if (!browserTestingMap.get(browserName).get(aClass).contains(method)) {
                            browserTestingMap.get(browserName).get(aClass).add(method);
                        }
                    });
                }));

        logger.info("Run Tests by browsers: {}", browserTestingMap.keySet());
        return browserTestingMap;
    }

    private static Set<String> addIncludedDefaultBrowser(Method method) {
        String DEFAULT_BROWSER_TYPE_NAME = removeQuoteMark(getConfigInstance().defaultBrowser().toUpperCase());
        Set<String> methodSupportedBrowsers = BrowserUtils.getMethodSupportedBrowsers(method);
        Set<String> methodBrowsers = BrowserUtils.getMethodBrowsers(method);
        Set<String> ignoreBrowsers = BrowserUtils.getMethodIgnoredBrowsers(method);
        if (methodBrowsers.isEmpty() && !ignoreBrowsers.contains(DEFAULT_BROWSER_TYPE_NAME)) {
            methodSupportedBrowsers.add(DEFAULT_BROWSER_TYPE_NAME);
        }
        return methodSupportedBrowsers;
    }
}
