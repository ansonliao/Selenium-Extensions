package com.github.ansonliao.selenium.testfixtures;

import org.testng.annotations.Test;

@Test(enabled = false, groups = "smoke")
public class ClassGroupFixture {

    @Test(enabled = false, groups = "regression")
    public void methodWithGroup() {
    }

    @Test(enabled = false)
    public void methodWithoutGroup() {
    }
}
