# Selenium-Extensions

[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/saikrishna321/AppiumTestDistribution)
[![badge-jdk](https://img.shields.io/badge/jdk-21-green.svg)](https://adoptium.net/)
[![Build Status](https://github.com/ansonliao/Selenium-Extensions/actions/workflows/build.yml/badge.svg)](https://github.com/ansonliao/Selenium-Extensions/actions/workflows/build.yml)

A Selenium extension for Java that runs TestNG test cases in parallel across
multiple browsers. Browser sessions are managed by
[Selenide](https://selenide.org/) and results are reported with
[Allure Report](https://allurereport.org/).

> **3.0.0 is a breaking release.** See [Breaking changes](#breaking-changes-in-300).

| | |
|---|---|
| Language / runtime | Java 21+ |
| Build | Gradle 9.6.1 (wrapper included) |
| Test framework | TestNG 7.12.0 |
| Browser automation | Selenium 4.51.0 + Selenide 7.19.0 |
| Reporting | Allure 3.0.0 (built-in), ReportPortal 5.6.8 (optional) |
| Classpath scanning | ClassGraph 4.8.199 |

## Requirements

- Java 21 or newer
- A local browser (Chrome, Firefox, Edge or IE) or a Selenium Grid
- Internet access on first run so Selenium Manager can resolve driver binaries

## Installation

The library is published through [JitPack](https://jitpack.io). Use the
`3.0.0` tag once released, or `master-SNAPSHOT` for the latest commit.

### Maven

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

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ansonliao:Selenium-Extensions:3.0.0'
}
```

## Quick start

```java
import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.annotations.browser.Chrome;
import com.github.ansonliao.selenium.annotations.browser.Firefox;
import org.testng.annotations.Test;

public class LoginTest {

    @Test
    @Chrome
    @URL("https://example.com/login")
    public void loginWithChrome() {
        // page code, see "Using Selenide in tests"
    }

    @Test
    @Firefox
    @URL("https://example.com/login")
    public void loginWithFirefox() {
        // page code
    }
}
```

Create a runner and execute it:

```java
public class MyTestRunner {
    @Test
    public void run() {
        TestNGRunner.run();
    }
}
```

```bash
./gradlew test --tests MyTestRunner   # Gradle
mvn clean test -Dtest=MyTestRunner    # Maven
```

When running test classes directly through Gradle/Maven (without
`TestNGRunner`), register the listeners on the class:

```java
@Listeners({SeleniumParallelTestListener.class, TestResultListener.class})
public class LoginTest { /* ... */ }
```

## How it works

- **Parallel by test.** Each generated TestNG `<test>` tag runs in its own
  thread; parallel mode is `TESTS`.
- **Generated suite.** No `testng.xml` is required: browser annotations are
  scanned and the suite is built programmatically.
- **Per-thread browser.** Every TestNG thread owns one `SelenideDriver`
  instance, so browser sessions never leak between parallel tests.
- **Managed drivers.** Selenium Manager resolves driver binaries; remote runs
  use `selenium.hub.url`.

## Driver management

Browsers are created and closed by Selenide. Driver binaries are resolved by
Selenium Manager (bundled with Selenium 4.6+), so no manual driver download or
`WebDriverManager` configuration is required.

`@Headless` and `@Incognito` are applied per test method. `selenium.hub.url`
points Selenide at a remote Selenium Grid.

### Using Selenide in tests

```java
import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.SelenideDriver;
import com.github.ansonliao.selenium.driver.SelenideManager;

public class LoginPage {

    public void login(String user, String password) {
        SelenideDriver driver = SelenideManager.getSelenideDriver();
        driver.$("#username").setValue(user);
        driver.$("#password").setValue(password);
        driver.$("button[type=submit]").click();
        driver.$(".welcome").shouldBe(visible);
    }
}
```

Raw Selenium access is still available through `WDManager.getDriver()`, which
returns the underlying `WebDriver` of the current thread.

## Annotations

| Annotation | Target | Purpose |
|---|---|---|
| `@Chrome`, `@Firefox`, `@Edge`, `@InternetExplorer` | class, method | Adds the test to the browser matrix |
| `@IgnoreChrome`, `@IgnoreFirefox`, `@IgnoreEdge`, `@IgnoreInternetExplorer` | class, method | Excludes a browser from the matrix |
| `@Headless` | class, method | Runs the browser headless |
| `@Incognito` | class, method | Runs the browser in private/incognito mode |
| `@URL` | class, method | Opens the URL before the test |
| `@Author` / Javadoc `@author` | class, method | Adds Allure `owner` labels |
| `@Description` | class, method | Adds the Allure test description |
| `@Retry(maxRetry = N)` | class, method | Retries a failing test (requires `RetryListener`) |
| `@PageName` | class | Page name metadata for page objects |

To enable retries, add the listener to `testng.listeners`:

```properties
testng.listeners=com.github.ansonliao.selenium.testng.TestResultListener, \
  com.github.ansonliao.selenium.parallel.SeleniumParallelTestListener, \
  com.github.ansonliao.selenium.testng.RetryListener
```

## Reporting

### Allure (built-in)

`allure-testng` is a transitive dependency and its TestNG listener registers
automatically. Every test case carries:

- `owner` labels from `@Author` / Javadoc `@author`
- `tag` labels from TestNG groups (and the browser when
  `add.browser.group.to.report=true`)
- a `browser` label
- an `Open URL: ...` step, plus a `Screenshot` attachment on failure

Results are written to the directory configured by `allure.results.directory`.
In this repository, `src/test/resources/allure.properties` sets it to
`build/allure-results`.

Generate the report:

```bash
./gradlew allureServe    # generate and open a local server
./gradlew allureReport   # static report
```

The static report is written to `build/reports/allure-report/allureReport/`.
The first run downloads the Allure 3 CLI and a private Node.js runtime;
subsequent runs reuse the cache.

**Maven consumers** can generate the report with `allure-maven` 3.1.0:

```xml
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>3.1.0</version>
</plugin>
```

```bash
mvn allure:serve
```

Set `allure.results.directory=target/allure-results` in your own
`allure.properties`.

### Screenshots

Failure screenshots are stored under `target/screenshots` and attached to the
failed test in the Allure report. Capturing requires the browser to have
started; failures before browser startup are reported without a screenshot.

### ReportPortal (optional)

ReportPortal is **not** a transitive dependency: projects without a
ReportPortal server are unaffected. To publish results to ReportPortal, add the
agent to your test classpath and configure it.

Maven:

```xml
<dependency>
    <groupId>com.epam.reportportal</groupId>
    <artifactId>agent-java-testng</artifactId>
    <version>5.6.8</version>
    <scope>test</scope>
</dependency>
```

Gradle:

```groovy
testImplementation 'com.epam.reportportal:agent-java-testng:5.6.8'
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
Screenshots can be published with a consumer-side listener calling
`ReportPortal.emitLog(message, LogLevel.INFO.name(), new Date(), file)`.

> Do not add the ReportPortal agent to projects without a reachable server:
> the listener tries to open a launch at suite start.

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

To run the bundled demo against a real browser:

```bash
./gradlew test --tests testcases.Test_Demo
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

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `No such browser webdriver` | Browser name is not one of `CHROME`, `FIREFOX`, `EDGE`, `IE`, `INTERNETEXPLORER` |
| Browser fails to start | Install the browser; on first run Selenium Manager needs network access to download the driver |
| Allure report is empty | Run the tests before `allureReport`/`allureServe`; verify `allure.results.directory` points at the generated results |
| Allure CLI download fails | The Gradle plugin provisions Node.js on first use; use a mirror or run once with network access, then reuse `.allure/` cache |
| ReportPortal launch fails | Ensure `rp.endpoint` is reachable and the API key/project are valid |
| Screenshot missing on failure | The failure happened before the browser started, or the driver was already closed |
| Tests run twice | Both `TestNGRunner` and direct TestNG discovery are active; use one entry point |

## Breaking changes in 3.0.0

- Build migrated from Maven to Gradle (wrapper included). JitPack coordinates
  are unchanged.
- CI migrated from Travis CI (shut down for open source) to GitHub Actions with
  a JDK 21/25 matrix.
- Requirements raised to **Java 21**; Selenium **4.51.0**, TestNG **7.12.0**.
- **ExtentReports removed**; use Allure 3 (`allure-testng`). `ExtentManager`
  and `ExtentTestManager` were removed.
- **Drivers are managed by Selenide**; the custom factory classes,
  `WebDriverManager` and `wd.use.mirror` were removed. `WDManager` remains as a
  facade for the underlying `WebDriver`.
- **Opera and PhantomJS support removed.**
- `wd.parameter.key.*` configuration keys removed.
- Public API removals: `DriverManager`, `DriverManagerFactory`, the
  `ChromeFactory`/`FirefoxFactory`/`EdgeFactory`/`InternetExplorerFactory`/
  `OperaFactory`/`PhantomJsFactory` classes, `WDMHelper`, `PlatformUtils`,
  `ExtentManager`, `ExtentTestManager`, `Opera`/`PhantomJs` annotations and
  their `Ignore*` variants.

## License

Selenium-Extensions is released under the
[Apache License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
