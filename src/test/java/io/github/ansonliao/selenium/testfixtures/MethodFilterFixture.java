package io.github.ansonliao.selenium.testfixtures;

import io.github.ansonliao.selenium.annotations.browser.Chrome;
import io.github.ansonliao.selenium.annotations.browser.IgnoreChrome;
import org.testng.annotations.Test;

/**
 * Carries enabled {@code @Test} methods so the TestNG filter / XML builder
 * tests can exercise real method discovery. It lives outside the
 * {@code *Test} naming pattern, so Gradle never runs it as a test class.
 */
public class MethodFilterFixture {

    @Test
    public void unannotated() {
    }

    @Test
    @Chrome
    public void chromeOnly() {
    }

    @Test
    @IgnoreChrome
    public void notForChrome() {
    }

    @Test(groups = "smoke")
    public void smoke() {
    }
}
