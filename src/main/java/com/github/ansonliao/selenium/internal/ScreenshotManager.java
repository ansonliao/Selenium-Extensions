package com.github.ansonliao.selenium.internal;

import com.github.ansonliao.selenium.factory.WDManager;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import ru.yandex.qatools.ashot.AShot;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

public class ScreenshotManager {
    private static final String FILE_SEPARATOR = File.separator;
    private static final String PROJECT_ROOT_DIR = System.getProperty("user.dir");
    private static final String SCREENSHOT_DIR = PROJECT_ROOT_DIR
            .concat(FILE_SEPARATOR)
            .concat("target")
            .concat(FILE_SEPARATOR)
            .concat("screenshots");

    public String capture(Class<?> clazz, String imgPrefix, String browserName) {
        // File scrFile = ((TakesScreenshot) WDManager.getDriver())
        //         .getScreenshotAs(OutputType.FILE);

        String destDir = String.join(
                FILE_SEPARATOR,
                SCREENSHOT_DIR,
                clazz.getPackage().getName(),
                clazz.getSimpleName(),
                browserName,
                imgPrefix.concat("_").concat(String.valueOf(
                        new Timestamp(System.currentTimeMillis()).getTime()))
                        .concat(".jpeg"));
        try {
            // MyFileUtils.copyFile(scrFile, new File(destDir));
            ImageIO.write(
                    new AShot().takeScreenshot(WDManager.getDriver()).getImage(),
                    "PNG",
                    new File(destDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destDir.replace(
                PROJECT_ROOT_DIR
                        .concat(FILE_SEPARATOR)
                        .concat("target")
                        .concat(FILE_SEPARATOR), "");
    }
}
