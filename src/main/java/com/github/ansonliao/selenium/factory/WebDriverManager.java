package com.github.ansonliao.selenium.factory;

import org.openqa.selenium.WebDriver;

public class WebDriverManager {

    private static ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<>();

    public synchronized static void setDriver(WebDriver driver) {
        webDriverThreadLocal.set(driver);
    }

    public synchronized static WebDriver getDriver() {
        return webDriverThreadLocal.get();
    }
}
