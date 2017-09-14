package com.github.ansonliao.selenium.executor;

import org.openqa.selenium.support.FindBy;
import ru.yandex.qatools.htmlelements.annotations.Name;
import ru.yandex.qatools.htmlelements.element.Button;
import ru.yandex.qatools.htmlelements.element.TextInput;

/**
 * @author: ansonliao
 * @Date: 2017-Sep-14
 * @Time: 15:05
 */
public class HomePO {

    @FindBy(name = "q")
    @Name("Search Input Field")
    public TextInput searchInput;

    @FindBy(name = "btnK")
    @Name("Search Button")
    public Button searchBtn;

}
