package io.github.ansonliao.selenium.utils;

import io.github.ansonliao.selenium.testfixtures.ChromeFixture;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class MyFileUtilsTest {

    @Test
    public void isFileExistedFalseForNullAndEmpty() {
        assertFalse(MyFileUtils.isFileExisted(null));
        assertFalse(MyFileUtils.isFileExisted(""));
    }

    @Test
    public void isFileExistedFalseForMissingFile() {
        assertFalse(MyFileUtils.isFileExisted("no-such-file-xyz.txt"));
    }

    @Test
    public void isFileExistedTrueForExistingFile() throws Exception {
        File temp = File.createTempFile("myfileutils", ".tmp");
        temp.deleteOnExit();
        assertTrue(MyFileUtils.isFileExisted(temp.getAbsolutePath()));
    }

    @Test
    public void createScreenshotFolderForBrowserCreatesDirectory() {
        File createdDir = MyFileUtils.createScreenshotFolderForBrowser(
                ChromeFixture.class, "chrome");
        assertNotNull(createdDir);
        assertTrue(createdDir.exists());
        assertTrue(createdDir.isDirectory());
    }

    @AfterMethod
    public void cleanup() {
        // Remove the entire target/screenshots tree created by this test so the
        // unit run leaves no filesystem artifact behind (the directory lives
        // outside build/, so Gradle's clean does not remove it).
        File screenshots = new File(System.getProperty("user.dir"),
                "target/screenshots");
        deleteRecursively(screenshots);
    }

    private void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}
