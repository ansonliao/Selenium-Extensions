package com.github.ansonliao.selenium.testng;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;

import java.lang.reflect.Method;

import static java.util.stream.Collectors.toList;

public class XmlClassBuilder {

    public static Multimap<String, XmlClass> build() {
        Multimap<String, XmlClass> browserXmlClassMap = HashMultimap.create();
        TestNGFilter.run().forEach((browserName, testngClassMap) ->
                testngClassMap.forEach((aClass, methods) -> {
                    XmlClass xmlClass = new XmlClass(aClass.getCanonicalName());
                    xmlClass.setIncludedMethods(methods.parallelStream()
                            .map(Method::getName).map(XmlInclude::new)
                            .distinct().collect(toList()));

                    browserXmlClassMap.put(browserName, xmlClass);
                }));

        return browserXmlClassMap;
    }
}
