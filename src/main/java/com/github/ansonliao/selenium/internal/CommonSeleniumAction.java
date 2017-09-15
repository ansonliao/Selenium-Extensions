package com.github.ansonliao.selenium.internal;

import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.annotations.PageName;
import com.github.ansonliao.selenium.internal.interrupt.Sleep;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.htmlelements.element.TypifiedElement;


public class CommonSeleniumAction {
    protected static Logger logger = Logger.getLogger(CommonSeleniumAction.class);
    private WebDriver driver;
    private String pageName;

    public CommonSeleniumAction(WebDriver driver) {
        this.driver = driver;
    }

    public void click(TypifiedElement element) {
        element.getWrappedElement().click();
        Sleep.byMillisecondWithNoLog(200);
        ExtentTestManager.getExtentTest().log(Status.INFO,
                String.format("%s: Click",
                    generateExtentTestLogMsgPrefix(getPageName(), element.getName())));
    }

    public void type(TypifiedElement element, String value) {
        element.getWrappedElement().sendKeys(value);
        Sleep.byMillisecondWithNoLog(200);
        ExtentTestManager.getExtentTest().log(Status.INFO,
                String.format("%s: Type text: %s",
                        generateExtentTestLogMsgPrefix(getPageName(), element.getName()),
                        value));
    }

    public void clearText(TypifiedElement element) {
        element.getWrappedElement().clear();
        ExtentTestManager.getExtentTest().log(
                Status.INFO,
                String.format("%s: Clear Text",
                        generateExtentTestLogMsgPrefix(getPageName(), element.getName())));
    }

    public WebDriver openUrl(String url) {
        driver.get(url);
        ExtentTestManager.getExtentTest().log(
                Status.INFO,
                withBoldHTML("Test Start"));
        ExtentTestManager.getExtentTest().log(Status.INFO,
                withBoldHTML("Open URL: ") + url);
        return driver;
    }

    public String getPageName() {
        if (pageName == null) {
            if (this.getClass().isAnnotationPresent(PageName.class)) {
                pageName = this.getClass().getAnnotation(PageName.class).value().trim();
            }
        }
        return pageName;
    }

    public String withBoldHTML(String s) {
        return !s.trim().isEmpty()
                ? "<b>" + s + "</b>"
                : "";
    }

    public String generateExtentTestLogMsgPrefix(String pageName, String elementName) {
        if (pageName != null && !pageName.trim().isEmpty()) {
            String log = "[" + pageName.trim();
            if (elementName != null && !elementName.trim().isEmpty()) {
                log += " --> " + elementName + "] ";
                return log;
            } else {
                return "[Page: " + pageName.trim() + "] ";
            }
        } else {
            if (elementName != null && !elementName.trim().isEmpty()) {
                return "[Element: " + elementName + "] ";
            } else {
                return "";
            }
        }
    }
}
