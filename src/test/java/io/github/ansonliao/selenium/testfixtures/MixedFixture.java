package io.github.ansonliao.selenium.testfixtures;

import io.github.ansonliao.selenium.annotations.browser.Chrome;
import io.github.ansonliao.selenium.annotations.browser.Firefox;

@Chrome
public class MixedFixture {

    @Firefox
    public void someTestMethod() {
    }
}
