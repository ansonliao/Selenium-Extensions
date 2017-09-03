package com.github.ansonliao.selenium.executor;

import com.github.ansonliao.selenium.annotations.Chrome;
import com.github.ansonliao.selenium.annotations.Firefox;
import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.factory.UserBaseTest;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by ansonliao on 10/3/2017.
 */

@Chrome
public class Test_Sample1 extends UserBaseTest {

    @Test
    @Chrome
    @URL("https://www.google.com")
    public void f1() {
        openUrl(getUrl());
        System.out.println("I'm in f1 - " + browserName + " : " + Thread.currentThread().getId());
    }

    @Test
    @Firefox
    @URL("http://www.m800.com/")
    public void f2() {
        openUrl(getUrl());
        System.out.println("I'm in f2 - "+ browserName + " : " + Thread.currentThread().getId());
    }
}
