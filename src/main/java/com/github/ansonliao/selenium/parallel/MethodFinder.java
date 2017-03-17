package com.github.ansonliao.selenium.parallel;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ansonliao on 16/2/2017.
 */
public class MethodFinder {
    private static Logger logger = Logger.getLogger(MethodFinder.class);

    public Set<Method> findAllAnnotatedTestMethodInClass(Class clazz) {
        Set<Method> methods = findMethodInClass(clazz);
        Set<Method> testMethods = new HashSet<>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Test.class)) {
                testMethods.add(method);
            }
        }
        return testMethods;
    }

    public Set<Method> findMethodInClass(Class clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        return new HashSet<>(Arrays.asList(methods));
    }

    public static void main(String[] args) {
        MethodFinder methodFinder = new MethodFinder();
        String packageName = "com.maaii.automation.selenium.parallel";
        List<Class<?>> classes = ClassFinder.find(packageName);
        for (Class clazz : classes) {
            System.out.println("Class: " + clazz.getName());
            System.out.println("    Methods: ");
            for (Method method : methodFinder.findMethodInClass(clazz)) {
                System.out.println("        " + method.getName());
            }
        }
    }
}
