package io.github.ansonliao.selenium.testng;

import io.github.ansonliao.selenium.testfixtures.MethodFilterFixture;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class XmlTestBuilderTest {

    private TestNGFilterState state;

    @BeforeMethod
    public void setUp() {
        state = TestNGFilterState.seed(MethodFilterFixture.class);
        XmlTestBuilder.setXmlSuite(new XmlSuite());
    }

    @AfterMethod
    public void tearDown() {
        state.close();
        XmlTestBuilder.setXmlSuite(null);
    }

    @Test
    public void getXmlSuiteReturnsWhatWasSet() {
        XmlSuite suite = new XmlSuite();
        XmlTestBuilder.setXmlSuite(suite);
        assertSame(XmlTestBuilder.getXmlSuite(), suite);
    }

    @Test
    public void buildCreatesOneTestPerBrowserWithBrowserParameter() {
        List<XmlTest> tests = XmlTestBuilder.build();

        assertEquals(tests.size(), 1);
        XmlTest xmlTest = tests.get(0);
        assertEquals(xmlTest.getName(), "Selenium Test - CHROME");
        assertEquals(xmlTest.getParameter("browser"), "CHROME");
        assertEquals(xmlTest.getXmlClasses().size(), 1);
        assertEquals(xmlTest.getXmlClasses().get(0).getName(),
                MethodFilterFixture.class.getCanonicalName());
        assertNotNull(xmlTest.getSuite());
    }

    @Test
    public void buildLeavesSuiteOrderingUnchangedByDefault() {
        List<XmlTest> tests = XmlTestBuilder.build();

        assertEquals(tests.get(0).getPreserveOrder(), Boolean.FALSE);
    }
}
