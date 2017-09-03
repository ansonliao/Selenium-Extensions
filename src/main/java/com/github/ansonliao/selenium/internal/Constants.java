package com.github.ansonliao.selenium.internal;

import java.io.File;

/**
 * Created by ansonliao on 30/3/2017.
 */
public class Constants {

    public static final String FILE_SEPARATOR = File.separator;
    private static final String WEBDRIVER_DIR = "target" + FILE_SEPARATOR + "drivers";

    public static final String CHROME_DRIVER_64BIT_MAC_PATH = WEBDRIVER_DIR + FILE_SEPARATOR + "chromedriver-mac-64bit";

    public static final String CHROME_DRIVER_64BIT_LINUX_PATH = WEBDRIVER_DIR + FILE_SEPARATOR + "chromedriver-linux-64bit";

    public static final String CHROME_DRIVER_32BIT_WINDOWS_PATH = WEBDRIVER_DIR + FILE_SEPARATOR + "chromedriver-win32-64bit.exe";

    public static final String FIREFOX_DRIVER_64BIT_MAC_PATH = WEBDRIVER_DIR + FILE_SEPARATOR + "geckodriver-mac-64bit";

    public static final String FIREFOX_DRIVER_64BIT_LINUX_PATH = WEBDRIVER_DIR + FILE_SEPARATOR + "geckodriver-linux-64bit";

    public static final String FIREFOX_DRIVER_64BIT_WINDOWS_PATH = WEBDRIVER_DIR + FILE_SEPARATOR + "geckodriver-windows-64bit.exe";

}
