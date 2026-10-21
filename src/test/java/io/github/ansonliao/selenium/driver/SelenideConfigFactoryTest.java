package io.github.ansonliao.selenium.driver;

import com.codeborne.selenide.SelenideConfig;
import io.github.ansonliao.selenium.annotations.Headless;
import io.github.ansonliao.selenium.annotations.Incognito;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class SelenideConfigFactoryTest {

    private Method resolve(String name) throws NoSuchMethodException {
        return Fixture.class.getDeclaredMethod(name);
    }

    @Test
    public void normalizeBrowserAcceptsLowerCase() {
        assertEquals(SelenideConfigFactory.normalizeBrowser("chrome"), "chrome");
        assertEquals(SelenideConfigFactory.normalizeBrowser("firefox"), "firefox");
        assertEquals(SelenideConfigFactory.normalizeBrowser("edge"), "edge");
        assertEquals(SelenideConfigFactory.normalizeBrowser("ie"), "ie");
    }

    @Test
    public void normalizeBrowserAcceptsMixedCaseAndTrimming() {
        assertEquals(SelenideConfigFactory.normalizeBrowser("  CHROME  "), "chrome");
        assertEquals(SelenideConfigFactory.normalizeBrowser("InternetExplorer"),
                "ie");
        assertEquals(SelenideConfigFactory.normalizeBrowser("INTERNET EXPLORER"),
                "ie");
    }

    @Test
    public void normalizeBrowserRejectsEmptyAndUnknown() {
        assertThrows(IllegalArgumentException.class,
                () -> SelenideConfigFactory.normalizeBrowser(""));
        assertThrows(IllegalArgumentException.class,
                () -> SelenideConfigFactory.normalizeBrowser("safari"));
    }

    @Test
    public void normalizeHubUrlHandlesEmpty() {
        assertEquals(SelenideConfigFactory.normalizeHubUrl(""), "");
        assertEquals(SelenideConfigFactory.normalizeHubUrl(null), "");
    }

    @Test
    public void normalizeHubUrlAppendsWdHub() {
        assertEquals(SelenideConfigFactory.normalizeHubUrl("http://localhost:4444"),
                "http://localhost:4444/wd/hub");
        assertEquals(SelenideConfigFactory.normalizeHubUrl("http://localhost:4444/"),
                "http://localhost:4444/wd/hub");
    }

    @Test
    public void normalizeHubUrlKeepsExistingWdHub() {
        assertEquals(SelenideConfigFactory.normalizeHubUrl("http://localhost:4444/wd/hub"),
                "http://localhost:4444/wd/hub");
    }

    @Test
    public void normalizeHubUrlKeepsExistingWdHubWithTrailingSlash() {
        assertEquals(SelenideConfigFactory.normalizeHubUrl("http://localhost:4444/wd/hub/"),
                "http://localhost:4444/wd/hub");
    }

    @Test
    public void normalizeHubUrlIsCaseInsensitive() {
        assertEquals(SelenideConfigFactory.normalizeHubUrl("http://localhost:4444/WD/HUB"),
                "http://localhost:4444/WD/HUB");
    }

    @Test
    public void createUsesDefaultBrowserWhenEmpty() throws Exception {
        SelenideConfig config = SelenideConfigFactory.create("", resolve("plain"));
        assertEquals(config.browser(), "chrome");
        assertFalse(config.headless());
        // No hub url is configured by default, so remote must stay unset.
        assertTrue(config.remote() == null || config.remote().isEmpty());
    }

    @Test
    public void createAppliesHeadlessFromMethod() throws Exception {
        SelenideConfig config = SelenideConfigFactory.create("chrome", resolve("headless"));
        assertTrue(config.headless());
    }

    @Test
    public void createAppliesIncognitoArgument() throws Exception {
        SelenideConfig config = SelenideConfigFactory.create("chrome", resolve("incognito"));
        ChromeOptions options = (ChromeOptions) config.browserCapabilities();
        Object chromeOptions = options.getCapability("goog:chromeOptions");
        assertTrue(chromeOptions.toString().contains("--incognito"));
    }

    @Test
    public void createAppliesHeadlessFromClass() throws Exception {
        SelenideConfig config = SelenideConfigFactory.create("chrome",
                ClassLevelFixture.class.getDeclaredMethod("plain"));
        assertTrue(config.headless());
    }

    @Test
    public void createAppliesIncognitoFromClass() throws Exception {
        SelenideConfig config = SelenideConfigFactory.create("chrome",
                ClassLevelFixture.class.getDeclaredMethod("plain"));
        ChromeOptions options = (ChromeOptions) config.browserCapabilities();
        Object chromeOptions = options.getCapability("goog:chromeOptions");
        assertTrue(chromeOptions.toString().contains("--incognito"));
    }

    static class Fixture {
        @Test
        public void plain() {
        }

        @Test
        @Headless
        public void headless() {
        }

        @Test
        @Incognito
        public void incognito() {
        }
    }

    @Headless
    @Incognito
    static class ClassLevelFixture {
        @Test
        public void plain() {
        }
    }
}
