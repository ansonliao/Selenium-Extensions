package com.github.ansonliao.selenium.parallel;

import com.google.common.collect.Sets;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class ClassFinder {
    private static Logger logger = Logger.getLogger(ClassFinder.class);
    private static final String PACKAGE_SEPARATOR = ".";
    private static final String DIR_SEPARATOR = File.separator;
    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final String TESTNG_TEST_CLASS_PREFIX = "test";
    private static final String BAD_PACKAGE_ERROR =
            "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

    private static FastClasspathScanner classpathScanner;

    static {
        classpathScanner = new FastClasspathScanner()
                .enableMethodAnnotationIndexing();
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
        List<Class<?>> classes = new ArrayList<>();
        for (File file : scannedDir.listFiles()) {
            classes.addAll(find(file, scannedPackage));
        }
        return classes;
    }

    private static List<Class<?>> find(File file, String scannedPackage) {
        List<Class<?>> classes = new ArrayList<>();
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
        Set<Class<?>> annotatedTestClasses = new HashSet<>();
        classes.forEach(clazz -> {
            if (clazz.getName().toLowerCase().contains("test")) {
                annotatedTestClasses.add(clazz);
            }
        });

        return annotatedTestClasses;
    }

    public static List<Class<?>> findAllTestNGTestClasses(String... packages) {
        Set<Class<?>> classes = Sets.newHashSet();
        ScanResult result = classpathScanner.scan();

        // find all testng test classes with class annotated @Test
        result.getNamesOfClassesWithAnnotation(Test.class)
                .stream()
                .map(className -> createClass(className))
                .filter(clazz ->
                        clazz.getSimpleName().toLowerCase().startsWith(TESTNG_TEST_CLASS_PREFIX))
                .forEach(clazz -> classes.add(clazz));

        // find all testng test classes with method annotated @Test
        result.getNamesOfClassesWithMethodAnnotation(Test.class)
                .stream()
                .map(className -> createClass(className))
                .filter(clazz ->
                        clazz.getSimpleName().toLowerCase().startsWith(TESTNG_TEST_CLASS_PREFIX))
                .forEach(clazz -> classes.add(clazz));

        if (packages == null || packages.length == 0) {
            logger.info("Find all TestNG test classes in current project.");
            return classes.stream().collect(Collectors.toList());
        }

        List<String> packageNames = Arrays.asList(packages);
        logger.info("Find TestNG classes in package(s): " + packageNames);

        //TODO: replace forEach() with filter of stream
        classes.stream().collect(Collectors.toList()).forEach(aClass -> {
            if (!packageNames.contains(aClass.getPackage().getName())) {
                classes.remove(aClass);
            }
        });

        return classes.stream().collect(Collectors.toList());
    }

    private synchronized static Class<?> createClass(String className) {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return clazz;
    }
}
