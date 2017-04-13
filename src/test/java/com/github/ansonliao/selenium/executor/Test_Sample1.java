package com.github.ansonliao.selenium.executor;

import com.github.ansonliao.selenium.annotations.Chrome;
import com.github.ansonliao.selenium.annotations.RemoteAddress;
import com.github.ansonliao.selenium.factory.UserBaseTest;
import org.testng.annotations.Test;

/**
 * Created by ansonliao on 10/3/2017.
 */

@Chrome
public class Test_Sample1 extends UserBaseTest {

    @Test
    @Chrome
    @RemoteAddress("https://www.google.com")
    public void f1() {
        openRemoteAddress();
        System.out.println("I'm in f1 - " + browserName + " : " + Thread.currentThread().getId());
    }

    @Test
    @RemoteAddress("http://www.m800.com/")
    public void f2() {
        openRemoteAddress();
        System.out.println("I'm in f2 - "+ browserName + " : " + Thread.currentThread().getId());
    }
}
