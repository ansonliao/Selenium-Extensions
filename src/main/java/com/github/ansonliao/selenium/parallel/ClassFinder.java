package com.github.ansonliao.selenium.parallel;

import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ClassFinder {
    private static Logger logger = Logger.getLogger(ClassFinder.class);
    private static final String PACKAGE_SEPARATOR = ".";
    private static final String DIR_SEPARATOR = File.separator;
    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final String BAD_PACKAGE_ERROR =
            "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

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
}
