package com.github.ansonliao.selenium.utils;

import com.google.common.collect.Lists;
import org.testng.annotations.Test;
import org.testng.util.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SEFilterUtils {

    public static boolean addBrowserGroupToReport() {
        final String KEY = "addBrowserGroupToReport";
        return SEConfig.getBoolean(KEY);
    }

    public static List<String> runByBrowsers() {
        final String KEY = "runByBrowsers";
        return Strings.isNotNullAndNotEmpty(SEConfig.getString(KEY))
                ? Arrays.asList(SEConfig.getString(KEY).split(","))
                .parallelStream().map(String::trim).map(String::toUpperCase).distinct()
                .collect(Collectors.toList())
                : Lists.newLinkedList();
    }

    public static String defaultBrowser() {
        final String KEY = "defaultBrowser";
        return SEConfig.getString(KEY);
    }

    public static String defaultBrowserAnnotationPackage() {
        final String KEY = "defaultBrowserAnnotationPackage";
        return SEConfig.getString(KEY);
    }

    public static int defaultTestClassesSizeOfTestNGXML() {
        final String KEY = "defaultTestClassesSizeOfTestNGXML";
        return SEConfig.getInt(KEY) > 0
                ? SEConfig.getInt(KEY)
                : 10;
    }

    public static List<String> testingPackageNames() {
        final String KEY = "testingPackageNames";
        return Strings.isNotNullAndNotEmpty(SEConfig.getString(KEY))
                ? Arrays.asList(SEConfig.getString(KEY).split(","))
                .parallelStream().map(String::trim).distinct()
                .collect(Collectors.toList())
                : Lists.newLinkedList();
    }

    public static List<String> testingBrowserNames() {
        final String KEY = "testingBrowserNames";
        return Strings.isNotNullAndNotEmpty(SEConfig.getString(KEY))
                ? Arrays.asList(SEConfig.getString(KEY).split(","))
                .parallelStream().map(String::trim).distinct()
                .collect(Collectors.toList())
                : Lists.newLinkedList();
    }

    public static List<String> testingTestGroups() {
        final String KEY = "testingTestGroups";
        return Strings.isNotNullAndNotEmpty(SEConfig.getString(KEY))
                ? Arrays.asList(SEConfig.getString(KEY).split(","))
                .parallelStream().map(String::trim).distinct()
                .collect(Collectors.toList()) : Lists.newLinkedList();
    }

    public static List<String> testingTestNGClasses() {
        final String KEY = "testingTestNGClasses";
        return Strings.isNotNullAndNotEmpty(SEConfig.getString(KEY))
                ? Arrays.asList(SEConfig.getString(KEY).split(","))
                .parallelStream().map(String::trim).distinct()
                .collect(Collectors.toList()) : Lists.newLinkedList();
    }

    public static String getChromeDriverExportKey() {
        final String KEY = "wdParameterKey.chrome";
        return Optional.ofNullable(SEConfig.getString(KEY))
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Chrome driver export key was not found."));
    }

    public static String getFirefoxDriverExportKey() {
        final String KEY = "wdParameterKey.firefox";
        return Optional.ofNullable(SEConfig.getString(KEY))
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Firefox driver export key was not found."));
    }

    public static String getPhantomJsDriverExportKey() {
        final String KEY = "wdParameterKey.phantomjs";
        return Optional.ofNullable(SEConfig.getString(KEY))
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "PhantomJS driver export key was not found."));
    }

    public static String getEdgeDriverExportKey() {
        final String KEY = "wdParameterKey.edge";
        return Optional.ofNullable(SEConfig.getString(KEY))
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Edge driver export key was not found."));
    }

    public static String getIEDriverExportKey() {
        final String KEY = "wdParameterKey.ie";
        return Optional.ofNullable(SEConfig.getString(KEY))
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "InternetExplorer driver export key was not found."));
    }

    public static String getOperaDriverExportKey() {
        final String KEY = "wdParameterKey.opera";
        return Optional.ofNullable(SEConfig.getString(KEY))
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Opera driver export key was not found."));
    }

    public static boolean useTaobaoMirror() {
        final String KEY = "wd.useTaobaoMirror";
        return SEConfig.getBoolean(KEY);
    }
}
