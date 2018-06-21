package com.github.ansonliao.selenium.utils.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SEConfigs {
    private static SEConfig config;

    @Config.HotReload(value = 500, unit = TimeUnit.MILLISECONDS)
    @Config.Sources({"classpath:seleniumextensions.properties", "classpath:se.properties"})
    public interface SEConfig extends Config {

        @Key("addBrowserGroupToReport")
        @DefaultValue("false")
        boolean addBrowserGroupToReport();

        @Key("runByBrowsers")
        @DefaultValue("")
        @Separator(",")
        List<String> runByBrowsers();

        @Key("defaultBrowser")
        @DefaultValue("Chrome")
        String defaultBrowser();

        @Key("defaultBrowserAnnotationPackage")
        @DefaultValue("com.github.ansonliao.selenium.annotations.browser")
        @Separator(",")
        List<String> defaultBrowserAnnotationPackage();

        @Key("defaultTestClassesSizeOfTestNGXML")
        @DefaultValue("10")
        int defaultTestClassesSizeOfTestNGXML();

        @Key("testingPackageNames")
        @DefaultValue("")
        @Separator(",")
        List<String> testingPackageNames();

        @Key("testingBrowserNames")
        @DefaultValue("")
        @Separator(",")
        List<String> testingBrowserNames();

        @Key("testingTestGroups")
        @DefaultValue("")
        @Separator(",")
        List<String> testingTestGroups();

        @Key("testingTestNGClasses")
        @DefaultValue("")
        @Separator(",")
        List<String> testingTestNGClasses();

        @Key("testngListeners")
        @DefaultValue("")
        @Separator(",")
        List<String> testngListeners();

        @Key("defaultTestNGListeners")
        @DefaultValue(
                "com.github.ansonliao.selenium.testng.TestResultListener, com.github.ansonliao.selenium.parallel.SeleniumParallelTestListener")
        @Separator(",")
        List<String> defaultTestNGListeners();

        @Key("wdParameterKey.chrome")
        @DefaultValue("webdriver.chrome.driver")
        String chromeDriverProperty();

        @Key("wdParameterKey.firefox")
        @DefaultValue("webdriver.gecko.driver")
        String firefoxDriverProperty();

        @Key("wdParameterKey.phantomjs")
        @DefaultValue("phantomjs.binary.path")
        String phantomjsDriverProperty();

        @Key("wdParameterKey.edge")
        @DefaultValue("webdriver.edge.driver")
        String edgeDriverProperty();

        @Key("wdParameterKey.ie")
        @DefaultValue("webdriver.ie.driver")
        String ieDriverProperty();

        @Key("wdParameterKey.opera")
        @DefaultValue("webdriver.opera.driver")
        String operaDriverProperty();

        @Key("wd.useTaobaoMirror")
        @DefaultValue("false")
        boolean useTaobaoMirror();

        @Key("SELENIUM_HUB_URL")
        @DefaultValue("")
        String seleniumHubUrl();
    }

    public static synchronized SEConfig getConfigInstance() {
        if (config == null) {
            config = ConfigFactory.create(SEConfig.class, System.getProperties(), System.getenv());
        }
        return config;
    }

}
