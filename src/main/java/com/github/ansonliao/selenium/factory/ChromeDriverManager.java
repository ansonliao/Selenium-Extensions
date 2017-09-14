package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.internal.platform.Platform;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;


public class ChromeDriverManager extends DriverManager {

    private ChromeDriverService driverService;

    @Override
    protected void startService() {
        if (driverService == null) {
            try {
                driverService = new ChromeDriverService.Builder()
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
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        ChromeOptions options = new ChromeOptions();
        if (getIsHeadless()) {
            options.addArguments("headless");
        }
        if (getIsIncognito()) {
            options.addArguments("incognito");
        }

        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        driver = new ChromeDriver(driverService, capabilities);
    }

    @Override
    protected String getDriverPath() {
        String path;
        switch (Platform.getOSType()) {
            case MAC:
                path = Constants.CHROME_DRIVER_64BIT_MAC_PATH;
                break;
            case WINDOWS:
                path = Constants.CHROME_DRIVER_32BIT_WINDOWS_PATH;
                break;
            case UNIX:
                path = Constants.CHROME_DRIVER_64BIT_LINUX_PATH;
                break;
            default:
                path = Constants.CHROME_DRIVER_64BIT_MAC_PATH;
                break;
        }
        return path;
    }
}
