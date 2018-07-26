package com.github.ansonliao.selenium.utils.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.aeonbits.owner.Config.HotReloadType.ASYNC;

public class SEConfigs {
    private static SEConfiguration config;

    @Config.HotReload(value = 500, unit = MILLISECONDS, type = ASYNC)
    @Config.Sources({
            "classpath:seleniumextensions.properties",
            "classpath:se.properties"})
    public interface SEConfiguration extends Config {

        // SE utils identify keys
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
        String browserAnnotationPackage();

        @Key("testTagClassSizeOfTestNgXml")
        @DefaultValue("10")
        int testTagClassSizeOfTestNgXml();

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
        @DefaultValue(
                "com.github.ansonliao.selenium.testng.TestResultListener, "
                        + "com.github.ansonliao.selenium.parallel.SeleniumParallelTestListener")
        @Separator(",")
        List<String> testngListeners();

        // key set to "browser.ignore.anntation.prefix" if needed
        @DefaultValue("IGNORE")
        String brwoserIgnoreAnnotationPrefix();

        // key set to "testng.xml.browser.parameter.key" if needed
        @DefaultValue("browser")
        String testngXmlBrowserParamKey();

        // key set to "BROWSER_IGNORE_ANNOTATION_TYPE_PROPERTY" if needed
        @DefaultValue("BROWSER_IGNORE")
        String browserIgnoreAnnotationTypeProp();

        // key set to "BROWSER_ANNOTATION_TYPE_PROPERTY" if needed
        @DefaultValue("BROWSER")
        String browserAnnotationTypeProp();

        // webdriver export property key
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

        // selenium grid, remote webdriver
        @Key("selenium.hub.url")
        @DefaultValue("")
        String seleniumHubUrl();

    }

    public static synchronized SEConfiguration getConfigInstance() {
        if (config == null) {
            config = ConfigFactory.create(SEConfiguration.class, System.getProperties(), System.getenv());
        }
        return config;
    }

}
