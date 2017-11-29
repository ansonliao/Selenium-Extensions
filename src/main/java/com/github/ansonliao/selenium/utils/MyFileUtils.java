package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.internal.Constants;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class MyFileUtils extends FileUtils {

    public static synchronized File createScreenshotFolderForBrowser(
            Class clazz, String browserName) {
        String className = clazz.getSimpleName();
        String packageName = clazz.getPackage().getName();
        String destDir = String.join(
                Constants.FILE_SEPARATOR, Constants.SCREENSHOT_DIR,
                packageName, className, browserName);

        if ((new File(destDir)).exists()) {
            return new File(destDir);
        }

        new File(destDir).mkdirs();
        return new File(destDir);
    }
}
