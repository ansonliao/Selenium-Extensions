package com.github.ansonliao.selenium.internal;

import com.google.common.collect.Lists;

import java.util.List;

public class Variables {

    public static List<String> TESTING_PACKAGE_NAMES = Lists.newArrayList();

    public static List<String> TESTING_BROWSER_NAMES = Lists.newArrayList();

    public static List<String> TESTING_TEST_GROUPS = Lists.newArrayList();

    public static List<String> TESTING_TESTNG_CLASSES = Lists.newArrayList();

    public static String DEFAULT_BROWSER_TYPE_NAME = "CHROME";

    public static String DEFAULT_BROWSER_ANNOTATION_PACKAGE_NAME =
            "com.github.ansonliao.selenium.annotations.browser";

    public static int DEFAULT_TEST_CLASS_SIZE_OF_TESTNG_XML = 1;

    public static int TEST_FAILURE_COUNTER = 0;
}
