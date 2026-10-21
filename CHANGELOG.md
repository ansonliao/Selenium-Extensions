# Changelog

All notable changes to Selenium-Extensions are documented here.
This project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Changed

- **Breaking: Maven group and Java package renamed to `io.github.ansonliao`** —
  the group was `com.github.ansonliao` and the base package was
  `com.github.ansonliao.selenium`. Dependency coordinates, imports and any
  `testng.listeners` / `browser.annotation.package` overrides must be updated.

## [3.0.0] - 2026-10-19

A full modernization of the project: new build system, new browser driver
layer, new reporting stack and a Java 21 baseline. This is a breaking release;
see [Migration notes](#migration-notes) below.

### Added

- **Selenide-managed drivers** — `SelenideManager` and
  `SelenideConfigFactory` create one `SelenideDriver` per TestNG thread, so
  parallel browser runs are isolated. Supports headless, incognito, remote
  Selenium Grid (`selenium.hub.url`) and `caps/caps.json` capabilities.
- **Allure 3 reporting** — `AllureReport` helper plus `allure-testng` 3.0.0 and
  the `io.qameta.allure-report` Gradle plugin 4.3.0. Test cases carry `owner`
  labels (`@Author`/Javadoc), `tag` labels (TestNG groups/browser), a `browser`
  label, and failure screenshots.
- **Selenide element steps in Allure** — the official `allure-selenide` 3.0.0
  listener is registered on every TestNG worker thread, so Selenide actions
  appear as `$("Submit button") click()` steps when elements are named with
  `as(...)` or `@As`. Selenide's `open()` is reported as `$("open") <url>`.
  Selenide screenshots/page sources stay disabled; `ScreenshotManager` remains
  the only failure-screenshot source.
- **Gradle build** — `build.gradle` (Groovy DSL), Gradle 9.6.1 wrapper,
  `maven-publish` for JitPack, sources and Javadoc jars.
- **GitHub Actions CI** — JDK 21/25 matrix, checkstyle, build-only (no browser
  tests).
- **ReportPortal integration (opt-in)** — documented usage with
  `agent-java-testng` 5.6.8; ReportPortal is not a transitive dependency.
- **Tests** — `AuthorUtilsTest` covers annotation, Javadoc, class fallback and
  missing-source behavior; `SelenideAllureStepsTest` covers listener
  install/uninstall idempotency.
- **Documentation** — expanded README (installation for Maven/Gradle, quick
  start, annotations, reporting, troubleshooting) and this changelog.

### Changed

| Component | Before | After |
|---|---|---|
| Build | Maven (`pom.xml`) | Gradle 9.6.1 (Groovy DSL, wrapper) |
| Java | 1.8 | 21 |
| Selenium | 3.141.59 | 4.51.0 |
| TestNG | 6.10 | 7.12.0 |
| Classpath scanner | FastClasspathScanner 2.7.5 | ClassGraph 4.8.199 |
| Reporting | ExtentReports 3.1.3 | Allure 3.0.0 |
| Driver management | Custom factories + WebDriverManager 2.2.5 | Selenide 7.19.0 + Selenium Manager |
| Gson | 2.8.5 | 2.14.0 |
| json-path | 2.4.0 | 3.0.0 |
| qdox | 2.0-M7 | 2.2.0 |
| slf4j | 1.7.24 | 2.0.17 |
| owner | owner-java8 1.0.10 | owner 1.0.12 |
| jfiglet | 0.0.8 | 0.0.9 |
| AShot | 1.5.2 | 1.5.4 |
| Checkstyle | 8.18 | 10.26.1 |
| License | GPL-3.0 in pom / Apache 2.0 in README | Apache 2.0 everywhere |

- **`AuthorUtils` refactor** — qdox is initialized lazily and null-safely;
  all Javadoc `@author` tags are collected; method authors (annotation +
  Javadoc) fall back to class authors; duplicates are removed preserving
  order.
- **`SeleniumParallel#getAuthors`** now delegates to `AuthorUtils` and is
  deprecated.
- **`ClassFinder`** uses a lazy, thread-safe ClassGraph `ScanResult` with
  shutdown-hook cleanup; `getAllClassNames()` replaces the leaked
  `ScanResult` accessor.
- **TestNG internals replaced** — `org.testng.util.Strings` and
  `org.testng.collections.Lists` usages removed; `RetryListener` adapted to
  `getRetryAnalyzerClass()`.

### Removed

- ExtentReports dependency and classes (`ExtentManager`, `ExtentTestManager`).
- Custom driver factories (`DriverManager`, `DriverManagerFactory`,
  `ChromeFactory`, `FirefoxFactory`, `EdgeFactory`, `InternetExplorerFactory`,
  `OperaFactory`, `PhantomJsFactory`), `WDMHelper` and `PlatformUtils`.
- WebDriverManager dependency, the taobao mirror option (`wd.use.mirror`) and
  the `wd.parameter.key.*` configuration keys.
- Opera and PhantomJS browser support and their annotations
  (`Opera`, `PhantomJs`, `IgnoreOpera`, `IgnorePhantomJs`).
- Travis CI configuration (`.travis.yml`).
- Maven build files (`pom.xml`, Maven reporting plugins).

### Fixed

- `INTERNETEXPLOER` typo that made Internet Explorer dispatch always fail
  (the factory map was removed with the Selenide migration).
- `AuthorUtils` NPEs when `src` was missing or a class/method was not found in
  the parsed source tree; multiple Javadoc authors were previously truncated
  to the first one.
- `getClassAuthors` built a list of `boolean` values as a side effect and
  discarded it; it now returns a real author list and `Optional.empty()` when
  none exist.
- gson's removed `JsonParser` constructor replaced with
  `JsonParser.parseString(...)`.
- Checkstyle 10 incompatibilities in `google-style.xml` (`LineLength` moved to
  `Checker`, removed `LeftCurly.maxLineLength` and `LineLength.ignorePattern`)
  and legacy import-order violations.

### Verification

- `./gradlew clean build` passes on JDK 21 (compile, Checkstyle 10.26.1,
  TestNG tests, sources/Javadoc jars).
- CI matrix validates JDK 21 and 25.
- Smoke test run: Selenide 7.19.0 / Selenium 4.51.0 / headless Chrome 155,
  two demo tests passed.
- Allure report generated at
  `build/reports/allure-report/allureReport/index.html`, including `browser`
  labels, URL steps and a linked failure screenshot.

### Migration notes

- Coordinates are unchanged:
  `com.github.ansonliao:Selenium-Extensions:3.0.0` via JitPack.
- **Java 21 is required.** Upgrade the build and CI JDK before upgrading.
- Replace ExtentReports usage with Allure (`allure-testng`); the
  `ExtentManager`/`ExtentTestManager` classes no longer exist.
- Browser creation is owned by Selenide. Use
  `SelenideManager.getSelenideDriver()` for Selenide APIs or
  `WDManager.getDriver()` for the raw `WebDriver`. Custom `DriverManager`
  subclasses must be removed.
- Remove `wd.use.mirror` and `wd.parameter.key.*` from configuration files.
  Driver binaries are resolved by Selenium Manager.
- Remove Opera/PhantomJS annotations from tests; use Chrome, Firefox, Edge or
  Internet Explorer.
- When overriding `testng.listeners`, keep the two default listeners and add
  any custom listeners after them.
