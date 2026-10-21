package io.github.ansonliao.selenium.testfixtures;

import io.github.ansonliao.selenium.annotations.Retry;

public class RetryFixture {

    @Retry(maxRetry = 2)
    public void retriable() {
    }

    public void plain() {
    }
}
