package io.github.ansonliao.selenium.parallel;

import io.github.ansonliao.selenium.annotations.Description;
import io.github.ansonliao.selenium.annotations.Headless;
import io.github.ansonliao.selenium.annotations.Incognito;
import io.github.ansonliao.selenium.annotations.URL;
import io.github.ansonliao.selenium.driver.SelenideManager;
import io.github.ansonliao.selenium.utils.AuthorUtils;
import io.github.ansonliao.selenium.utils.MyFileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SeleniumParallel {
    private static final String FILE_SEPARATOR = File.separator;
    private static final String PROJECT_ROOT_DIR = System.getProperty("user.dir");
    private static final String SCREENSHOT_DIR = PROJECT_ROOT_DIR
            .concat(FILE_SEPARATOR)
            .concat("target")
            .concat(FILE_SEPARATOR)
            .concat("screenshots");
    protected String browserName;
    protected String url;

    public String findUrl(Method method) {
        if (method.isAnnotationPresent(URL.class)) {
            url = method.getAnnotation(URL.class).value();
        } else if (method.getDeclaringClass().isAnnotationPresent(URL.class)) {
            url = method.getDeclaringClass().getAnnotation(URL.class).value();
        } else {
            url = null;
        }

        return url;
    }

    public String getUrl() {
        return url;
    }

    public WebDriver startWebDriver(Method method) {
        SelenideManager.createDriver(browserName, method);
        return SelenideManager.getWebDriver();
    }

    public WebDriver getDriver() {
        return SelenideManager.getWebDriver();
    }

    public WebDriver openUrl(String url) {
        SelenideManager.getSelenideDriver().open(url);
        return getDriver();
    }

    protected String takeScreenShot(String imgPrefix) throws IOException {
        File scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);

        String destDir = SCREENSHOT_DIR
                .concat(FILE_SEPARATOR)
                .concat(this.getClass().getPackage().getName())
                .concat(FILE_SEPARATOR)
                .concat(this.getClass().getSimpleName())
                .concat(FILE_SEPARATOR)
                .concat(this.browserName)
                .concat(FILE_SEPARATOR)
                .concat(imgPrefix)
                .concat("_")
                .concat(String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()))
                .concat(".jpeg");
        MyFileUtils.copyFile(scrFile, new File(destDir));
        return destDir;
    }

    /**
     * @deprecated use {@link AuthorUtils#getMethodAuthors(Method)} directly.
     */
    @Deprecated
    public List<String> getAuthors(String className, Method method) {
        return AuthorUtils.getMethodAuthors(method);
    }

    public List<String> getTestGroups(Object object) {
        Set<String> allGroups = new HashSet<>();
        Test t;

        if (object instanceof Class) {
            Class clazz = (Class) object;
            t = (Test) clazz.getAnnotation(Test.class);
            if (t != null && t.groups().length > 0) {
                allGroups.addAll(Arrays.asList(t.groups()));
            }
            return allGroups.stream().collect(Collectors.toList());
        } else {
            Class clazz = ((Method) object).getDeclaringClass();
            t = (Test) clazz.getAnnotation(Test.class);
            if (t != null && t.groups().length > 0) {
                allGroups.addAll(Arrays.asList(t.groups()));
            }

            t = ((Method) object).getAnnotation(Test.class);
            if (t != null && t.groups().length > 0) {
                allGroups.addAll(Arrays.asList(t.groups()));
            }
            return allGroups.stream().collect(Collectors.toList());
        }
    }

    public String getDescription(Object object) {
        String description = null;
        if (object instanceof Class) {
            Class clazz = (Class) object;
            if (clazz.isAnnotationPresent(Description.class)) {
                return ((Description) clazz.getAnnotation(Description.class))
                        .value().trim();
            }
        }
        if (object instanceof Method) {
            Method method = (Method) object;
            if (method.isAnnotationPresent(Description.class)) {
                return method.getAnnotation(Description.class)
                        .value().trim();
            }
        }

        return description;
    }

    public String withBoldHTML(String s) {
        return !s.trim().isEmpty()
                ? "<b>" + s + "</b>"
                : "";
    }

}
