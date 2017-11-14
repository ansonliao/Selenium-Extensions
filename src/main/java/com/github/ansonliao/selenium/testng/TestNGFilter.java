package com.github.ansonliao.selenium.testng;

import com.github.ansonliao.selenium.internal.Variables;
import com.github.ansonliao.selenium.parallel.ClassFinder;
import com.github.ansonliao.selenium.parallel.MethodFinder;
import com.github.ansonliao.selenium.utils.BrowserUtils;
import com.github.ansonliao.selenium.utils.SEConfig;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TestNGFilter {
    private static final Logger logger = LoggerFactory.getLogger(TestNGFilter.class);
    private static List<Class<?>> testngClasses = new ArrayList<>();
    private static Multimap<Class<?>, Method> testNGClass2MethodMap = HashMultimap.create();
    private static final String EXTERNAL_RUN_BROWSER_KEY = "runByBrowsers";

    static {
        String[] testingPackageNames = Variables.TESTING_PACKAGE_NAMES.toArray(
                new String[Variables.TESTING_PACKAGE_NAMES.size()]);
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
        if (!Variables.TESTING_TESTNG_CLASSES.isEmpty()) {
            return testngClasses.parallelStream()
                    .filter(aClass ->
                            Variables.TESTING_TESTNG_CLASSES
                                    .parallelStream()
                                    .map(String::toUpperCase)
                                    .collect(Collectors.toList())
                                    .contains(aClass.getCanonicalName().toUpperCase()))
                    .distinct()
                    .collect(Collectors.toList());
        }

        return testngClasses;
    }

    /**
     *
     * @return A map that key = testng class, value = list of testng method of the testng class
     */
    public static Multimap<Class<?>, Method> filterTestNGMethodByTestGroups() {
        testNGClass2MethodMap = HashMultimap.create();
        if (Variables.TESTING_TEST_GROUPS.isEmpty()) {
            testngClasses.stream()
                    .forEach(aClass ->
                            MethodFinder.findAllAnnotatedTestMethodInClass(aClass)
                                    .forEach(method -> testNGClass2MethodMap.put(aClass, method)));

            return testNGClass2MethodMap;
        }

        testngClasses.stream().forEach(aClass -> {
            List<Method> methodList = Variables.TESTING_TEST_GROUPS
                    .parallelStream()
                    .map(group -> MethodFinder.findTestNGMethodInClassByGroup(aClass, group))
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
            if (methodList.size() > 0) {
                methodList.forEach(method -> testNGClass2MethodMap.put(aClass, method));
            }
        });

        return testNGClass2MethodMap;
    }

    // filter testng method by given test browser list
    public static Multimap<Class<?>, Method> filterTestNGMethodByBrowsers() {
        if (!Variables.TESTING_BROWSER_NAMES.isEmpty()) {
            testNGClass2MethodMap.keySet().parallelStream().forEach(aClass ->
                    testNGClass2MethodMap.get(aClass).stream().forEach(method -> {
                        Sets.SetView result = Sets.intersection(
                                BrowserUtils.getMethodSupportedBrowsers(method),
                                Sets.newHashSet(Variables.TESTING_BROWSER_NAMES));
                        if (result.size() == 0) {
                            if (testNGClass2MethodMap.get(aClass).contains(method)) {
                                testNGClass2MethodMap.get(aClass).remove(method);
                            }
                        }
                    }));
        }

        return testNGClass2MethodMap;
    }

    public static Map<String, Map<Class<?>, List<Method>>> getBrowser2TestNGClass2TestNGMethodMap() {
        Map<String, Map<Class<?>, List<Method>>> browserTestingMap = new HashMap<>();
        Multimap<String, Method> browserTestngMethodMap = HashMultimap.create();

        if (!Strings.isNullOrEmpty(SEConfig.getString(EXTERNAL_RUN_BROWSER_KEY))) {
            // only fetch support browsers
            List<String> browsers =
                    Sets.intersection(
                            Arrays.stream(SEConfig.getString(EXTERNAL_RUN_BROWSER_KEY).split(","))
                                    .map(String::trim).collect(Collectors.toSet()),
                            Sets.newHashSet(BrowserUtils.getSupportedBrowsers()))
                    .parallelStream().collect(Collectors.toList());

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
        String DEFAULT_BROWSER_TYPE_NAME = Variables.DEFAULT_BROWSER_TYPE_NAME.trim().toUpperCase();
        Set<String> methodSupportedBrowsers = BrowserUtils.getMethodSupportedBrowsers(method);
        Set<String> methodBrowsers = BrowserUtils.getMethodBrowsers(method);
        Set<String> ignoreBrowsers = BrowserUtils.getMethodIgnoredBrowsers(method);
        if (methodBrowsers.isEmpty()
                && !ignoreBrowsers.contains(DEFAULT_BROWSER_TYPE_NAME)) {
            methodSupportedBrowsers.add(DEFAULT_BROWSER_TYPE_NAME);
        }
        return methodSupportedBrowsers;
    }
}
