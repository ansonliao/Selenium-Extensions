package io.github.ansonliao.selenium.testfixtures;

import io.github.ansonliao.selenium.annotations.Description;

@Description("   class level description   ")
public class DescriptionFixture {

    @Description("   method level description   ")
    public void described() {
    }

    public void notDescribed() {
    }
}
