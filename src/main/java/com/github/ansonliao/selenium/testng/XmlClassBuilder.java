package com.github.ansonliao.selenium.testng;

import com.github.ansonliao.selenium.utils.WDMHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

public class XmlClassBuilder {

    public static Multimap<String, XmlClass> build() {
        Multimap<String, XmlClass> browserXmlClassMap = HashMultimap.create();
        TestNGFilter.run().forEach((browserName, testngClassMap) ->
                testngClassMap.forEach((aClass, methods) -> {
                    XmlClass xmlClass = new XmlClass(aClass.getCanonicalName());
                    xmlClass.setIncludedMethods(methods.parallelStream()
                            .map(Method::getName).map(XmlInclude::new)
                            .distinct().collect(Collectors.toList()));

                    browserXmlClassMap.put(browserName, xmlClass);
                }));

        /** TODO:
         * until WDM 1.7.2, download WebDriver binary only works at single thread,
         * upgrade WebDriver binary to Async with multi-threads once WDM support
         * multi-thread download
         */
        browserXmlClassMap.keySet().stream()
                .map(String::toUpperCase)
                .forEach(WDMHelper::downloadWebDriverBinary);

        return browserXmlClassMap;
    }
}
