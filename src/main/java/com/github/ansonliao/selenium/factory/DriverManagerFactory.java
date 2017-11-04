package com.github.ansonliao.selenium.factory;

public class DriverManagerFactory {

    public static DriverManager getManager(String browserName) {
        DriverManager driverManager;

        switch (browserName) {
            case "CHROME":
                driverManager = ChromeFactory.getInstance();
                break;
            case "FIREFOX":
                driverManager = FirefoxFactory.getInstance();
                break;
            case "EDGE":
                driverManager = EdgeFactory.getInstance();
                break;
            case "INTERNETEXPLOER":
                driverManager = InternetExplorerFactory.getInstance();
                break;
            case "PHANTOMJS":
                driverManager = PhantomJsFactory.getInstance();
                break;
            case "OPERA":
                driverManager = OperaFactory.getInstance();
                break;
            default:
                driverManager = ChromeFactory.getInstance();
                break;
        }

        return driverManager;
    }
}
