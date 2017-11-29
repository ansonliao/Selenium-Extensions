package com.github.ansonliao.selenium.internal;

import com.github.ansonliao.selenium.factory.WDManager;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

public class ScreenshotManager {

    public String capture(Class<?> clazz, String imgPrefix, String browserName) {
        File scrFile = ((TakesScreenshot) WDManager.getDriver())
                .getScreenshotAs(OutputType.FILE);

        String destDir = String.join(
                Constants.FILE_SEPARATOR,
                Constants.SCREENSHOT_DIR,
                clazz.getPackage().getName(),
                clazz.getSimpleName(),
                browserName,
                imgPrefix.concat("_").concat(String.valueOf(
                                new Timestamp(System.currentTimeMillis()).getTime()))
                        .concat(".jpeg"));
        try {
            MyFileUtils.copyFile(scrFile, new File(destDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destDir.replace(
                Constants.PROJECT_ROOT_DIR
                        .concat(Constants.FILE_SEPARATOR)
                        .concat("target")
                        .concat(Constants.FILE_SEPARATOR), "");
    }
}
