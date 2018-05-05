package com.github.ansonliao.selenium.utils;

import org.openqa.selenium.Platform;

public class PlatformUtils {

    public static synchronized Platform getPlatform() {
        String osName = System.getProperty("os.name");
        String osNameMatch = osName.toLowerCase();
        if (isMac(osNameMatch)) {
            return Platform.MAC;
        } else if (isUnix(osNameMatch)) {
            return Platform.LINUX;
        } else if (isWindows(osNameMatch)) {
            return Platform.WINDOWS;
        } else {
            return Platform.LINUX;
        }
    }

    public static synchronized boolean isWindows(String osName) {
        return (osName.indexOf("win") >= 0);
    }

    public static synchronized boolean isMac(String osName) {
        return (osName.indexOf("mac") >= 0 || osName.indexOf("darwin") >= 0);
    }

    public static synchronized boolean isUnix(String osName) {
        return (osName.indexOf("nix") >= 0
            || osName.indexOf("nux") >= 0
            || osName.indexOf("aix") >= 0);
    }

    public static synchronized boolean isSolaris(String osName) {
        return (osName.indexOf("sunos") >= 0);
    }


}
