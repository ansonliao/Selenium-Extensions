package io.github.ansonliao.selenium.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;
import static io.github.ansonliao.selenium.utils.MyFileUtils.isFileExisted;
import static io.github.ansonliao.selenium.utils.config.SEConfigs.getConfigInstance;

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
        init(getConfigInstance().capsPath());
    }

    /**
     * Parses the webdriver capability file at {@code capsPath}. Safe to call
     * only during class initialization or from tests; the production path is the
     * static initializer above.
     */
    static synchronized void init(String capsPath) {
        wdCapsFilePath = capsPath.replace("\\", "/");
        try {
            documentContext = isFileExisted(wdCapsFilePath)
                    ? JsonPath.parse(new File(wdCapsFilePath))
                    : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Testability hook: replaces the parsed capability document without touching
     * the filesystem. Package-private on purpose.
     */
    static synchronized void reset(DocumentContext context) {
        wdCapsFilePath = null;
        documentContext = context;
    }

    public synchronized static boolean isJsonFileEmpty() {
        if (documentContext == null) {
            return true;
        }
        return StringUtils.isNullOrEmpty(documentContext.read("$").toString().trim());
    }

    public synchronized static boolean isPathExists(String path) {
        if (isJsonFileEmpty()) {
            return false;
        }
        String fullPath = path.trim().startsWith("$.") ? path : "$.".concat(path);
        try {
            // DEFAULT_PATH_LEAF_TO_NULL makes a missing leaf read as null instead
            // of throwing, so a null result must be treated as "not exists".
            return documentContext.read(fullPath) != null;
        } catch (PathNotFoundException e) {
            return false;
        }
    }

    private static String pathFor(String browser, String subKey) {
        return "$." + browser + "." + subKey;
    }

    private static <T> T readCapability(String browser, String subKey, Class<T> type) {
        if (isJsonFileEmpty()) {
            return null;
        }
        String exact = pathFor(browser, subKey);
        if (isPathExists(exact)) {
            return documentContext.read(exact, type);
        }
        // Caps files commonly use lower-case browser keys (e.g. "chrome"),
        // while callers may pass an upper-case key, so retry with lower-case.
        String lower = pathFor(browser.toLowerCase(Locale.ROOT), subKey);
        if (isPathExists(lower)) {
            return documentContext.read(lower, type);
        }
        return null;
    }

    public synchronized static Map<String, Object> getCaps(String browser) {
        Map<String, Object> value = readCapability(browser, DESIRED_CAPABILITIES_KEY, Map.class);
        return value != null ? value : Maps.newHashMap();
    }

    public synchronized static List<Object> getCliArgs(String browser) {
        List<Object> value = readCapability(browser, CLI_ARGS_KEY, List.class);
        return value != null ? value : Lists.newArrayList();
    }

    public synchronized static List<Object> getExtensions(String browser) {
        List<Object> value = readCapability(browser, EXTENSIONS_KEY, List.class);
        return value != null ? value : Lists.newArrayList();
    }

    public synchronized static Map<String, Object> getEmulation(String browser) {
        Map<String, Object> value = readCapability(browser, "emulation", Map.class);
        return value != null ? value : Maps.newHashMap();
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
