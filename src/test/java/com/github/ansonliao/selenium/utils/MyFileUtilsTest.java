package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.testfixtures.ChromeFixture;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class MyFileUtilsTest {

    private File createdDir;

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
        createdDir = MyFileUtils.createScreenshotFolderForBrowser(
                ChromeFixture.class, "chrome");
        assertNotNull(createdDir);
        assertTrue(createdDir.exists());
        assertTrue(createdDir.isDirectory());
    }

    @AfterMethod
    public void cleanup() {
        if (createdDir != null && createdDir.exists()) {
            deleteRecursively(createdDir);
            File parent = createdDir.getParentFile();
            while (parent != null && !"screenshots".equals(parent.getName())) {
                if (parent.list().length == 0) {
                    parent.delete();
                    parent = parent.getParentFile();
                } else {
                    break;
                }
            }
            File screenshots = new File(System.getProperty("user.dir"),
                    "target/screenshots");
            if (screenshots.exists() && screenshots.list().length == 0) {
                screenshots.delete();
            }
        }
        createdDir = null;
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
