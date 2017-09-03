package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.internal.platform.Browser;

public class DriverManagerFactory {

    public static DriverManager getManager(Browser browser) {
        DriverManager driverManager = null;

        switch (browser) {
            case CHROME:
                driverManager = new ChromeDriverManager();
                break;
            case FIREFOX:
                // add firefox driver manager here
                break;
            case Edge:
                // add edge driver manager here
                break;
            case InternetExplorer:
                // add ie driver manager here
                break;
            default:
                driverManager = new ChromeDriverManager();
                break;
        }

        return driverManager;
    }
}
