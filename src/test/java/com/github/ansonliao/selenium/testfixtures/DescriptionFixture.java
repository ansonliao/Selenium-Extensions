package com.github.ansonliao.selenium.testfixtures;

import com.github.ansonliao.selenium.annotations.Description;

@Description("   class level description   ")
public class DescriptionFixture {

    @Description("   method level description   ")
    public void described() {
    }

    public void notDescribed() {
    }
}
