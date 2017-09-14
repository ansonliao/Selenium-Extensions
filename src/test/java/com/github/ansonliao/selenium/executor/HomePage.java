package com.github.ansonliao.selenium.executor;

import com.github.ansonliao.selenium.internal.CommonSeleniumAction;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementDecorator;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementLocatorFactory;


public class HomePage extends CommonSeleniumAction {
    HomePO po = new HomePO();

    public HomePage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(
                new HtmlElementDecorator(new HtmlElementLocatorFactory(driver)), po);
    }

    public HomePage clearSearchInput() {
        clearText(po.searchInput);
        return this;
    }

    public HomePage searchKeywords(String value) {
        type(po.searchInput, value);
        return this;
    }

    public HomePage clickSearchButton() {
        click(po.searchBtn);
        return this;
    }


}
