package com.github.ansonliao.selenium.executor;

import com.github.ansonliao.selenium.internal.platform.Browser;
import com.github.ansonliao.selenium.internal.platform.Platform;
import com.github.ansonliao.selenium.parallel.ClassFinder;
import com.github.ansonliao.selenium.parallel.MethodFinder;
import com.github.ansonliao.selenium.utils.BrowserUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ansonliao on 20/2/2017.
 */
public class MyExecutor {

    private Set<Method> chromeTestMethods = new HashSet<>();
    private Set<Method> firefoxTestMethods = new HashSet<>();
    private Set<Method> edgeTestMethods = new HashSet<>();
    private Set<Method> interExplorerTestMethods = new HashSet<>();
    private Map<Browser, Set<Method>> browserMethodsMap = new HashMap<>();
    private MethodFinder methodFinder;
    private XmlSuite xmlSuite;
    private static int testClassSize = 50;
    private static PropertiesConfiguration properties = new PropertiesConfiguration();

    private static final String CONFIG_PROPERTY_FILE = "config.properties";
    private static final String TEST_CLASS_SIZE_PROPERTY = "TEST_CLASS_SIZE";

    static {
        try {
            properties.load(CONFIG_PROPERTY_FILE);
        } catch (ConfigurationException e) {
            System.out.println(
                    String.format(
                            "Config properties file was not found. [%s%s%s]",
                            System.getProperty("user.dir"),
                            File.separator,
                            CONFIG_PROPERTY_FILE));
        }

        if (properties.containsKey(TEST_CLASS_SIZE_PROPERTY)) {
            testClassSize = Integer.valueOf(
                    properties.getProperty(TEST_CLASS_SIZE_PROPERTY).toString());
        }

    }

    private static final String separator = ".";

    public MyExecutor() {
        methodFinder = new MethodFinder();
    }

    public void getAllTestMethods(String packageName) {
        Set<Class<?>> classes = new HashSet<>(
                ClassFinder.findAllTestClassesInPackage(packageName));
        classes.forEach(clazz -> {
            Set<Method> methods =
                    methodFinder.findAllAnnotatedTestMethodInClass(clazz);
            methods.forEach(
                    method -> setTestMethodBrowser(clazz, method));
        });
    }

    public void getAllTestMethods(String packageName, List<String> testClassList) {
        Set<Class<?>> testClassSet = new HashSet<>(
                ClassFinder.findAllTestClassesInPackage(packageName));
        Set<Class<?>> testClasses = new HashSet<>();
        Set<String> tcl = new HashSet<>(testClassList);
        Map<String, Class<?>> testClassMap = new HashMap<>();

        testClassSet.forEach(
                testClass -> testClassMap.put(
                        testClass.getSimpleName(), testClass));
        tcl.forEach(testClassName -> {
            if (testClassMap.containsKey(testClassName)) {
                testClasses.add(testClassMap.get(testClassName));
            }
        });

        testClasses.forEach(clazz -> {
            Set<Method> methods =
                    methodFinder.findAllAnnotatedTestMethodInClass(clazz);
            methods.forEach(
                    method -> setTestMethodBrowser(clazz, method));
        });
    }

    private void setTestMethodBrowser(Class clazz, Method method) {
        Set<Browser> browserTypes = BrowserUtils.getMethodSupportedBrowsers(clazz, method);
        Set<Browser> browsers = browserTypes;
        browsers.retainAll(Platform.defaultSupportedBrowsers);

        // default browser is CHROME
        if (browsers.isEmpty()) {
            browsers.add(Browser.getDefaultBrowser());
        }
        browsers.forEach(browser -> {
            if (!browserMethodsMap.containsKey(browser)) {
                browserMethodsMap.put(browser, new HashSet<>());
            }
            Set<Method> ms = browserMethodsMap.get(browser);
            ms.add(method);
            browserMethodsMap.put(browser, ms);
        });
    }

//    private Set<XmlClass> createXmlClasses(Set<Class<?>> testClasses) {
//        Set<XmlClass> xmlClasses = new HashSet<>();
//        testClasses.forEach(testClass -> xmlClasses.add(new XmlClass(testClass.getCanonicalName())));
//        return xmlClasses;
//    }

    private Set<Class<?>> getClassesFromMethod(Set<Method> methods) {
        Set<Class<?>> testClasses = new HashSet<>();
        HashMap<Class<?>, Set<Method>> xmlClassesMap = new HashMap<>();
        methods.forEach(method -> {
            Class<?> clazz = method.getDeclaringClass();
            testClasses.add(clazz);
            if (xmlClassesMap.containsKey(clazz)) {
                xmlClassesMap.get(clazz).add(method);
            } else {
                Set<Method> ms = new HashSet<>();
                ms.add(method);
                xmlClassesMap.put(clazz, ms);
            }
        });
        return testClasses;
    }

    private Set<XmlClass> createXmlClasses(Set<Method> methods) {
        if (methods.isEmpty()) {
            return null;
        }

        Set<XmlInclude> xmlIncludes = new HashSet<>();
        Set<Class<?>> classes = new HashSet<>();
        Map<String, Set<Method>> xmlClassMap = new HashMap<>();
        methods.forEach(method ->
                classes.add(method.getDeclaringClass()));
        classes.forEach(clazz -> {
            Set<Method> ms = new HashSet<>();
            methods.forEach(method -> {
                if (clazz.getCanonicalName().equalsIgnoreCase(method.getDeclaringClass().getCanonicalName())) {
                    ms.add(method);
                }
            });
            xmlClassMap.put(clazz.getCanonicalName(), ms);
        });

        Set<XmlClass> xmlClasses = new HashSet<>();
        xmlClassMap.forEach((k, v) -> {
            String className = k;
            Set<Method> ms = v;
            Set<XmlInclude> xmlIncludeSet = new HashSet<>();
            XmlClass xmlClass = new XmlClass(className);
            ms.forEach(m -> xmlIncludeSet.add(
                        new XmlInclude(m.getName())));
            xmlClass.setIncludedMethods(new ArrayList<>(xmlIncludeSet));
            xmlClasses.add(xmlClass);
        });
        return xmlClasses;
    }

    private Map<Browser, Set<XmlTest>> createXmlTest(Map<Browser, Set<XmlClass>> map) {
        if (map.isEmpty()) {
            return null;
        }

        Map<Browser, Set<XmlTest>> xmlTestMap = new HashMap<>();

        for (Browser browser : map.keySet()) {
            Set<XmlClass> xmlClasses = map.get(browser);
            int xmlTestSize = xmlClasses.size() / testClassSize;
            ++xmlTestSize;
            int counter = xmlTestSize;
            Set<XmlTest> xmlTests = new HashSet<>();
            ArrayList<XmlClass> xmlClassArrayList = new ArrayList<>(xmlClasses);

            int startIndex = 0;
            int endIndex = testClassSize;
            int browserIndex = 1;
            while (xmlTestSize > 0) {
                if (xmlClassArrayList.size() == 0) {
                    break;
                }

                XmlTest xmlTest = new XmlTest(xmlSuite);
                String xmlTestName = browserIndex == 1
                        ? String.format("Selenium Test - %s", browser.getName())
                        : String.format("Selenium Test - %s %d", browser.getName(), browserIndex);
                xmlTest.setName(xmlTestName);
                xmlTest.addParameter("browser", browser.getName());
                xmlTest.setPreserveOrder(false);

                if (xmlClassArrayList.size() < testClassSize) {
                    xmlTest.setXmlClasses(
                            xmlClassArrayList.subList(
                                    startIndex, xmlClassArrayList.size()));
                } else {
                    xmlTest.setXmlClasses(
                            xmlClassArrayList.subList(startIndex, endIndex));
                    xmlClassArrayList = new ArrayList<>(
                            xmlClassArrayList.subList(endIndex, xmlClassArrayList.size()));
                }
                xmlTests.add(xmlTest);
                --xmlTestSize;
                ++browserIndex;
            }

            xmlTestMap.put(browser, xmlTests);
        }

        return xmlTestMap;
    }

    public XmlSuite createXmlSuite() {
        int threadCount = 0;
        xmlSuite = new XmlSuite();
        xmlSuite.setName("Selenium Web UI Test");
        xmlSuite.setParallel(XmlSuite.ParallelMode.TESTS);
        xmlSuite.setVerbose(2);

        Map<Browser, Set<XmlClass>> browserXmlClassMap = new HashMap<>();
        browserMethodsMap.forEach((browser, methods) ->
                browserXmlClassMap.put(browser, createXmlClasses(methods)));

        Map<Browser, Set<XmlTest>> xmlTests = createXmlTest(browserXmlClassMap);
        for (Browser browser : xmlTests.keySet()) {
            threadCount = threadCount + xmlTests.get(browser).size();
        }
        xmlSuite.setThreadCount(threadCount);
        return xmlSuite;
    }

    public void testNGRun(List<XmlSuite> xmlSuites) {
        TestNG testNG = new TestNG();
        testNG.setXmlSuites(xmlSuites);
        testNG.run();
    }

    private boolean isMethodSetEmpty(Set<Method> methodSet) {
        return (methodSet.isEmpty() || methodSet != null) ? false : true;
    }

    public Set<Method> getChromeTestMethods() {
        return browserMethodsMap.get(Browser.CHROME);
    }

    public Set<Method> getFirefoxTestMethods() {
        return browserMethodsMap.get(Browser.FIREFOX);
    }

    public Set<Method> getEdgeTestMethods() {
        return browserMethodsMap.get(Browser.Edge);
    }

    public Set<Method> getInterExplorerTestMethods() {
        return browserMethodsMap.get(Browser.InternetExplorer);
    }

    public static void main(String[] args) {
        System.out.println(Browser.getDefaultBrowser().getName());
    }
}
