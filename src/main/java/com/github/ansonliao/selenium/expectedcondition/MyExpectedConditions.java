package com.github.ansonliao.selenium.expectedcondition;


import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.yandex.qatools.htmlelements.element.TypifiedElement;

import java.util.logging.Logger;

/**
 * Created by ansonliao on 24/2/2017.
 */
public class MyExpectedConditions {

    private static final Logger logger = Logger.getLogger(MyExpectedConditions.class.getName());

    public static ExpectedCondition<Boolean> invisibleOfElement(WebElement element) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                try {
                    return Boolean.valueOf(!element.isDisplayed());
                } catch (NoSuchElementException var3) {
                    return Boolean.valueOf(true);
                } catch (StaleElementReferenceException var4) {
                    return Boolean.valueOf(true);
                }
            }
        };
    }

    public static ExpectedCondition<Boolean> invisibleOfElement(TypifiedElement element) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                try {
                    return Boolean.valueOf(!element.getWrappedElement().isDisplayed());
                }catch (NoSuchElementException var3) {
                    return Boolean.valueOf(true);
                } catch (StaleElementReferenceException var4) {
                    return Boolean.valueOf(true);
                }
            }
        };
    }

    public static ExpectedCondition<Boolean> textPresent(String text) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return webDriver.getPageSource().contains(text);
            }
        };
    }
}
