package com.github.ansonliao.selenium.utils.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.aeonbits.owner.Config.HotReloadType.ASYNC;

public class SEConfigs {
    private static SEConfiguration config =
            ConfigFactory.create(SEConfiguration.class, System.getProperties(), System.getenv());

    /**
     * TODO: The sources set to classpath doesn't work, need to investigate and solve the problem
     */
    @Config.HotReload(value = 500, unit = MILLISECONDS, type = ASYNC)
    @Config.Sources({"file:src/test/resources/seleniumextensions.properties", "file:src/test/resources/se.properties"})
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
        String defaultBrowserAnnotationPackage();

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
        return config;
    }

    public static void main(String[] args) {
        System.setProperty("selenium.hub.url", "hello");
        System.out.println(getConfigInstance().seleniumHubUrl());
    }

}
