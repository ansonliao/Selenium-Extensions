package com.github.ansonliao.selenium.internal.platform;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ansonliao on 20/2/2017.
 */
public class Platform {

    public static Set<Browser> defaultSupportedBrowsers = new HashSet<>();
    public static Set<Browser> ignoredBrowsers;

    static {
        for (Browser browser : Browser.values()) {
            defaultSupportedBrowsers.add(browser);
        }
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
        private OSType(String name) {
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
        return (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") >= 0);
    }

    public static synchronized boolean isSolaris(String osName) {
        return (osName.indexOf("sunos") >= 0);
    }

    public static void main(String[] args) {
        System.out.println(getOSType().getName());
    }
}
