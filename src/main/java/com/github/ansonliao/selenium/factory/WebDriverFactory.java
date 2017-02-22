package com.github.ansonliao.selenium.factory;

import com.github.ansonliao.selenium.internal.platform.Platform;
import org.openqa.selenium.WebDriver;


/**
 * Created by ansonliao on 16/2/2017.
 */
public class WebDriverFactory {

    public WebDriver getInstance(Platform.BrowserType browserType) {
        WebDriver webDriver;
        switch (browserType) {
            case CHROME:
                webDriver = new ChromeFactory().getInstance();
                break;
            case FIREFOX:
                webDriver = new FirefoxFactory().getInstance();
                break;
//            case Edge:
//                // add edge factory here
//                break;
//            case InternetExplorer:
//                // add ie factory here
//                break;
            default:
                webDriver = new ChromeFactory().getInstance();
        }
        return webDriver;
    }
}
