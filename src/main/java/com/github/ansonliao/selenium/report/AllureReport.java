package com.github.ansonliao.selenium.report;

import com.github.ansonliao.selenium.annotations.Description;
import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.github.ansonliao.selenium.utils.StringUtils.isNotNullAndNotEmpty;

/**
 * Thin facade over the Allure runtime API used by the TestNG listeners.
 * Allure's own TestNG listener records pass/fail/skip status; this helper only
 * adds metadata, steps and failure screenshots.
 */
public final class AllureReport {

    private static final Logger logger = LoggerFactory.getLogger(AllureReport.class);

    private AllureReport() {
    }

    public static void describeTest(Method method, String browserName,
                                    List<String> authors, List<String> groups) {
        String description = descriptionOf(method);
        if (isNotNullAndNotEmpty(description)) {
            Allure.description(description);
        }
        Allure.label("browser", browserName);
        if (authors != null) {
            authors.forEach(author -> Allure.label("owner", author));
        }
        if (groups != null) {
            groups.forEach(group -> Allure.label("tag", group));
        }
    }

    public static void step(String message) {
        Allure.step(message);
    }

    public static void attachScreenshot(String absolutePath) {
        Path path = Path.of(absolutePath);
        if (!Files.isRegularFile(path)) {
            logger.warn("Screenshot file not found: {}", absolutePath);
            return;
        }
        try (InputStream stream = Files.newInputStream(path)) {
            Allure.attachment("Screenshot", "image/png", stream,
                    AttachmentOptions.withFileExtension("png"));
        } catch (IOException e) {
            logger.error("Unable to attach screenshot {}", absolutePath, e);
        }
    }

    private static String descriptionOf(Method method) {
        if (method.isAnnotationPresent(Description.class)) {
            return method.getAnnotation(Description.class).value().trim();
        }
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.isAnnotationPresent(Description.class)) {
            return declaringClass.getAnnotation(Description.class).value().trim();
        }
        return "";
    }
}
