package com.github.ansonliao.selenium.utils;


import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

public class TestNGListenerUtils {
    private final Logger logger = LoggerFactory.getLogger(TestNGListenerUtils.class);

    public static List<String> getDefaultTestNGListeners() {
        return getConfigInstance().defaultTestNGListeners().isEmpty()
                ? getConfigInstance().defaultTestNGListeners()
                : Lists.newArrayList();
        // if (getConfigInstance().testngListeners().size() > 0) {
        //     return Arrays.stream(SEConfig.getString(DEFAULT_TESTNG_LISTENER_KEY).split(","))
        //             .map(String::trim).distinct().collect(Collectors.toList());
        // } else {
        //     return Lists.newArrayList();
        // }
    }

    public static List<String> getTestNGListeners() {
        return getConfigInstance().testngListeners().isEmpty()
                ? getConfigInstance().testngListeners()
                : Lists.newArrayList();
        // if (Strings.isNotNullAndNotEmpty(SEConfig.getString(TESTNG_LISTENER_KEY))) {
        //     return Arrays.stream(SEConfig.getString(TESTNG_LISTENER_KEY).split(","))
        //             .map(String::trim).distinct().collect(Collectors.toList());
        // } else {
        //     return Lists.newArrayList();
        // }
    }

}
