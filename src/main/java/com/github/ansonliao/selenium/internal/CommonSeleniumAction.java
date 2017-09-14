package com.github.ansonliao.selenium.internal;

import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.internal.interrupt.Sleep;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.htmlelements.element.TypifiedElement;


public class CommonSeleniumAction {
    protected static Logger logger = Logger.getLogger(CommonSeleniumAction.class);
    private WebDriver driver;

    public CommonSeleniumAction(WebDriver driver) {
        this.driver = driver;
    }

    public void click(TypifiedElement element) {
        element.getWrappedElement().click();
        Sleep.byMillisecondWithNoLog(200);
        ExtentTestManager.getExtentTest().log(Status.INFO,
                String.format("Click: [%s]", element.getName()));
    }

    public void type(TypifiedElement element, String value) {
        element.getWrappedElement().sendKeys(value);
        Sleep.byMillisecondWithNoLog(200);
        element.sendKeys(Keys.ESCAPE);
        ExtentTestManager.getExtentTest().log(
                Status.INFO,
                String.format("Type: [%s], Element: [%s]", value, element.getName()));
    }

    public void clearText(TypifiedElement element) {
        element.getWrappedElement().clear();
        ExtentTestManager.getExtentTest().log(
                Status.INFO,
                String.format("Clear Text: [%s]", element.getName()));
    }

    public WebDriver openUrl(String url) {
        driver.get(url);
        ExtentTestManager.getExtentTest().log(
                Status.INFO,
                "Test Start ==> Open Url: " + url);
        return driver;
    }
}
