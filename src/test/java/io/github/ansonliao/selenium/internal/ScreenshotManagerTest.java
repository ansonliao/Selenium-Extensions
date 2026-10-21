package io.github.ansonliao.selenium.internal;

import io.github.ansonliao.selenium.testfixtures.UrlFixture;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertTrue;

public class ScreenshotManagerTest {

    private String pathFor(String prefix, String browser) {
        return new ScreenshotManager().screenshotPath(UrlFixture.class, prefix, browser);
    }

    @Test
    public void screenshotPathUsesPackageClassBrowserAndTimestamp() {
        String path = pathFor("failure", "chrome");

        assertTrue(path.startsWith(System.getProperty("user.dir")
                + File.separator + "target" + File.separator + "screenshots"), path);
        assertTrue(path.contains(File.separator + "io.github.ansonliao.selenium.testfixtures"
                + File.separator + "UrlFixture" + File.separator + "chrome" + File.separator), path);
        assertTrue(path.endsWith(".png"), path);
        assertTrue(path.matches(".*failure_\\d+\\.png"), path);
    }

    @Test
    public void screenshotPathEmbedsCurrentEpochMillis() {
        long before = System.currentTimeMillis();
        String path = pathFor("timeout", "firefox");
        long after = System.currentTimeMillis();

        long stamp = Long.parseLong(path.replaceAll(".*_(\\d+)\\.png", "$1"));

        assertTrue(stamp >= before && stamp <= after,
                "timestamp " + stamp + " outside [" + before + ", " + after + "]");
    }

    @Test
    public void screenshotPathKeepsPrefixInFileName() {
        assertTrue(pathFor("assertion-failed", "edge")
                .matches(".*assertion-failed_\\d+\\.png"));
    }
}
