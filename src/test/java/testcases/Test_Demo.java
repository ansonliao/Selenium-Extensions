package testcases;

import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.utils.WDMHelper;
import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static com.github.ansonliao.selenium.factory.WDManager.getDriver;

public class Test_Demo {

    @Test
    @URL("http://the-internet.herokuapp.com/login")
    public void f1() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        System.out.println(getDriver().getCurrentUrl());
        System.out.println(getDriver().getTitle());
    }

    @Test
    public void f2() {
        Lists.newArrayList("chrome", "firefox")
                .parallelStream()
                .forEach(WDMHelper::downloadWebDriverBinary);
        System.out.println("completed");
    }

}
