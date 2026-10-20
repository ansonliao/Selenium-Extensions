package com.github.ansonliao.selenium.testng;

import com.google.common.collect.Multimap;

import com.github.ansonliao.selenium.testfixtures.MethodFilterFixture;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class XmlClassBuilderTest {

    private TestNGFilterState state;

    @BeforeMethod
    public void seedFilterState() {
        state = TestNGFilterState.seed(MethodFilterFixture.class);
    }

    @AfterMethod
    public void restoreFilterState() {
        state.close();
    }

    @Test
    public void buildGroupsClassesByBrowserAndKeepsIncludedMethods() {
        Multimap<String, XmlClass> byBrowser = XmlClassBuilder.build();

        assertEquals(byBrowser.keySet().size(), 1);
        assertTrue(byBrowser.containsKey("CHROME"));

        XmlClass xmlClass = byBrowser.get("CHROME").iterator().next();
        assertEquals(xmlClass.getName(), MethodFilterFixture.class.getCanonicalName());

        List<XmlInclude> includes = xmlClass.getIncludedMethods();
        assertEquals(includes.size(), 3);
        assertTrue(includes.stream().map(XmlInclude::getName).noneMatch("notForChrome"::equals));
    }
}
