package com.github.ansonliao.selenium.report;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;

/**
 * Registers the official Allure Selenide listener on the current thread.
 *
 * <p>Selenide keeps listeners in a {@link ThreadLocal}, so this is invoked per
 * TestNG worker thread from
 * {@link com.github.ansonliao.selenium.driver.SelenideManager} instead of a
 * static initializer.</p>
 */
public final class SelenideAllureSteps {

    public static final String LISTENER_NAME = "selenium-extensions-allure-selenide";

    private static final AllureSelenide LISTENER = new AllureSelenide()
            .screenshots(false)
            .savePageSource(false);

    private SelenideAllureSteps() {
    }

    /**
     * Installs the Allure Selenide listener on the current thread.
     * Calling this method more than once has no effect.
     */
    public static void install() {
        if (!SelenideLogger.hasListener(LISTENER_NAME)) {
            SelenideLogger.addListener(LISTENER_NAME, LISTENER);
        }
    }

    /**
     * Removes the Allure Selenide listener from the current thread, if present.
     */
    public static void uninstall() {
        SelenideLogger.removeListener(LISTENER_NAME);
    }
}
