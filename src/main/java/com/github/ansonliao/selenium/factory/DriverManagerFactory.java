package com.github.ansonliao.selenium.factory;

public class DriverManagerFactory {

    public static DriverManager getManager(String browserName) {
        DriverManager driverManager = null;

        switch (browserName) {
            case "CHROME":
                driverManager = new ChromeDriverManager();
                break;
            case "FIREFOX":
                driverManager = new FirefoxDriverManager();
                break;
            case "EDGE":
                // add edge driver manager here
                break;
            case "INTERNETEXPLOER":
                // add ie driver manager here
                break;
            default:
                driverManager = new ChromeDriverManager();
                break;
        }

        return driverManager;
    }
}
