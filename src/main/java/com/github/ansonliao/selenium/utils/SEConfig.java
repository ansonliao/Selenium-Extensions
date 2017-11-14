package com.github.ansonliao.selenium.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SEConfig {
    private static final Logger logger =
            LoggerFactory.getLogger(SEConfig.class);
    private static SEConfig instance = new SEConfig();
    private Config config;

    protected SEConfig() {
        config = ConfigFactory.load(SEConfig.class.getClassLoader(),
                System.getProperty("se.properties", "seleniumextensions.properties"));
    }

    public synchronized static SEConfig getInstance() {
        return instance;
    }

    public static String getString(String key) {
        String value = "";
        if (!key.equals("")) {
            // dots are not allowed in POSIX environmental variables
            value = System.getenv(key.replace(".", "_"));
            if (value == null) {
                value = SEConfig.getInstance().config.getString(key);
            }

        }
        return value;
    }

    public static int getInt(String key) {
        return SEConfig.getInstance().config.getInt(key);
    }

    public static boolean getBoolean(String key) {
        return SEConfig.getInstance().config.getBoolean(key);
    }

    public static boolean isKeyExisted(String key) {
        try {
            logger.info("Checking property key existed or not: [{}].", key);
            String value = getString(key);
            logger.info("Property was found: [{}: {}].", key, value);
        } catch (ConfigException e) {
            logger.info("Property was not found, key = {}.", key);
            return false;
        }
        return true;
    }

}
