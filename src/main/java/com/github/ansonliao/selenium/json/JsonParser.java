package com.github.ansonliao.selenium.json;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonParser {
    private static final Gson GSON_INSTANCE = new Gson();

    public static Gson getGsonInstance() {
        return GSON_INSTANCE;
    }

    public synchronized static JsonElement getJsonElement(JsonElement json, String path) {
        String[] parts = path.split("\\.|\\[|\\]");
        JsonElement result = json;

        for (String key : parts) {
            key = key.trim();
            if (key.isEmpty()) {
                continue;
            }
            if (result == null) {
                result = JsonNull.INSTANCE;
                break;
            }
            if (result.isJsonObject()) {
                result = ((JsonObject) result).get(key);
            } else if (result.isJsonArray()) {
                int ix = Integer.valueOf(key) - 1;
                result = ((JsonArray) result).get(ix);
            } else {
                break;
            }
        }
        return result;
    }

    public synchronized static Map<String, Object> getMapNode(JsonElement json, String path) {
        if (json == null || !isNodeExisted(json, path)) {
            return Maps.newHashMap();
        }
        JsonElement element = getJsonElement(json, path);
        return getGsonInstance().fromJson(element.toString(), Map.class);
    }

    public synchronized static List<Object> getArrayNodeAsList(JsonElement json, String path) {
        if (json == null || !isNodeExisted(json, path)) {
            return Lists.newArrayList();
        }
        JsonElement element = getJsonElement(json, path);
        return getGsonInstance().fromJson(element.toString(), List.class);
    }

    public synchronized static boolean isNodeExisted(JsonElement json, String path) {
        return json == null ? false : !(getJsonElement(json, path) == null);
    }

    public static void main(String[] args) throws FileNotFoundException {
        String jsonFile = "caps/caps_empty.json";
        BufferedReader reader = new BufferedReader(new FileReader(jsonFile));

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        System.out.println(isNodeExisted(jsonObject, "chrome"));
        // System.out.println(isNodeExisted(jsonObject, "chrome.caps"));
        // JsonElement element = getJsonElement(jsonObject, "chrome.caps");
        // System.out.println(element.toString());
        // Map<String, Object> map = gson.fromJson(element.toString(), Map.class);
        // map.forEach((k, v) -> System.out.println("Key: " + k + ", value: " + v));

        // List<String> arglist = gson.fromJson(getJsonElement(jsonObject, "chrome.test"), List.class);
        // System.out.println(isNodeExisted(jsonObject, "chrome.test"));
    }
}
