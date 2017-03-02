package com.github.ansonliao.selenium.internal;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.internal.interrupt.Sleep;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.htmlelements.element.TypifiedElement;

/**
 * Created by ansonliao on 17/2/2017.
 */
public class CommonSeleniumAction {
    private WebDriver driver;
    protected ExtentTest extentTest;

    public CommonSeleniumAction(WebDriver driver) {
        this.driver = driver;
    }

    public void click(TypifiedElement element) {
        element.getWrappedElement().click();
        Sleep.byMillisecondWithNoLog(200);
        extentTest.log(Status.INFO, "{Search Page}:::: Element: " + element.getName() + ", Action: [Click]");
    }

    public void type(TypifiedElement element, String value) {
        element.getWrappedElement().sendKeys(value);
        element.sendKeys(Keys.ESCAPE);
        Sleep.byMillisecondWithNoLog(200);
        extentTest.log(Status.INFO, "{Search Page}:::: Element: " + element.getName() + ", Action: Type, Value: " + value);
    }

    public void clearText(TypifiedElement element) {
        element.getWrappedElement().clear();
    }

    public WebDriver openUrl(String url) {
        driver.get(url);
        extentTest.log(Status.INFO, "Test Start ==> Open Url: " + url);
        return driver;
    }

}
