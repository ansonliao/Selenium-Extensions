package testcases;

import com.codeborne.selenide.SelenideDriver;
import com.github.ansonliao.selenium.annotations.Headless;
import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.driver.SelenideManager;
import com.github.ansonliao.selenium.parallel.SeleniumParallelTestListener;
import com.github.ansonliao.selenium.testng.TestResultListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Condition.visible;
import static com.github.ansonliao.selenium.factory.WDManager.getDriver;

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
        driver.$("#username").shouldBe(visible);
        driver.$("#username").setValue("tomsmith");
        System.out.println("Selenide element interaction completed");
    }

}
