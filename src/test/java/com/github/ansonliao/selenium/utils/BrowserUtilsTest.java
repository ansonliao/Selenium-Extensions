package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.testfixtures.BrowserAndModeFixture;
import com.github.ansonliao.selenium.testfixtures.ChromeAndIgnoreChromeFixture;
import com.github.ansonliao.selenium.testfixtures.ChromeFixture;
import com.github.ansonliao.selenium.testfixtures.IgnoreChromeFixture;
import com.github.ansonliao.selenium.testfixtures.MixedFixture;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class BrowserUtilsTest {

    @Test
    public void supportedBrowsersIncludeKnownAnnotations() {
        List<String> supported = BrowserUtils.getSupportedBrowsers();
        assertTrue(supported.contains("CHROME"));
        assertTrue(supported.contains("FIREFOX"));
        assertTrue(supported.contains("EDGE"));
        assertTrue(supported.contains("INTERNETEXPLORER"));
    }

    @Test
    public void getClassBrowsersReadsEnabledBrowser() {
        Set<String> browsers = BrowserUtils.getClassBrowsers(ChromeFixture.class);
        assertEquals(browsers, Set.of("CHROME"));
    }

    @Test
    public void getClassIgnoredBrowsersReadsIgnoredBrowser() {
        Set<String> ignored = BrowserUtils.getClassIgnoredBrowsers(IgnoreChromeFixture.class);
        assertEquals(ignored, Set.of("CHROME"));
    }

    @Test
    public void getClassSupportedBrowsersSubtractsIgnored() {
        Set<String> supported = BrowserUtils.getClassSupportedBrowsers(
                ChromeAndIgnoreChromeFixture.class);
        assertTrue(supported.isEmpty());
    }

    @Test
    public void getClassSupportedBrowsersKeepsEnabled() {
        Set<String> supported = BrowserUtils.getClassSupportedBrowsers(ChromeFixture.class);
        assertEquals(supported, Set.of("CHROME"));
    }

    @Test
    public void getMethodSupportedBrowsersUnionsClassAndMethod() throws Exception {
        Method method = MixedFixture.class.getDeclaredMethod("someTestMethod");
        Set<String> supported = BrowserUtils.getMethodSupportedBrowsers(MixedFixture.class, method);
        assertEquals(supported, Set.of("CHROME", "FIREFOX"));
    }

    @Test
    public void filterBrowsersFromAnnotationsExcludesModes() {
        Set<String> filtered = BrowserUtils.filterBrowsersFromAnnotations(
                Arrays.asList(BrowserAndModeFixture.class.getAnnotations()));
        assertEquals(filtered, Set.of("CHROME"));
    }
}
