package com.github.ansonliao.selenium.json;

import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class JsonLoaderTest {

    @Test
    public void acceptOnlyJsonUris() {
        JsonLoader loader = new JsonLoader();
        assertTrue(loader.accept(URI.create("file:/tmp/caps.json")));
        assertFalse(loader.accept(URI.create("file:/tmp/caps.properties")));
    }

    @Test
    public void defaultSpecForAppendsJsonExtension() {
        JsonLoader loader = new JsonLoader();
        assertEquals(loader.defaultSpecFor("classpath:caps"), "classpath:caps.json");
    }

    @Test
    public void loadFlattensNestedJsonIntoProperties() throws Exception {
        Path file = Files.createTempFile("caps", ".json");
        try {
            Files.writeString(file, "{"
                    + "\"chrome\":{"
                    + "\"caps\":{\"platform\":\"linux\"},"
                    + "\"cli_args\":[\"--no-sandbox\",\"--headless\"]"
                    + "}}");

            Properties properties = new Properties();
            new JsonLoader().load(properties, file.toUri());

            assertEquals(properties.get("chrome.caps.platform"), "linux");
            assertEquals(properties.get("chrome.cli_args"),
                    "--no-sandbox, --headless");
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
