package com.github.ansonliao.selenium.utils;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

public class SEFilterUtils {

    public static boolean addBrowserGroupToReport() {
        return getConfigInstance().addBrowserGroupToReport();
    }

    public static List<String> runByBrowsers() {
        return getConfigInstance().runByBrowsers().isEmpty()
                ? Lists.newArrayList()
                : getConfigInstance().runByBrowsers();
    }

    public static String defaultBrowser() {
        return getConfigInstance().defaultBrowser();
    }

    public static String defaultBrowserAnnotationPackage() {
        return getConfigInstance().defaultBrowserAnnotationPackage();
    }

    public static int defaultTestClassesSizeOfTestNGXML() {
        return getConfigInstance().defaultTestClassesSizeOfTestNGXML();
    }

    public static List<String> testingPackageNames() {
        return getConfigInstance().testingPackageNames().isEmpty()
                ? Lists.newArrayList()
                : getConfigInstance().testingPackageNames();
    }

    public static List<String> testingBrowserNames() {
        return getConfigInstance().testingBrowserNames().isEmpty()
                ? Lists.newArrayList()
                : getConfigInstance().testingBrowserNames();
    }

    public static List<String> testingTestGroups() {
        return getConfigInstance().testingTestGroups().isEmpty()
                ? Lists.newArrayList()
                : getConfigInstance().testingTestGroups();
    }

    public static List<String> testingTestNGClasses() {
        return getConfigInstance().testingTestNGClasses().isEmpty()
                ? Lists.newArrayList()
                : getConfigInstance().testingTestNGClasses();
    }

    public static String getChromeDriverExportKey() {
        return Optional.ofNullable(getConfigInstance().chromeDriverProperty())
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Chrome driver export key was not found."));
    }

    public static String getFirefoxDriverExportKey() {
        return Optional.ofNullable(getConfigInstance().firefoxDriverProperty())
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Firefox driver export key was not found."));
    }

    public static String getPhantomJsDriverExportKey() {
        return Optional.ofNullable(getConfigInstance().phantomjsDriverProperty())
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "PhantomJS driver export key was not found."));
    }

    public static String getEdgeDriverExportKey() {
        return Optional.ofNullable(getConfigInstance().edgeDriverProperty())
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Edge driver export key was not found."));
    }

    public static String getIEDriverExportKey() {
        return Optional.ofNullable(getConfigInstance().ieDriverProperty())
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "InternetExplorer driver export key was not found."));
    }

    public static String getOperaDriverExportKey() {
        return Optional.ofNullable(getConfigInstance().operaDriverProperty())
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Opera driver export key was not found."));
    }

    public static boolean useTaobaoMirror() {
        return getConfigInstance().useTaobaoMirror();
    }
}
