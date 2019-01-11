package com.github.ansonliao.selenium.json;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.aeonbits.owner.loaders.Loader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JsonLoader implements Loader {

    public boolean accept(URI uri) {
        try {
            URL url = uri.toURL();
            return url.getFile().toLowerCase().endsWith(".json");
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    public void load(Properties result, URI uri) throws IOException {
        Path path = Paths.get(uri);
        List<String> list = Files.readAllLines(path);
        parseJsonString(String.join(System.lineSeparator(), list), result);
    }

    public String defaultSpecFor(String uriPrefix) {
        return uriPrefix + ".json";
    }

    private void parseJsonString(String json, Properties result) {
        com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
        JsonElement element = parser.parse(json);
        parseJsonElement(element, result, "");
    }

    private void parseJsonElement(JsonElement element, Properties result, String targetKey) {
        if (element.isJsonObject()) {
            for (String key : element.getAsJsonObject().keySet()) {
                parseJsonElement(
                        element.getAsJsonObject().get(key),
                        result,
                        targetKey + (targetKey.isEmpty() ? "" : ".") + key);
            }
        } else if (element.isJsonArray()) {
            JsonArray jsonArray = element.getAsJsonArray();
            List<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement jsonElement = jsonArray.get(i);
                if (jsonElement.isJsonObject()) {
                    parseJsonElement(jsonElement, result, targetKey + "[" + i + "]");
                } else if (jsonElement.isJsonPrimitive()) {
                    list.add(jsonElement.getAsString());
                }
            }
            if (!list.isEmpty()) {
                result.put(targetKey, String.join(", ", list));
            }
        } else {
            result.put(targetKey, element.getAsString());
        }
    }

}
