package com.github.ansonliao.selenium.testfixtures;

import com.github.ansonliao.selenium.annotations.browser.Chrome;
import com.github.ansonliao.selenium.annotations.browser.Firefox;

@Chrome
public class MixedFixture {

    @Firefox
    public void someTestMethod() {
    }
}
