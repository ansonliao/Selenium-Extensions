package com.github.ansonliao.selenium.internal.platform;

/**
 * Created by ansonliao on 10/3/2017.
 */
public enum Browser {
    CHROME("CHROME"),
    FIREFOX("FIREFOX"),
    InternetExplorer("INTERNETEXPLORER"),
    Edge("EDGE");

    private final String name;
    Browser(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Browser getDefaultBrowser() {
        return CHROME;
    }

}
