package com.github.ansonliao.selenium.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.util.Strings;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.ansonliao.selenium.utils.MyFileUtils.isFileExisted;
import static com.github.ansonliao.selenium.utils.PlatformUtils.getPlatform;
import static com.github.ansonliao.selenium.utils.PlatformUtils.isWindows;
import static com.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;
import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;

public class CapsUtils {

    public static final String CLI_ARGS_KEY = "cli_args";
    public static final String DESIRED_CAPABILITIES_KEY = "caps";
    public static final String EXTENSIONS_KEY = "extensions";
    public static Configuration GSON_CONFIGURATION = Configuration.builder().jsonProvider(new GsonJsonProvider())
            .options(DEFAULT_PATH_LEAF_TO_NULL).build();

    private static Logger logger = LoggerFactory.getLogger(CapsUtils.class);
    private static String wdCapsFilePath;
    private static DocumentContext documentContext;

    static {
        wdCapsFilePath = isWindows(getPlatform().toString())
                ? getConfigInstance().capsPath().replace("\\", "/")
                : getConfigInstance().capsPath();
        try {
            documentContext = isFileExisted(wdCapsFilePath)
                    ? JsonPath.parse(new File(wdCapsFilePath))
                    : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static boolean isJsonFileEmpty() {
        if (documentContext == null) {
            return true;
        }
        return Strings.isNullOrEmpty(documentContext.read("$").toString().trim());
    }

    public synchronized static boolean isPathExists(String path) {
        if (isJsonFileEmpty()) {
            return false;
        }
        String fullPath = path.trim().startsWith("$.") ? path : "$.".concat(path);
        try {
            documentContext.read(fullPath);
            return true;
        } catch (PathNotFoundException e) {
            return false;
        }
    }

    public synchronized static Map<String, Object> getCaps(String browser) {
        if (isJsonFileEmpty()) {
            return Maps.newHashMap();
        }
        String path = "$.".concat(browser).concat(".").concat(DESIRED_CAPABILITIES_KEY);

        return isPathExists(path) ? documentContext.read(path, Map.class) : Maps.newHashMap();
    }

    public synchronized static List<Object> getCliArgs(String browser) {
        if (isJsonFileEmpty()) {
            return Lists.newArrayList();
        }
        String path = "$.".concat(browser).concat(".").concat(CLI_ARGS_KEY);

        return isPathExists(path) ? documentContext.read(path, List.class) : Lists.newArrayList();
    }

    public synchronized static List<Object> getExtensions(String browser) {
        if (isJsonFileEmpty()) {
            return Lists.newArrayList();
        }
        String path = "$.".concat(browser).concat(".").concat(EXTENSIONS_KEY);
        return isPathExists(path) ? documentContext.read(path, List.class) : Lists.newArrayList();
    }

    public synchronized static Map<String, Object> getEmulation(String browser) {
        if (isJsonFileEmpty()) {
            return Maps.newHashMap();
        }
        String path = "$.".concat(browser).concat(".").concat("emulation");

        return isPathExists(path) ? documentContext.read(path, Map.class) : Maps.newHashMap();
    }

    // public synchronized static JsonElement getCaps() {
    //     if (!isCapsExisted(wdCapsFilePath)) {
    //         logger.info("No webdriver desired capabilities Json file found from path : "
    //                 + wdCapsFilePath);
    //         return JsonNull.INSTANCE;
    //     }
    //
    //     capsJsonReader = getCapsJsonReader(wdCapsFilePath);
    //     JsonObject object = getGsonInstance().fromJson(capsJsonReader, JsonObject.class);
    //     JsonElement element = JsonParser.getJsonElement(object, "");
    //     if (element == null) {
    //         logger.info(
    //                 "WebDriver desired capabilities Json file was found from path: {}, but no Json data retrieved.",
    //                 wdCapsFilePath);
    //         return null;
    //     }
    //     return element;
    // }
    //
    // public synchronized static boolean isCapsExisted(String file) {
    //     return Files.exists(Paths.get(file));
    // }
    //
    // public synchronized static BufferedReader getCapsJsonReader(String file) {
    //     if (isCapsExisted(file)) {
    //         try {
    //             return new BufferedReader(new FileReader(file));
    //         } catch (FileNotFoundException e) {
    //             e.printStackTrace();
    //             return null;
    //         }
    //     } else {
    //         return null;
    //     }
    // }

    public static void main(String[] args) throws IOException {
        System.out.println(getEmulation("chrome"));

        // System.out.println(JsonPath.using(Configuration.builder().jsonProvider(new GsonJsonProvider())
        //         .options(DEFAULT_PATH_LEAF_TO_NULL).build())
        //         .parse(new File(wdCapsFilePath)).read("$.chrome.emulation").toString());
    }

}
