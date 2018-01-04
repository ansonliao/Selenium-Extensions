package com.github.ansonliao.selenium.utils;


import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.util.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestNGListenerUtils {
    private final Logger logger = LoggerFactory.getLogger(TestNGListenerUtils.class);
    private static final String DEFAULT_TESTNG_LISTENER_KEY = "defaultTestNGListeners";
    private static final String TESTNG_LISTENER_KEY = "testngListeners";

    public static List<String> getDefaultTestNGListners() {
        if (Strings.isNotNullAndNotEmpty(SEConfig.getString(DEFAULT_TESTNG_LISTENER_KEY))) {
            return Arrays.stream(SEConfig.getString(DEFAULT_TESTNG_LISTENER_KEY).split(","))
                    .map(String::trim).distinct().collect(Collectors.toList());
        } else {
            return Lists.newArrayList();
        }
    }

    public static List<String> getTestNGListeners() {
        if (Strings.isNotNullAndNotEmpty(SEConfig.getString(TESTNG_LISTENER_KEY))) {
            return Arrays.stream(SEConfig.getString(TESTNG_LISTENER_KEY).split(","))
                    .map(String::trim).distinct().collect(Collectors.toList());
        } else {
            return Lists.newArrayList();
        }
    }

    @Test
    public void f1() {
        getDefaultTestNGListners().forEach(System.out::println);
        getTestNGListeners().forEach(System.out::println);
    }

}
