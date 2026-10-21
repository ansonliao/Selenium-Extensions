package io.github.ansonliao.selenium.testfixtures;

import io.github.ansonliao.selenium.annotations.URL;

@URL("https://class.example.com")
public class UrlFixture {

    @URL("https://method.example.com")
    public void methodLevelUrl() {
    }

    public void inheritsClassLevelUrl() {
    }
}
