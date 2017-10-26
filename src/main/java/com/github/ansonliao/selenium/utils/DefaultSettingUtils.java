package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.internal.Variables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultSettingUtils {
    private static final String PACKAGE_KEY                 =
            "testPackages";
    private static final String GROUPS_KEY                  =
            "testGroups";
    private static final String CLASS_LIST                  =
            "testClasses";
    private static final String BROWSER_LIST                =
            "testBrowsers";
    private static final String DEFAULT_BROWSER             =
            "defaultBrowser";
    private static final String CLASS_SIZE                  =
            "testClassSize";
    private static final String BROWSER_ANNOTATION_PACKAGE  =
            "browserPackage";

    public static void set() {
        setTestingPackages();
        setTestingGroups();
        setTestingClassList();
        setTestingBrowserList();
        setDefaultBrowser();
        setDefaultTestClassSize();
        setBrowserAnnotationPackage();
    }

    public static void setTestingPackages() {
        Variables.TESTING_PACKAGE_NAMES =
                splitStringIntoList(getSystemProperty(PACKAGE_KEY));
    }

    public static void setTestingPackages(List<String> packageList) {
        Variables.TESTING_PACKAGE_NAMES = packageList;
    }

    public static void setTestingGroups() {
        Variables.TESTING_TEST_GROUPS =
                splitStringIntoList(getSystemProperty(GROUPS_KEY));
    }

    public static void setTestingGroups(List<String> groupList) {
        Variables.TESTING_TEST_GROUPS = groupList;
    }

    public static void setTestingClassList() {
        Variables.TESTING_TESTNG_CLASSES =
                splitStringIntoList(getSystemProperty(CLASS_LIST));
    }

    public static void setTestingClassList(List<String> classList) {
        Variables.TESTING_TESTNG_CLASSES = classList;
    }

    public static void setTestingBrowserList() {
        Variables.TESTING_BROWSER_NAMES =
                splitStringIntoList(getSystemProperty(BROWSER_LIST));
    }

    public static void setTestingBrowserList(List<String> browserList) {
        Variables.TESTING_BROWSER_NAMES = browserList;
    }

    public static void setDefaultBrowser() {
        getSystemProperty(DEFAULT_BROWSER)
                .ifPresent(browser -> Variables.DEFAULT_BROWSER_TYPE_NAME = browser.toUpperCase());
    }

    public static void setDefaultBrowser(String browserName) {
        Variables.DEFAULT_BROWSER_TYPE_NAME = browserName;
    }

    public static void setDefaultTestClassSize() {
        getSystemProperty(CLASS_SIZE)
                .ifPresent(size -> Variables.DEFAULT_TEST_CLASS_SIZE_OF_TESTNG_XML = Integer.valueOf(size));
    }

    public static void setDefaultTestClassSize(int classSize) {
        if (classSize > 0) {
            Variables.DEFAULT_TEST_CLASS_SIZE_OF_TESTNG_XML = classSize;
        }
    }

    public static void setBrowserAnnotationPackage() {
        getSystemProperty(BROWSER_ANNOTATION_PACKAGE)
                .ifPresent(pkg -> Variables.DEFAULT_BROWSER_ANNOTATION_PACKAGE_NAME = pkg);
    }

    public static void setBrowserAnnotationPackage(String packageName) {
        Variables.DEFAULT_BROWSER_ANNOTATION_PACKAGE_NAME = packageName;
    }



    public static Optional<String> getSystemProperty(String key) {
        if (System.getProperties().containsKey(key)) {
            return Optional.ofNullable(
                    System.getProperty(key).trim().isEmpty()
                            ? null
                            : System.getProperty(key).trim());
        }

        return Optional.empty();
    }

    public static Optional<String> getSystemEnv(String key) {
        if (System.getenv().containsKey(key)) {
            return Optional.ofNullable(
                    System.getenv(key).trim().isEmpty()
                            ? null
                            : System.getenv(key).trim());
        }

        return Optional.empty();
    }

    public static List<String> splitStringIntoList(
            Optional<String> optional) {
        List<String> stringList = Lists.newArrayList();
        optional.ifPresent(string -> {
            if (string.contains(",")) {
                Lists.newArrayList(string.split(","))
                        .parallelStream().map(String::trim)
                        .distinct().collect(Collectors.toList())
                        .forEach(stringList::add);
            } else {
                stringList.add(string.trim());
            }
        });

        return stringList;
    }
}
