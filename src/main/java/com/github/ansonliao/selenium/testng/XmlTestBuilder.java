package com.github.ansonliao.selenium.testng;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

public class XmlTestBuilder {
    private static XmlSuite xmlSuite;

    public static List<XmlTest> build() {
        Set<XmlTest> xmlTestList = Sets.newHashSet();
        Multimap<String, XmlClass> browserXmlclassMap = XmlClassBuilder.build();
        final int DEFAULT_TEST_CLASS_SIZE = getConfigInstance().defaultTestClassesSizeOfTestNGXML();

        browserXmlclassMap.keySet().forEach(browserName -> {
            List<XmlClass> xmlClassList = browserXmlclassMap.get(browserName)
                    .stream().distinct().collect(Collectors.toList());
            ArrayList<XmlClass> tempXmlClass = Lists.newArrayList(xmlClassList);
            int xmlClassGroupSize = xmlClassList.size() / DEFAULT_TEST_CLASS_SIZE + 1;
            int counter = xmlClassGroupSize;

            int startIndex = 0;
            int endIndex = DEFAULT_TEST_CLASS_SIZE;
            int browserIndex = 1;
            while (xmlClassGroupSize > 0) {
                if (tempXmlClass.size() == 0) {
                    break;
                }

                XmlTest xmlTest = new XmlTest(xmlSuite);
                String xmlTestName = browserIndex == 1
                        ? String.format("Selenium Test - %s", browserName)
                        : String.format("Selenium Test - %s %d", browserName, browserIndex);
                xmlTest.setName(xmlTestName);
                xmlTest.addParameter(getConfigInstance().testngXmlBrowserParamKey(), browserName);
                xmlTest.setPreserveOrder(false);

                if (tempXmlClass.size() < DEFAULT_TEST_CLASS_SIZE) {
                    xmlTest.setXmlClasses(tempXmlClass.subList(startIndex, tempXmlClass.size()));
                } else {
                    xmlTest.setXmlClasses(tempXmlClass.subList(startIndex, endIndex));
                    tempXmlClass = Lists.newArrayList(tempXmlClass.subList(endIndex, tempXmlClass.size()));
                }

                xmlTestList.add(xmlTest);
                --xmlClassGroupSize;
                ++browserIndex;
            }
        });

        // add groups
        // if (!getConfigInstance().testingTestGroups().isEmpty()) {
        //    xmlTestList.forEach(xmlTest ->
        //            getConfigInstance().testingTestGroups()
        //                    .forEach(group -> xmlTest.addIncludedGroup(group)));
        // }

        return Lists.newArrayList(xmlTestList);
    }

    public static void setXmlSuite(XmlSuite suite) {
        xmlSuite = suite;
    }

    public static XmlSuite getXmlSuite() {
        return xmlSuite;
    }

}
