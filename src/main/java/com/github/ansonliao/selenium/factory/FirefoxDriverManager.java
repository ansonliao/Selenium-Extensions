package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.internal.platform.Platform;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;

public class FirefoxDriverManager extends DriverManager {

    private GeckoDriverService driverService;

    @Override
    protected void startService() {
        if (driverService == null) {
            try {
                driverService = new GeckoDriverService.Builder()
                        .usingDriverExecutable(new File(getDriverPath()))
                        .usingAnyFreePort()
                        .build();
                driverService.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void stopService() {
        if (driverService != null && driverService.isRunning()) {
            driverService.stop();
        }
    }

    @Override
    protected void createService() {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        System.setProperty("webdriver.gecko.driver", getDriverPath());
        if (getIsIncognito()) {
            // https://gist.github.com/4M01/9b60a5f852fabdc5f65c9496e6eccf03
            //profile.setPreference("browser.private.browsing.autostart", true);
            //capabilities.setCapability("browser.private.browsing.autostart", true);
            capabilities.setCapability("marionette", true);
        }
        System.out.println("Gecko Driver: " + getDriverPath());
        driver = new FirefoxDriver(capabilities);
    }

    @Override
    protected String getDriverPath() {
        String path;
        switch (Platform.getOSType()) {
            case MAC:
                path = Constants.FIREFOX_DRIVER_64BIT_MAC_PATH;
                break;
            case WINDOWS:
                path = Constants.FIREFOX_DRIVER_64BIT_WINDOWS_PATH;
                break;
            case UNIX:
                path = Constants.FIREFOX_DRIVER_64BIT_LINUX_PATH;
                break;
            default:
                path = Constants.FIREFOX_DRIVER_64BIT_MAC_PATH;
        }
        return path;
    }
}
