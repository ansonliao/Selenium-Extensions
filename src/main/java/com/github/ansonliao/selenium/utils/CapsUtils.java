package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.json.JsonParser;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.github.ansonliao.selenium.json.JsonParser.getGsonInstance;
import static com.github.ansonliao.selenium.utils.PlatformUtils.getPlatform;
import static com.github.ansonliao.selenium.utils.PlatformUtils.isWindows;
import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

public class CapsUtils {
    private static Logger logger = LoggerFactory.getLogger(CapsUtils.class);
    private static String wdCapsFilePath;
    private static BufferedReader reader;

    static {
        wdCapsFilePath = isWindows(getPlatform().toString())
                ? getConfigInstance().capsPath().replace("\\", "/")
                : getConfigInstance().capsPath();
    }

    public synchronized static JsonElement getCaps() {
        if (!isCapsExisted(wdCapsFilePath)) {
            logger.info("No webdriver desired capabilities Json file found from path : " + wdCapsFilePath);
            return JsonNull.INSTANCE;
        }

        reader = getReader(wdCapsFilePath);
        JsonObject object = getGsonInstance().fromJson(reader, JsonObject.class);
        JsonElement element = JsonParser.getJsonElement(object, "");
        if (element == null) {
            logger.info("WebDriver desired capabilities Json file was found from path: {}, but no Json data retrieved.",
                    wdCapsFilePath);
            return null;
        }
        return element;
    }

    public synchronized Map<String, Object> parseNodeToMap(JsonElement json) {
        return json == null ? Maps.newHashMap() : getGsonInstance().fromJson(json.toString(), Map.class);
    }

    public synchronized List<Object> parseNodeArrayToList(JsonElement json) {
        return json == null ? Lists.newArrayList() : getGsonInstance().fromJson(json.toString(), List.class);
    }

    public synchronized static boolean isCapsExisted(String file) {
        return Files.exists(Paths.get(file));
    }

    public synchronized static BufferedReader getReader(String file) {
        if (isCapsExisted(file)) {
            try {
                return new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        System.setProperty("wd.caps.file", "caps/caps_empty.json");
        System.out.println(getCaps());
    }
}
