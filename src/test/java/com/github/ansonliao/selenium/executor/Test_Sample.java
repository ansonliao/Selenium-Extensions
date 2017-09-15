package com.github.ansonliao.selenium.executor;

import com.github.ansonliao.selenium.annotations.Chrome;
import com.github.ansonliao.selenium.annotations.Description;
import com.github.ansonliao.selenium.annotations.Firefox;
import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.internal.UserBaseTest;
import com.github.ansonliao.selenium.internal.interrupt.Sleep;
import com.github.ansonliao.selenium.internal.platform.Browser;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;


/**
 * @author ansonliao
 */
@Chrome
public class Test_Sample extends UserBaseTest {

    /**
     * @author DemoTest
     */
    @Test(groups = {"@BVT", "@BLOCKER"})
    @Firefox
    @URL("https://www.google.com")
    @Description("Test Google search with keywords \"Selenium\"")
    public void Test_Google_Search_Function() {
        openUrl(getUrl());
        HomePage homePage = new HomePage(getDriver());
        homePage.clearText(homePage.po.searchInput);
        homePage.type(homePage.po.searchInput, "Selenium");
        homePage.clickSearchButton();

        Sleep.bySecond(2);
        if (browserName.equals(Browser.FIREFOX.getName())) {
            assertTrue(false, "Test Failed");
        } else {
            assertTrue(getDriver().getCurrentUrl().toLowerCase().contains("selenium"),
                    "Result URL isn't contains Selenium");
        }
    }
}
