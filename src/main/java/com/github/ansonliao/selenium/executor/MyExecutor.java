package com.github.ansonliao.selenium.executor;

import com.github.ansonliao.selenium.internal.platform.Platform;
import com.github.ansonliao.selenium.parallel.ClassFinder;
import com.github.ansonliao.selenium.parallel.MethodFinder;
import com.github.ansonliao.selenium.utils.BrowserUtils;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by ansonliao on 20/2/2017.
 */
public class MyExecutor {

    private Set<Method> chromeTestMethods = new HashSet<>();
    private Set<Method> firefoxTestMethods = new HashSet<>();
    private Set<Method> edgeTestMethods = new HashSet<>();
    private Set<Method> interExplorerTestMethods = new HashSet<>();
    private MethodFinder methodFinder;
    private XmlSuite xmlSuite;
    private static int testClassSize = 50;
    private static Properties properties = new Properties();

    private static final String CONFIG_PROPERTY_FILE = "config.properties";
    private static final String TEST_CLASS_SIZE_PROPERTY = "TEST_CLASS_SIZE";
    static {
        try {
            properties.load(new FileInputStream(CONFIG_PROPERTY_FILE));
        } catch (IOException e) {
            System.out.println(String.format(
                    "Config properties file was not found. [%s%s%s]",
                    System.getProperty("user.dir"), File.separator, CONFIG_PROPERTY_FILE));
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
        Set<Class<?>> classes = new HashSet<>(ClassFinder.findAllTestClassesInPackage(packageName));
        classes.forEach(clazz -> {
            Set<Method> methods = methodFinder.findAllAnnotatedTestMethodInClass(clazz);
            methods.forEach(method -> setTestMethodBrowser(clazz, method));
        });
    }

    public void getAllTestMethods(String packageName, List<String> testClassList) {
        Set<Class<?>> testClassSet = new HashSet<>(ClassFinder.findAllTestClassesInPackage(packageName));
        Set<Class<?>> testClasses = new HashSet<>();
        Set<String> tcl = new HashSet<>(testClassList);
        Map<String, Class<?>> testClassMap = new HashMap<>();

        testClassSet.forEach(testClass -> testClassMap.put(testClass.getSimpleName(), testClass));
        tcl.forEach(testClassName -> {
            if (testClassMap.containsKey(testClassName)) {
                testClasses.add(testClassMap.get(testClassName));
            }
        });

        testClasses.forEach(clazz -> {
            Set<Method> methods = methodFinder.findAllAnnotatedTestMethodInClass(clazz);
            methods.forEach(method -> setTestMethodBrowser(clazz, method));
        });
    }

    private void setTestMethodBrowser(Class clazz, Method method) {
        Set<Platform.BrowserType> browserTypes = BrowserUtils.getMethodSupportedBrowsers(clazz, method);
        if (browserTypes.contains(Platform.BrowserType.CHROME)) {
            chromeTestMethods.add(method);
        }
        if (browserTypes.contains(Platform.BrowserType.FIREFOX)) {
            firefoxTestMethods.add(method);
        }
        if (browserTypes.contains(Platform.BrowserType.Edge)) {
            edgeTestMethods.add(method);
        }
        if (browserTypes.contains(Platform.BrowserType.InternetExplorer)) {
            interExplorerTestMethods.add(method);
        }
        // Default browser is Chrome
        if (!browserTypes.contains(Platform.BrowserType.CHROME)
                && !browserTypes.contains(Platform.BrowserType.FIREFOX)
                && !browserTypes.contains(Platform.BrowserType.Edge)
                && !browserTypes.contains(Platform.BrowserType.InternetExplorer)) {
            chromeTestMethods.add(method);
        }
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

    private Set<XmlTest> createChromeXmlTest(Set<XmlClass> xmlClasses) {
        return createXmlTest(xmlClasses, Platform.BrowserType.CHROME);
    }

    private Set<XmlTest> createFireFoxXmlTest(Set<XmlClass> xmlClasses) {
        return createXmlTest(xmlClasses, Platform.BrowserType.FIREFOX);
    }

    private Set<XmlTest> createEdgeXmlTest(Set<XmlClass> xmlClasses) {
        return createXmlTest(xmlClasses, Platform.BrowserType.Edge);
    }

    private Set<XmlTest> createInternetExplorerXmlTest(Set<XmlClass> xmlClasses) {
        return createXmlTest(xmlClasses, Platform.BrowserType.InternetExplorer);
    }

    private Set<XmlTest> createXmlTest(Set<XmlClass> xmlClasses, Platform.BrowserType browserType) {
        if (xmlClasses == null || xmlClasses.isEmpty()) {
            return null;
        }

        int xmlTestSize = xmlClasses.size()/testClassSize;
        ++xmlTestSize;
        int counter = xmlTestSize;
        Set<XmlTest> xmlTests = new HashSet<>();
        ArrayList<XmlClass> xmlClassArrayList = new ArrayList<>(xmlClasses);

        int startIndex = 0;
        int endIndex = testClassSize;
        while (xmlTestSize > 0) {
            if (xmlClassArrayList.size() == 0) {
                break;
            }
            XmlTest xmlTest = new XmlTest(xmlSuite);
            xmlTest.setName(String.format(
                    "Selenium Test - %s %d", browserType.getName(), xmlTestSize));
            xmlTest.addParameter("browser", browserType.getName());
            xmlTest.setPreserveOrder(false);

            if (xmlClassArrayList.size() < testClassSize) {
                xmlTest.setXmlClasses(xmlClassArrayList.subList(startIndex, xmlClassArrayList.size()));
            } else {
                xmlTest.setXmlClasses(xmlClassArrayList.subList(startIndex, endIndex));
                xmlClassArrayList = new ArrayList<>(xmlClassArrayList.subList(endIndex, xmlClassArrayList.size()));
            }
            xmlTests.add(xmlTest);
//            startIndex = endIndex;
//            endIndex = endIndex + testClassSize;
            --xmlTestSize;
        }

        return xmlTests;

//        XmlTest xmlTest = new XmlTest(xmlSuite);
//        xmlTest.setName("Selenium Test - " + browserType.getName());
//        xmlTest.addParameter("browser", browserType.getName());
//        xmlTest.setPreserveOrder(false);
//        xmlTest.setXmlClasses(new ArrayList<>(xmlClasses));
//        return xmlTest;
    }

    public XmlSuite createXmlSuite() {
        int threadCount = 0;
        if (!isMethodSetEmpty(chromeTestMethods)) {
            ++threadCount;
        }
        if (!isMethodSetEmpty(firefoxTestMethods)) {
            ++threadCount;
        }
        if (!isMethodSetEmpty(edgeTestMethods)) {
            ++threadCount;
        }
        if (!isMethodSetEmpty(interExplorerTestMethods)) {
            ++threadCount;
        }
        xmlSuite = new XmlSuite();
        xmlSuite.setName("Maaii Selenium Test");
        xmlSuite.setThreadCount(threadCount);
        xmlSuite.setParallel(XmlSuite.ParallelMode.TESTS);
        xmlSuite.setVerbose(2);

        Set<XmlClass> xmlChromeClassSet = createXmlClasses(getChromeTestMethods());
        Set<XmlClass> xmlFirefoxClassSet = createXmlClasses(getFirefoxTestMethods());
        Set<XmlClass> xmlEdgeClassSet = createXmlClasses(getEdgeTestMethods());
        Set<XmlClass> xmlIEClassSet = createXmlClasses(getInterExplorerTestMethods());

        Set<XmlTest> xmlChromeTestSet = createChromeXmlTest(xmlChromeClassSet);
        Set<XmlTest> xmlFirefoxTest = createFireFoxXmlTest(xmlFirefoxClassSet);
        Set<XmlTest> xmlEdgeTest = createEdgeXmlTest(xmlEdgeClassSet);
        Set<XmlTest> xmlIETest = createInternetExplorerXmlTest(xmlIEClassSet);
//        xmlChromeTestSet.setSuite(xmlSuite);
//        xmlFirefoxTest.setSuite(xmlSuite);
//        xmlEdgeTest.setSuite(xmlSuite);
//        xmlIETest.setSuite(xmlSuite);

//        xmlTests.forEach(xmlTest -> xmlTest.setSuite(xmlSuite));
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


    /**
    private void setFirefoxTestMethods(Method method, Set<Platform.BrowserType> browserTypes) {
        if (browserTypes.contains(Platform.BrowserType.FIREFOX)) {
            firefoxTestMethods.add(method);
        }
    }

    private void setEdgeTestMethods(Method method, Set<Platform.BrowserType> browserTypes) {
        if (browserTypes.contains(Platform.BrowserType.Edge)) {
            edgeTestMethods.add(method);
        }
    }

    private void setInterExplorerTestMethods(Method method, Set<Platform.BrowserType> browserTypes) {
        if (browserTypes.contains(Platform.BrowserType.InternetExplorer)) {
            interExplorerTestMethods.add(method);
        }
    }
     */

    public Set<Method> getChromeTestMethods() {
        return chromeTestMethods;
    }

    public Set<Method> getFirefoxTestMethods() {
        return firefoxTestMethods;
    }

    public Set<Method> getEdgeTestMethods() {
        return edgeTestMethods;
    }

    public Set<Method> getInterExplorerTestMethods() {
        return interExplorerTestMethods;
    }
}
