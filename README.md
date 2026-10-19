# Selenium-Extensions
[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/saikrishna321/AppiumTestDistribution)
[![badge-jdk](https://img.shields.io/badge/jdk-21-green.svg)](https://adoptium.net/)
[![Build Status](https://github.com/ansonliao/Selenium-Extensions/actions/workflows/build.yml/badge.svg)](https://github.com/ansonliao/Selenium-Extensions/actions/workflows/build.yml)

A Selenium extension for Java that runs TestNG test cases in parallel across
multiple browsers. Browser sessions are managed by [Selenide](https://selenide.org/)
and results are reported with [Allure Report](https://allurereport.org/).

> **3.0.0 is a breaking release.** See [Breaking changes](#breaking-changes) below.

## Requirements

- Java 21 or newer
- Gradle 9.x (wrapper included) or Maven for consumers

## Maven Dependency

Add the JitPack repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>com.github.ansonliao</groupId>
    <artifactId>Selenium-Extensions</artifactId>
    <version>3.0.0</version>
</dependency>
```

## Build from source

```bash
./gradlew build          # compile, checkstyle, tests, jars
./gradlew build -x test  # build-only (no browser required)
```

## Parallel

TestNG is used to run Selenium test cases in parallel. Parallel mode is by test,
i.e. the `<test>` tag of the TestNG suite XML.

## Visual TestNG test suite XML generation

No need to provide a TestNG suite XML file: the suite is generated
programmatically from the browser annotations.

## Driver management

Browsers are created and closed by Selenide. Each TestNG thread gets its own
`SelenideDriver` instance, so parallel browser runs are isolated. Driver
binaries are resolved by Selenium Manager (bundled with Selenium 4.6+), so no
manual driver download or `WebDriverManager` configuration is required.

`@Headless` and `@Incognito` are applied per test method. `selenium.hub.url`
points Selenide at a remote Selenium Grid.

### Using Selenide in tests

```java
import static com.codeborne.selenide.Condition.visible;
import com.codeborne.selenide.SelenideDriver;
import com.github.ansonliao.selenium.driver.SelenideManager;

public class LoginTest {

    @Test
    @Chrome
    @URL("https://example.com/login")
    public void login() {
        SelenideDriver driver = SelenideManager.getSelenideDriver();
        driver.$("#username").setValue("user");
        driver.$("#password").setValue("secret");
        driver.$("button[type=submit]").click();
        driver.$(".welcome").shouldBe(visible);
    }
}
```

Raw Selenium access is still available through
`WDManager.getDriver()` (the underlying `WebDriver`).

## Report

### Allure report

`allure-testng` writes results to `build/allure-results` (see
`src/test/resources/allure.properties`). Generate the HTML report with:

```bash
./gradlew allureServe    # generate and open in a browser
./gradlew allureReport   # generate static report
```

The static report is written to `build/reports/allure-report/allureReport/`.
The first run downloads the Allure 3 CLI (including a private Node.js runtime);
subsequent runs reuse the cache. Maven consumers can use `allure-maven` or the
`allure` CLI directly.

Each test case carries `owner` labels from `@Author`, `tag` labels from TestNG
groups and a `browser` label. The TestNG suite/test/class/method hierarchy maps
to the Allure suite tree.

### Screenshot for test fail

Screenshots are stored under `target/screenshots` and attached to the failed
test in the Allure report.

### ReportPortal (optional)

ReportPortal is **not** a transitive dependency: projects without a
ReportPortal server are unaffected. To publish results to ReportPortal, add the
agent to your test classpath and configure it:

```xml
<dependency>
    <groupId>com.epam.reportportal</groupId>
    <artifactId>agent-java-testng</artifactId>
    <version>5.6.8</version>
    <scope>test</scope>
</dependency>
```

`src/test/resources/reportportal.properties`:

```properties
rp.endpoint=http://localhost:8080
rp.api.key=<your-api-key>
rp.project=<your-project>
rp.launch=Selenium Extensions
```

The agent registers itself through the TestNG `ServiceLoader`. If you override
`testng.listeners`, add
`com.epam.reportportal.testng.ReportPortalTestNGListener` to the list.
Screenshots can be published with a small consumer-side listener calling
`ReportPortal.emitLog(message, LogLevel.INFO.name(), new Date(), file)`.

## Multiple browsers support

- **Chrome**: Mac, Linux, Windows
- **Firefox**: Mac, Linux, Windows
- **Edge**: Windows
- **InternetExplorer**: Windows

> Opera and PhantomJS were removed in 3.0.0 (Selenium 4 no longer ships their
> drivers).

## Multiple browsers in Runtime

```java
public class TestBrowser {
    @Test
    @Chrome
    public void f1() {
        // your testing code here
    }

    @Test
    @Firefox
    public void f2() {
        // your testing code here
    }
}
```

You can also annotate the test class:

```java
@Chrome
public class TestBrowser {
    @Test
    @Edge
    public void f1() {
        // your testing code here
    }

    @Test
    @Firefox
    public void f2() {
        // your testing code here
    }
}
```

From the above code, test class `TestBrowser` is annotated by `@Chrome`, so all
test methods run in Chrome as well. Finally:

* `f1()`: runs in `Edge` and `Chrome` (if Windows);
* `f2()`: runs in `Firefox` and `Chrome`.

## Dynamic URL support for test method

```java
public class TestBrowser {

    @Test
    @Chrome
    @URL("https://www.google.com/")
    public void f1() {
        // your testing code here
    }

    @Test
    @Firefox
    @URL("https://www.wordpress.com")
    public void f2() {
        // your testing code here
    }
}
```

When the tests start, `https://www.google.com/` is opened for `f1` and
`https://www.wordpress.com` for `f2`.

You can also annotate the test class with `@URL`:

```java
@URL("http://www.google.com")
public class TestBrowser {
    @Test
    @Chrome
    public void f1() {
        // your testing code here
    }

    @Test
    @Firefox
    public void f2() {
        // your testing code here
    }
}
```

Both test cases open `http://www.google.com` automatically.

## Run test case

Create a runner that triggers the testing run:

```java
public class MyTestRunner {
    @Test
    public void run() {
        TestNGRunner.run();
    }
}
```

Run it with Gradle:

```bash
./gradlew test --tests MyTestRunner
```

or with Maven:

```bash
mvn clean test -Dtest=MyTestRunner
```

## Filter

`Selenium-Extensions` supports running test cases by filters:

- *All*: run all test cases in the test project
- *Package*: run test cases by specified packages
- *Test Group*: run test cases by TestNG groups in `@Test`
- *Test class*: run test cases by TestNG test classes
- *Browser*: run test cases by specified browser, such as `CHROME`, `FIREFOX`

### Full configuration list

Create your own configuration named `seleniumextensions.properties` or
`se.properties` and place it under `src/main/resources` or
`src/test/resources`.

| **Key** | **Description** | **Value** | **Default Value**|
| --- | --- | ---| --- |
| add.browser.group.to.report | Whether to add the testing browser as an Allure tag | Boolean: `true` / `false` | `false` |
| run.by.browsers | Run all TestNG tests by the specified browser(s), ignoring browser annotations | `CHROME`, `FIREFOX`, `IE`, `EDGE`, `INTERNETEXPLORER` | null |
| default.browser | Default browser for test cases without a browser annotation | `CHROME`, `FIREFOX`, `IE`, `EDGE`, `INTERNETEXPLORER` | `CHROME` (case insensitive) |
| browser.annotation.package | Package containing the browser annotations | string | `com.github.ansonliao.selenium.annotations.browser` |
| test.tag.class.size.of.testngxml | Number of test classes per `<test>` tag of the generated TestNG XML | positive integer | 10 |
| testing.package.names | Packages containing the test scripts to run | string or list | null |
| testing.browser.names | Run test cases whose browser annotation matches this setting | string or list | null |
| testing.test.groups | Run test cases assigned to these TestNG groups | string or list | null |
| testing.testng.classes | TestNG test classes to execute | string or list | null |
| testng.listeners | Custom TestNG listener(s) | string or list | `com.github.ansonliao.selenium.testng.TestResultListener`, `com.github.ansonliao.selenium.parallel.SeleniumParallelTestListener` |
| testng.class.prefix | Only scan test classes whose name starts with this prefix | string | `test` (case insensitive) |
| selenium.hub.url | Selenium Grid URL; when set, Selenide runs remotely | string | null |
| wd.caps.file | JSON file with extra capabilities, arguments and extensions | string | `caps/caps.json` |

> For list values, use comma (`,`) as the separator.

### Sample configuration: `src/test/resources/seleniumextensions.properties`

```properties
add.browser.group.to.report=true
run.by.browsers=CHROME, FIREFOX
default.browser=CHROME
testing.package.names=example.phase1, example.phase2
test.tag.class.size.of.testngxml=20
testing.test.groups=REGRESSION, SMOKE
```

### Provide filters as command line arguments

Execute `SMOKE` tests only:

```bash
./gradlew test -Dtesting.test.groups=SMOKE
```

or with Maven:

```bash
mvn clean test -Dtest=your_test_runner -Dtesting.test.groups=SMOKE
```

### Provide filters programmatically

Before calling `TestNGRunner.run()`, set the values via system properties or
environment variables:

```java
public class RegressionRunner {
    @Test
    public void runner() {
        System.setProperty("testing.test.groups", "REGRESSION");
        TestNGRunner.run();
    }
}
```

#### Difference between `default.browser` and `testing.browser.names`

- `testing.browser.names`: only test cases whose browser annotation is in this
  list are executed.
- `run.by.browsers`: browser annotations are ignored and every valid test case
  is re-assigned to the browsers in this list.

**Sample:**

```java
public class TestCaseSample {

    @Test(groups = {"Regression", "SMOKE"})
    @Chrome
    public void regression1() {
        // testing code here
    }

    @Test(groups = {"Regression", "BVT"})
    @Firefox
    public void regression2() {
        // testing code here
    }
}
```

1. `run.by.browsers=EDGE, FIREFOX`: both tests run in Edge and Firefox.
2. `testing.browser.names=FIREFOX`: only `regression2()` runs.
3. `testing.browser.names=EDGE`: no test runs (no test is annotated with Edge).

## Breaking changes in 3.0.0

- Build migrated from Maven to Gradle (wrapper included). JitPack coordinates
  are unchanged.
- Requirements raised to **Java 21**; Selenium **4.51.0**, TestNG **7.12.0**.
- **ExtentReports removed**; use Allure 3 (`allure-testng`). `ExtentManager`
  and `ExtentTestManager` were removed.
- **Drivers are managed by Selenide**; the custom factory classes,
  `WebDriverManager` and `wd.use.mirror` were removed. `WDManager` remains as a
  facade for the underlying `WebDriver`.
- **Opera and PhantomJS support removed.**
- `wd.parameter.key.*` configuration keys removed.

## License

Selenium-Extensions is released under the
[Apache License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
