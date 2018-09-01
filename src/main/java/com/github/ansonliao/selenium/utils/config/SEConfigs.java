package com.github.ansonliao.selenium.utils.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

import java.io.File;
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
        @Key("add.browser.group.to.report")
        @DefaultValue("false")
        boolean addBrowserGroupToReport();

        @Key("run.by.browsers")
        @DefaultValue("")
        @Separator(",")
        List<String> runByBrowsers();

        @Key("default.browser")
        @DefaultValue("Chrome")
        String defaultBrowser();

        @Key("browser.annotation.package")
        @DefaultValue("com.github.ansonliao.selenium.annotations.browser")
        String browserAnnotationPackage();

        @Key("test.tag.class.size.of.testngxml")
        @DefaultValue("10")
        int testTagClassSizeOfTestNgXml();

        @Key("testing.package.names")
        @DefaultValue("")
        @Separator(",")
        List<String> testingPackageNames();

        @Key("testing.browser.names")
        @DefaultValue("")
        @Separator(",")
        List<String> testingBrowserNames();

        @Key("testing.test.groups")
        @DefaultValue("")
        @Separator(",")
        List<String> testingTestGroups();

        @Key("testing.testng.classes")
        @DefaultValue("")
        @Separator(",")
        List<String> testingTestNGClasses();

        @Key("testng.listeners")
        @DefaultValue(
                "com.github.ansonliao.selenium.testng.TestResultListener, "
                        + "com.github.ansonliao.selenium.parallel.SeleniumParallelTestListener")
        @Separator(",")
        List<String> testngListeners();

        @Key("testng.class.prefix")
        @DefaultValue("test")
        String testngTestClassPrefix();

        // key set to "browser.ignore.anntation.prefix" if needed
        @DefaultValue("IGNORE")
        String browserIgnoreAnnotationPrefix();

        // key set to "testng.xml.browser.parameter.key" if needed
        @DefaultValue("browser")
        String testngXmlBrowserParamKey();

        @Key("testng.test.preserve.order")
        @DefaultValue("false")
        boolean testngPreserveOrder();

        // key set to "BROWSER_IGNORE_ANNOTATION_TYPE_PROPERTY" if needed
        @DefaultValue("BROWSER_IGNORE")
        String browserIgnoreAnnotationTypeProp();

        // key set to "BROWSER_ANNOTATION_TYPE_PROPERTY" if needed
        @DefaultValue("BROWSER")
        String browserAnnotationTypeProp();

        // webdriver export property key
        @Key("wd.parameter.key.chrome")
        @DefaultValue("webdriver.chrome.driver")
        String chromeDriverProperty();

        @Key("wd.parameter.key.firefox")
        @DefaultValue("webdriver.gecko.driver")
        String firefoxDriverProperty();

        @Key("wd.parameter.key.phantomjs")
        @DefaultValue("phantomjs.binary.path")
        String phantomjsDriverProperty();

        @Key("wd.parameter.key.edge")
        @DefaultValue("webdriver.edge.driver")
        String edgeDriverProperty();

        @Key("wd.parameter.key.ie")
        @DefaultValue("webdriver.ie.driver")
        String ieDriverProperty();

        @Key("wd.parameter.key.opera")
        @DefaultValue("webdriver.opera.driver")
        String operaDriverProperty();

        @Key("wd.use.mirror")
        @DefaultValue("false")
        boolean useTaobaoMirror();

        // selenium grid, remote webdriver
        @Key("selenium.hub.url")
        @DefaultValue("")
        String seleniumHubUrl();

        @Key("wd.caps.file")
        @DefaultValue("caps/caps.json")
        String capsPath();

    }

    public static synchronized SEConfiguration getConfigInstance() {
        if (config == null) {
            config = ConfigFactory.create(SEConfiguration.class, System.getProperties(), System.getenv());
        }
        return config;
    }

}
