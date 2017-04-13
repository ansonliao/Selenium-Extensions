package com.github.ansonliao.selenium.internal;

import java.io.File;

/**
 * Created by ansonliao on 31/3/2017.
 */
public class Constants {
    public static final String FILE_SEPARATOR = File.separator;

    public static final String ROOT_DIRECTORY = System.getProperty("user.dir");

    private static final String RESOURCE_DIRECTORY = "src/main/resources";

    public static final String CHROMEDRIVER_MAC_64BIT_PATH =
            ROOT_DIRECTORY + FILE_SEPARATOR + RESOURCE_DIRECTORY + FILE_SEPARATOR + "chromedriver-mac-64bit";

    public static final String CHROMEDRIVER_MAC_32BIT_PATH =
            ROOT_DIRECTORY + FILE_SEPARATOR + RESOURCE_DIRECTORY + FILE_SEPARATOR + "chromedriver-mac-32bit";

    public static final String CHROMEDRIVER_LINUX_64BIT_PATH =
            ROOT_DIRECTORY + FILE_SEPARATOR + RESOURCE_DIRECTORY + FILE_SEPARATOR + "chromedriver-linux-64bit";

    public static final String CHROMEDRIVER_LINUX_32BIT_PATH =
            ROOT_DIRECTORY + FILE_SEPARATOR + RESOURCE_DIRECTORY + FILE_SEPARATOR + "chromedriver-linux-32bit";

    public static final String CHROMEDRIVER_WINDOWS_32BIT_PATH =
            ROOT_DIRECTORY + FILE_SEPARATOR + RESOURCE_DIRECTORY + FILE_SEPARATOR + "chromedriver-windows-32bit.exe";

    public static final String FIREFOXDRIVER_MAC_64BIT_PATH =
            ROOT_DIRECTORY + FILE_SEPARATOR + RESOURCE_DIRECTORY + FILE_SEPARATOR + "geckodriver-mac-64bit";

    public static final String FIREFOXDRIVER_MAC_32BIT_PATH =
            ROOT_DIRECTORY + FILE_SEPARATOR + RESOURCE_DIRECTORY + FILE_SEPARATOR + "geckodriver-mac-32bit";

    public static final String FIREFOXDRIVER_LINUX_64BIT_PATH =
            ROOT_DIRECTORY + FILE_SEPARATOR + RESOURCE_DIRECTORY + FILE_SEPARATOR + "geckodriver-linux-64bit";

    public static final String FIREFOXDRIVER_LINUX_32BIT_PATH =
            ROOT_DIRECTORY + FILE_SEPARATOR + RESOURCE_DIRECTORY + FILE_SEPARATOR + "geckodriver-linix-32bit";

    public static final String FIREFOXDRIVER_WINDOWS_64BIT_PATH =
            ROOT_DIRECTORY + FILE_SEPARATOR + RESOURCE_DIRECTORY + FILE_SEPARATOR + "geckodriver-windows-64bit.exe";

    public static final String FIREFOXDRIVER_WINDOWS_32BIT_PATH =
            ROOT_DIRECTORY + FILE_SEPARATOR + RESOURCE_DIRECTORY + FILE_SEPARATOR + "geckodriver-windows-32bit.exe";

}
