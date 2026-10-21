package io.github.ansonliao.selenium.testfixtures;

import org.testng.annotations.Test;

public class MethodFinderSample {

    public void helper() {
    }

    @Test(enabled = false, groups = "smoke")
    public void smokeTest() {
    }

    @Test(enabled = false, groups = "regression")
    public void regressionTest() {
    }
}
