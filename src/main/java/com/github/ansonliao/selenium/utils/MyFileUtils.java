package com.github.ansonliao.selenium.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MyFileUtils extends FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(MyFileUtils.class);
    private static final String FILE_SEPARATOR = File.separator;
    private static final String PROJECT_ROOT_DIR = System.getProperty("user.dir");
    private static final String SCREENSHOT_DIR = PROJECT_ROOT_DIR
            .concat(FILE_SEPARATOR)
            .concat("target")
            .concat(FILE_SEPARATOR)
            .concat("screenshots");

    public static synchronized File createScreenshotFolderForBrowser(
            Class clazz, String browserName) {
        String className = clazz.getSimpleName();
        String packageName = clazz.getPackage().getName();
        String destDir = String.join(
                FILE_SEPARATOR, SCREENSHOT_DIR,
                packageName, className, browserName);

        if ((new File(destDir)).exists()) {
            return new File(destDir);
        }

        logger.info("Create screenshot directory: {}", destDir);
        new File(destDir).mkdirs();
        return new File(destDir);
    }
}
