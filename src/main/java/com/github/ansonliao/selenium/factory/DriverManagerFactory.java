package com.github.ansonliao.selenium.factory;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.Supplier;

public class DriverManagerFactory {
    private static Map<String, Supplier<DriverManager>> map = Maps.newHashMap();

    static {
        map.put("CHROME", ChromeFactory::getInstance);
        map.put("FIREFOX", FirefoxFactory::getInstance);
        map.put("EDGE", EdgeFactory::getInstance);
        map.put("INTERNETEXPLOER", InternetExplorerFactory::getInstance);
        map.put("PHANTOMJS", PhantomJsFactory::getInstance);
        map.put("OPERA", OperaFactory::getInstance);
    }

    public static DriverManager getManager(String browserName) {
        Supplier<DriverManager> driverManagerSupplier = map.get(browserName.toUpperCase());
        if (driverManagerSupplier != null) {
            return driverManagerSupplier.get();
        }
        throw new IllegalArgumentException("No such browser webdriver: " + browserName);
    }
}
