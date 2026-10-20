# Plan: Core Unit Test Suite for Selenium-Extensions

## Context
- Repo: `Selenium-Extensions` (`feature/3.0.0-modernization`, last pushed commit `4704a76`).
- 46 main classes (~2,620 LOC) but only 8 test methods exist (`AuthorUtilsTest`, `SelenideAllureStepsTest`, `Test_Demo`).
- **Problem 1**: `./gradlew test` currently executes `testcases/Test_Demo.java` (real browser demo). Its class-level `@Listeners` leak into the unit run.
- **Problem 2**: CI runs `./gradlew build -x test` → **zero tests execute in CI**.
- **Problem 3**: No coverage tooling.
- Decisions locked with user:
  1. **Scope = Core unit tests** (~11 classes, ~60 cases), browser-free.
  2. **Bugs found by tests are fixed in separate commits** (not mixed with test additions).
  3. **Fix infra so unit tests run in CI + add JaCoCo** (no coverage gate).

## Verified facts (read during planning)
- `Test_Demo` already lives in `src/test/java/testcases/` → excluding `**/testcases/**` cleanly isolates the demo.
- `SelenideConfigFactory` (package-private `final` in `driver`): `normalizeBrowser`, `normalizeHubUrl`, `create` build a `SelenideConfig` **without launching a browser**. `create` reads `@Headless`/`@Incognito` **only on the method**, not the class (bug).
- `SelenideManager` (driver): `createDriver` builds a `SelenideDriver` object (lazy browser) + `SelenideAllureSteps.install()` (Selenide `SelenideLogger` ThreadLocal, **safe without Allure context**). `getWebDriver()`/`hasStartedDriver()` can trigger Chrome launch → **unit tests must avoid them**, assert lifecycle state only.
- `@Headless`/`@Incognito` are already `@Target({TYPE, METHOD})` → class-level fix needs no annotation change.
- `BrowserUtils` depends on `ClassFinder` (ClassGraph scan, cached, no browser). Browser annotations: `Chrome/Firefox/Edge/InternetExplorer` + `Ignore*`, package `com.github.ansonliao.selenium.annotations.browser`, ignore prefix `IGNORE`.
- `CapsUtils` parses `caps/caps.json` from `user.dir` **at class load** (`documentContext` static). With no file → empty maps/lists. Needs a testability hook to inject a fixture.
- `TestNGRetry` has `static maxRetryCount` (re-read from annotation each `retry()` call) + instance `retryCount`. Test via `java.lang.reflect.Proxy` (no Mockito dependency).
- `build.gradle`: `tasks.named('test') { useTestNG() ... }`; checkstyle `maxWarnings=0`, google-style, 4-space, 120 cols; Java 21; no JaCoCo/test-fixtures plugin.
- `SEConfig` is `@Deprecated` (HOCON properties). Lower priority.

## Tested classes & concrete cases
| # | Class (package) | Test cases (browser-free) | Fixtures |
|---|---|---|---|
| 1 | `StringUtils` (utils) | `removeQuoteMark` strips `"`/`'`; `isNullOrEmpty` null/empty; `isNotNullAndNotEmpty` (~6) | none |
| 2 | `SelenideConfigFactory` (driver) | `normalizeBrowser` (chrome/Firefox/EDGE/ie/INTERNETEXPLORER/INTERNET EXPLORER/trim+case/empty→IAE/unknown→IAE); `normalizeHubUrl` (null→"", no suffix→append `/wd/hub`, trailing `/`→`wd/hub`, already `wd/hub` unchanged); `create` → SelenideConfig browser=chrome, headless=false, no remote; `@Headless` method→headless true; `@Incognito`→incognito arg in chrome options (~13) | fixture method via reflection |
| 3 | `SelenideManager` (driver) | `hasDriver()` false→true→false across create/close; `getSelenideDriver()` non-null after create; thread isolation (distinct per thread). **No `getWebDriver()`/`hasStartedDriver()`** (~5) | none |
| 4 | `BrowserUtils` (utils) | `getSupportedBrowsers()` contains CHROME/FIREFOX/EDGE/INTERNETEXPLORER; fixture `@Chrome`→`getClassBrowsers`={CHROME}; `@IgnoreChrome`→ignored={CHROME}; `@Chrome`+`@IgnoreChrome`→supported={}; method+class union; `filterBrowsersFromAnnotations` excludes TEST/HEADLESS/INCOGNITO (~8) | `@Chrome`/`@Firefox`/`@IgnoreChrome` fixture classes (no `@Test`) |
| 5 | `MethodFinder` (parallel) | `findAllAnnotatedTestMethodInClass`→only `@Test`; `findMethodInClass`→all declared; `findTestNGMethodInClassByGroup` method-level filtering; **BUG**: class-level `@Test(groups)` matching currently returns helpers too → assert post-fix returns only `@Test` methods (~6) | fixture class w/ class+method `@Test(groups)` + helper |
| 6 | `TestNGRetry` (testng) | Proxy `ITestResult`/`ITestNGMethod`/`ITestClass` over fixture method `@Retry(maxRetry=N)`: true N times then false; no `@Retry`→false; class-level `@Retry` branch (~5) | fixture w/ `@Retry` |
| 7 | `MyFileUtils` (utils) | `isFileExisted` null/empty→false, existing→true, missing→false; `createScreenshotFolderForBrowser` creates nested dirs idempotently (clean up after) (~5) | temp file/dir |
| 8 | `CapsUtils` (utils) | via testability hook: `getCaps`/`getCliArgs`/`getExtensions`/`getEmulation` for chrome/firefox; missing key→empty; no-file→empty (~6) | `src/test/resources/caps/caps.json` |
| 9 | `AllureReport` (report) | `attachScreenshot(missing)`→no exception/no-op (warn log); `attachScreenshot(real file)` best-effort attach. `describeTest`/`step` need Allure context→skip (~2) | temp png |
| 10 | `TestGroupUtils` (utils) | `getClassTestGroups` no-`@Test`→[], `@Test(groups)`→list; `getMethodTestGroups` class+method union; method w/o `@Test` inherits class groups; default config `testingTestGroups` empty (~5) | fixture `@Test(groups)` |
| 11 | `SEConfig` (utils, **optional**) | `getString` env override + properties fallback (needs `seleniumextensions.properties` in test resources) | props file |

**Total ~60 cases.** Fixtures = plain classes in `src/test/java/.../testfixtures/` **without `@Test`** so TestNG won't run them; no `java-test-fixtures` plugin needed.

## Testability hook (separate low-risk commit)
- `CapsUtils`: extract static init into `init(String path)`; keep current static block calling `init(getConfigInstance().capsPath())`. Add package-private `reset(DocumentContext)` so tests inject a classpath-loaded `caps.json`. Production path unchanged.

## Bug-fix commits (each separate, each with a focused validating test)
1. **MethodFinder**: class-level `@Test(groups)` branch must return only `@Test` methods (drop helpers).
2. **SelenideConfigFactory**: honor **class-level** `@Headless`/`@Incognito` (annotations already TYPE-targeted).
3. **SelenideConfigFactory.normalizeHubUrl**: normalize trailing slash / case consistently (trim, lowercase compare, avoid double `wd/hub` segment).
4. **CapsUtils**: lookup caps with normalized (lower-case) browser key so `chrome`/`CHROME` match.
5. **TestNGRetry**: make `maxRetryCount` instance field re-read per method (robustness against static pollution).

## Infrastructure (separate commit)
`build.gradle`:
- `test` task: `exclude '**/testcases/**'` (keeps `useTestNG()` + logging).
- Add `browserTest` task (`type: Test`, `useTestNG()`, `include '**/testcases/**'`, `shouldRunAfter test`) for local demo runs → `./gradlew browserTest`.
- `-PbrowserTests` → include `testcases` in `test` (optional CI path).
- JaCoCo: `jacoco` plugin; `toolVersion` verify-latest-in-impl; `jacocoTestReport` html+xml → `build/reports/jacoco`; `check.dependsOn jacocoTestReport`; **no coverage gate**; exclude `testfixtures` package from report.
- New test code must pass checkstyle (`maxWarnings=0`).

`.github/workflows/build.yml`:
- `./gradlew build -x test` → `./gradlew build --no-daemon --stacktrace`.
- Add `build/reports/jacoco` to upload-artifact path.
- Keep Java 21/25 matrix.

## Commit sequence (each must stay buildable & green)
1. **infra**: build.gradle (exclude testcases, browserTest task, JaCoCo) + CI → `./gradlew build` runs unit tests, demo excluded.
2. **testability**: CapsUtils `init`/`reset` hook (+ its test).
3–10. **tests**: `StringUtilsTest`, `SelenideConfigFactoryTest`, `SelenideManagerTest`, `BrowserUtilsTest`(+fixtures), `TestGroupUtilsTest`, `MethodFinderTest` (asserts post-fix behavior), `TestNGRetryTest`, `MyFileUtilsTest`, `CapsUtilsTest`(+`caps.json`), `AllureReportTest`.
11–15. **fixes** (each with validating test, committed separately): MethodFinder(#1), SelenideConfigFactory class-level Headless/Incognito(#2), normalizeHubUrl(#3), CapsUtils key casing(#4), TestNGRetry(#5).
16. (optional) `SEConfigTest`.
17. **final**: `./gradlew clean build` all green; verify no `target/screenshots` from unit run (testcases excluded) and JaCoCo report present.

## Verification
- `./gradlew clean build` → all unit tests pass, demo excluded, checkstyle zero warnings.
- `./gradlew browserTest` → demo runs (needs local browser; **not** in CI).
- Assert no `target/screenshots` created during `test`; `build/reports/jacoco/test/html/index.html` exists.

## Risks
- **Static state**: `ClassFinder.scanResult` (cached/shared), `SEConfigs.config` (cached, default empty), `CapsUtils.documentContext` (hooked), `TestNGRetry.maxRetryCount` (fixed). Use distinct fixture classes; keep tests stateless/order-independent.
- **Browser launch**: SelenideManager tests avoid `getWebDriver()`/`hasStartedDriver()`.
- **Checkstyle** on test sources is strict (import order, 4-space, 120 cols).
- **JaCoCo vs Java 25**: verify `toolVersion` during implementation.
