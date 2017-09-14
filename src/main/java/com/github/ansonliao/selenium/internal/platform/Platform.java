package com.github.ansonliao.selenium.internal.platform;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class Platform {
    private static Logger logger = Logger.getLogger(Platform.class);
    public static Set<Browser> defaultSupportedBrowsers = new HashSet<>();
    public static Set<Browser> ignoredBrowsers;

    static {
        defaultSupportedBrowsers
                .addAll(Arrays.asList(Browser.values()));
        ignoredBrowsers = defaultSupportedBrowsers;
    }

    public static synchronized OSType getOSType() {
        String osName = System.getProperty("os.name");
        String osNameMatch = osName.toLowerCase();
        OSType osType;

        if (isMac(osNameMatch)) {
            osType = OSType.MAC;
        } else if (isUnix(osNameMatch)) {
            osType = OSType.UNIX;
        } else if (isWindows(osNameMatch)) {
            osType = OSType.WINDOWS;
        } else {
            osType = OSType.SOLARIS;
        }

        return osType;
    }

    public enum OSType {
        MAC("MAC"),
        UNIX("UNIX"),
        WINDOWS("WINDOWS"),
        SOLARIS("SOLARIS");

        private final String name;
        OSType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
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
