package com.github.ansonliao.selenium.internal;

import java.io.File;

public class Constants {

    public static final String FILE_SEPARATOR = File.separator;

    public static final String PROJECT_ROOT_DIR = System.getProperty("user.dir");

    private static final String WEBDRIVER_DIR =
            Constants.PROJECT_ROOT_DIR + FILE_SEPARATOR + "drivers";

    public static final String SCREENSHOT_DIR = PROJECT_ROOT_DIR
            .concat(Constants.FILE_SEPARATOR)
            .concat("target")
            .concat(Constants.FILE_SEPARATOR)
            .concat("screenshots");

    public static final String CHROME_DRIVER_64BIT_MAC_PATH =
            WEBDRIVER_DIR + FILE_SEPARATOR + "chromedriver-mac-64bit";

    public static final String CHROME_DRIVER_64BIT_LINUX_PATH =
            WEBDRIVER_DIR + FILE_SEPARATOR + "chromedriver-linux-64bit";

    public static final String CHROME_DRIVER_32BIT_WINDOWS_PATH =
            WEBDRIVER_DIR + FILE_SEPARATOR + "chromedriver-win32-64bit.exe";

    public static final String FIREFOX_DRIVER_64BIT_MAC_PATH =
            WEBDRIVER_DIR + FILE_SEPARATOR + "geckodriver-mac-64bit";

    public static final String FIREFOX_DRIVER_64BIT_LINUX_PATH =
            WEBDRIVER_DIR + FILE_SEPARATOR + "geckodriver-linux-64bit";

    public static final String FIREFOX_DRIVER_64BIT_WINDOWS_PATH =
            WEBDRIVER_DIR + FILE_SEPARATOR + "geckodriver-windows-64bit.exe";

    public static final String BROWSER_ANNOTATION_PACKAGE =
            "com.github.ansonliao.selenium.annotations.browser";

    public static final String BROWSER_IGNORE_ANNOTATION_PREFIX = "IGNORE";

    public static final String BROWSER_ANNOTATION_TYPE_PROPERTY = "BROWSER";

    public static final String BROWSER_IGNORE_ANNOTATION_TYPE_PROPERTY = "BROWSER_IGNORE";

    public static final String TESTNG_XML_BROWSER_PARAMETER_KEY = "browser";

}
