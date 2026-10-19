package com.github.ansonliao.selenium.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class JsonParserTest {

    private JsonObject parse(String json) {
        return JsonParser.getGsonInstance().fromJson(json, JsonObject.class);
    }

    @Test
    public void getGsonInstanceReturnsSharedInstance() {
        assertNotNull(JsonParser.getGsonInstance());
        assertTrue(JsonParser.getGsonInstance() == JsonParser.getGsonInstance());
    }

    @Test
    public void getJsonElementReadsNestedObjectPath() {
        JsonObject json = parse("{\"chrome\":{\"caps\":{\"platform\":\"linux\"}}}");
        JsonElement element = JsonParser.getJsonElement(json, "chrome.caps");
        assertNotNull(element);
        assertEquals(element.getAsJsonObject().get("platform").getAsString(), "linux");
    }

    @Test
    public void getJsonElementReadsArrayWithOneBasedIndex() {
        JsonObject json = parse("{\"chrome\":{\"cli_args\":[\"a\",\"b\"]}}");
        assertEquals(JsonParser.getJsonElement(json, "chrome.cli_args[1]").getAsString(), "a");
        assertEquals(JsonParser.getJsonElement(json, "chrome.cli_args[2]").getAsString(), "b");
    }

    @Test
    public void getJsonElementReturnsNullForMissingTopLevelKey() {
        JsonObject json = parse("{\"chrome\":{\"caps\":{}}}");
        assertEquals(JsonParser.getJsonElement(json, "firefox"), null);
    }

    @Test
    public void isNodeExistedTrueForExistingPaths() {
        JsonObject json = parse("{\"chrome\":{\"caps\":{\"platform\":\"linux\"}}}");
        assertTrue(JsonParser.isNodeExisted(json, "chrome"));
        assertTrue(JsonParser.isNodeExisted(json, "chrome.caps"));
    }

    @Test
    public void isNodeExistedFalseForMissingTopLevelKey() {
        JsonObject json = parse("{\"chrome\":{\"caps\":{}}}");
        assertFalse(JsonParser.isNodeExisted(json, "firefox"));
    }

    @Test
    public void isNodeExistedFalseForNullJson() {
        assertFalse(JsonParser.isNodeExisted(null, "chrome"));
    }
}
