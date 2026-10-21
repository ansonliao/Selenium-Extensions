package testcases;

import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.SelenideElement;
import io.github.ansonliao.selenium.annotations.Headless;
import io.github.ansonliao.selenium.annotations.URL;
import io.github.ansonliao.selenium.driver.SelenideManager;
import io.github.ansonliao.selenium.parallel.SeleniumParallelTestListener;
import io.github.ansonliao.selenium.testng.TestResultListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static io.github.ansonliao.selenium.factory.WDManager.getDriver;

@Listeners({SeleniumParallelTestListener.class, TestResultListener.class})
public class Test_Demo {

    @Test
    @Headless
    @URL("http://the-internet.herokuapp.com/login")
    public void f1() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        System.out.println(getDriver().getCurrentUrl());
        System.out.println(getDriver().getTitle());
    }

    @Test
    @Headless
    @URL("http://the-internet.herokuapp.com/login")
    public void f2() {
        SelenideDriver driver = SelenideManager.getSelenideDriver();

        SelenideElement username = driver.$("#username").as("Username field");
        SelenideElement password = driver.$("#password").as("Password field");
        SelenideElement submit = driver.$("button[type=submit]").as("Submit button");

        username.shouldBe(visible).setValue("tomsmith");
        password.shouldBe(visible).setValue("SuperSecretPassword!");
        submit.click();

        driver.$(".flash.success").as("Flash message")
                .shouldBe(visible)
                .shouldHave(text("You logged into a secure area!"));
    }

}
