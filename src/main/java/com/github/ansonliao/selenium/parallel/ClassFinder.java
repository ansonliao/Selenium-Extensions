package com.github.ansonliao.selenium.parallel;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.github.ansonliao.selenium.utils.StringUtils.removeQuoteMark;
import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;
import static java.util.stream.Collectors.toList;

public class ClassFinder {
    private static final String PACKAGE_SEPARATOR = ".";
    private static final String DIR_SEPARATOR = File.separator;
    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final String TESTNG_TEST_CLASS_PREFIX =
            removeQuoteMark(getConfigInstance().testngTestClassPrefix().toLowerCase().trim());
    private static final String BAD_PACKAGE_ERROR =
            "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";
    private static Logger logger = LoggerFactory.getLogger(ClassFinder.class);
    private static FastClasspathScanner classpathScanner;
    private static ScanResult scanResult;

    static {
        classpathScanner = new FastClasspathScanner().enableMethodAnnotationIndexing();
        scanResult = classpathScanner.scan();
    }

    public static List<Class<?>> findAllTestClassesInPackage(String scannedPackage) {
        List<Class<?>> testClasses = new ArrayList<>();
        List<Class<?>> classes = find(scannedPackage);
        for (Class clazz : classes) {
            if (clazz.getSimpleName().toLowerCase().contains("test")) {
                testClasses.add(clazz);
            }
        }
        return testClasses;
    }

    public static List<Class<?>> find(String scannedPackage) {
        String scannedPath = scannedPackage.replace(PACKAGE_SEPARATOR, DIR_SEPARATOR);
        URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
        if (scannedUrl == null) {
            throw new IllegalArgumentException(
                    String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage));
        }
        File scannedDir = new File(scannedUrl.getFile());
        List<Class<?>> classes = Lists.newArrayList();
        for (File file : scannedDir.listFiles()) {
            classes.addAll(find(file, scannedPackage));
        }
        return classes;
    }

    private static List<Class<?>> find(File file, String scannedPackage) {
        List<Class<?>> classes = Lists.newArrayList();
        String resource = scannedPackage + PACKAGE_SEPARATOR + file.getName();
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                classes.addAll(find(child, resource));
            }
        } else if (resource.endsWith(CLASS_FILE_SUFFIX)) {
            int endIndex = resource.length() - CLASS_FILE_SUFFIX.length();
            String className = resource.substring(0, endIndex);
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }

    public static Set<Class<?>> findAnnotatedTestClasses(List<Class<?>> classes) {
        Set<Class<?>> annotatedTestClasses = Sets.newHashSet();
        classes.forEach(clazz -> {
            if (clazz.getName().toLowerCase().startsWith(TESTNG_TEST_CLASS_PREFIX)) {
                annotatedTestClasses.add(clazz);
            }
        });

        return annotatedTestClasses;
    }

    public static List<Class<?>> findAllTestNGTestClasses(String... packages) {
        Set<Class<?>> classes = Sets.newHashSet();
        //ScanResult result = classpathScanner.scan();

        // find all testng test classes with class annotated @Test
        scanResult.getNamesOfClassesWithAnnotation(Test.class)
                .parallelStream()
                .map(className -> createClass(className))
                .filter(clazz ->
                        clazz.getSimpleName().toLowerCase().startsWith(TESTNG_TEST_CLASS_PREFIX))
                .filter(clazz -> clazz.getAnnotation(Test.class).enabled())
                .forEach(clazz -> classes.add(clazz));

        // find all testng test classes with method annotated @Test
        scanResult.getNamesOfClassesWithMethodAnnotation(Test.class)
                .parallelStream()
                .map(className -> createClass(className))
                .filter(clazz ->
                        clazz.getSimpleName().toLowerCase().startsWith(TESTNG_TEST_CLASS_PREFIX))
                .filter(clazz -> !clazz.isAnnotationPresent(Test.class)
                        || clazz.getAnnotation(Test.class).enabled())
                .forEach(clazz -> classes.add(clazz));

        if (packages == null || packages.length == 0) {
            logger.info("Find all TestNG test classes in current project.");
            return classes.stream().collect(toList());
        }

        List<String> packageNames = Arrays.asList(packages);
        logger.info("Find TestNG classes in package(s): " + packageNames);

        //TODO: replace forEach() with filter of stream
        classes.stream().collect(toList()).forEach(aClass -> {
            if (!packageNames.contains(aClass.getPackage().getName())) {
                classes.remove(aClass);
            }
        });

        return classes.stream().collect(toList());
    }

    public synchronized static Class<?> createClass(String className) {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return clazz;
    }

    public static ScanResult getScanAllClassesResult() {
        return scanResult;
    }
}
