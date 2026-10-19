package com.github.ansonliao.selenium.testfixtures;

import com.github.ansonliao.selenium.annotations.Retry;

public class RetryFixture {

    @Retry(maxRetry = 2)
    public void retriable() {
    }

    public void plain() {
    }
}
