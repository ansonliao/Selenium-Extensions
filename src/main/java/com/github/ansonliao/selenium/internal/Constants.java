package com.github.ansonliao.selenium.internal;

import java.io.File;

public class Constants {

    public static final String FILE_SEPARATOR = File.separator;

    public static final String PROJECT_ROOT_DIR = System.getProperty("user.dir");

    public static final String SCREENSHOT_DIR = PROJECT_ROOT_DIR
            .concat(Constants.FILE_SEPARATOR)
            .concat("target")
            .concat(Constants.FILE_SEPARATOR)
            .concat("screenshots");

    public static final String BROWSER_ANNOTATION_PACKAGE =
            "com.github.ansonliao.selenium.annotations.browser";

    public static final String BROWSER_IGNORE_ANNOTATION_PREFIX = "IGNORE";

    public static final String BROWSER_ANNOTATION_TYPE_PROPERTY = "BROWSER";

    public static final String BROWSER_IGNORE_ANNOTATION_TYPE_PROPERTY = "BROWSER_IGNORE";

    public static final String TESTNG_XML_BROWSER_PARAMETER_KEY = "browser";

    public static final String SELENIUM_HUB_URL = "SELENIUM_HUB_URL";

}
