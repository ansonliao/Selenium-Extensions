package com.github.ansonliao.selenium.utils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CapsUtilsTest {

    @Test
    public void shouldReturnEmptyWhenNoCapsFileLoaded() {
        CapsUtils.reset(null);
        assertTrue(CapsUtils.isJsonFileEmpty());
        assertTrue(CapsUtils.getCaps("chrome").isEmpty());
        assertTrue(CapsUtils.getCliArgs("chrome").isEmpty());
        assertTrue(CapsUtils.getExtensions("chrome").isEmpty());
        assertTrue(CapsUtils.getEmulation("chrome").isEmpty());
    }

    @Test
    public void shouldReadCapsFromInjectedDocument() {
        String json = "{"
                + "\"chrome\":{"
                + "\"caps\":{\"platform\":\"linux\"},"
                + "\"cli_args\":[\"--no-sandbox\"],"
                + "\"extensions\":[\"e.crx\"],"
                + "\"emulation\":{\"device\":\"pixel\"}"
                + "}}";
        DocumentContext ctx = JsonPath.parse(json);
        CapsUtils.reset(ctx);

        Map<String, Object> caps = CapsUtils.getCaps("chrome");
        assertEquals(caps.get("platform"), "linux");

        List<Object> args = CapsUtils.getCliArgs("chrome");
        assertTrue(args.contains("--no-sandbox"));

        List<Object> extensions = CapsUtils.getExtensions("chrome");
        assertTrue(extensions.contains("e.crx"));

        Map<String, Object> emulation = CapsUtils.getEmulation("chrome");
        assertEquals(emulation.get("device"), "pixel");

        assertFalse(CapsUtils.isJsonFileEmpty());
    }

    @AfterClass
    public void restoreEmptyState() {
        // The parsed document is static; reset it so other test classes still
        // observe the default (no-caps-file) behaviour.
        CapsUtils.reset(null);
    }
}
