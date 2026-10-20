# Selenium-Extensions 测试补充计划

> 分析对象：`/Users/ansonliao/Documents/repo/github/Selenium-Extensions`
> 分支：`feature/3.0.0-modernization` @ `4704a76`
> 方式：只读探索，未修改任何源文件。

---

## 0. 结论摘要

- `src/main/java` 共 **46 个类 / 约 2,622 行**（含 15 个注解）。
- 现有测试只有 3 个类：`AuthorUtilsTest`、`SelenideAllureStepsTest`、`AuthorFixture`，外加一个会真正启动浏览器的 `testcases.Test_Demo`。
- CI 使用 `./gradlew build -x test`，**当前所有测试在 CI 中都被跳过**。
- 最值得补测的 6 个类：`SelenideConfigFactory`、`CapsUtils`、`SelenideManager`、`TestNGFilter`、`TestNGRetry`、`BrowserUtils`。
- 探索中发现 **20+ 个可由单测直接暴露的缺陷/边界**，其中 8 个属于“静默产生错误测试套件或 NPE”级别，见第 5 节。
- 建议顺序：先做 5 项测试基建改造（第 1 节），再按 P0 → P3 补测；死代码类优先删除而不是补测。

---

## 1. 测试基础设施（必须先做）

### 1.1 隔离会启动浏览器的 Demo

`build.gradle` 中 `test { useTestNG() }` 会扫描并执行 `testcases.Test_Demo`，它使用 `@URL` 和真实 Chrome，CI 无法运行。新增单测前必须把浏览器测试移出默认 `test` 任务：

```groovy
test {
    useTestNG()
    exclude '**/testcases/**'
}

tasks.register('browserTest', Test) {
    useTestNG()
    include '**/testcases/**'
}
```

否则 `./gradlew test` 会在无浏览器环境中失败或长时间挂起，新增单测也无法在 CI 落地。

### 1.2 静态状态重置钩子

以下静态状态在同一个 JVM 内无法切换，是测试 flaky 的根源：

| 类 | 静态状态 | 影响 |
|---|---|---|
| `utils/config/SEConfigs` | `private static SEConfiguration config` | 首次调用后 `-D` 覆盖失效 |
| `utils/CapsUtils` | `wdCapsFilePath`、`documentContext`（static 块读取文件） | 只能测一个 caps 文件；坏文件导致类初始化失败 |
| `parallel/ClassFinder` | `scanResult`、`TESTNG_TEST_CLASS_PREFIX` | 前缀在类加载时固化 |
| `testng/TestNGFilter` | `testngClasses`、`testNGClass2MethodMap`（static 块扫描） | `run()` 不可重复、状态累积 |
| `testng/XmlTestBuilder` | `private static XmlSuite xmlSuite` | 构建顺序耦合，不能并行 |
| `testng/TestNGRetry` | `private static int maxRetryCount` | 并发下互相污染（本身是缺陷） |

建议在 main 代码加**包级可见**的测试钩子（不改变公共 API）：

- `SEConfigs.reset()`：把 `config` 置空。
- `CapsUtils.reload(Path)` 或 `CapsUtils.reset()`：重新解析指定文件。
- `TestNGFilter.reset(List<Class<?>>, Multimap<...>)`：注入测试类/方法映射，绕过 static 扫描。
- `XmlTestBuilder.reset()`：清空 `xmlSuite`。

如果不想改 main 代码，替代方案是 Gradle `test { forkEvery = 1 }` + 每个测试类自己设置 `-D`，但会显著变慢，且仍无法在同一测试类内切换 caps 文件。**推荐加钩子**。

### 1.3 测试 Fixtures 的放置

`ClassFinder` 依赖类名以 `test` 开头（默认 `testng.class.prefix=test`），所以发现类测试的 fixture 必须命名为 `TestXxx`。但 Gradle 的 TestNG 任务会把 `src/test/java` 下所有带 `@Test` 的类当作测试执行。

推荐使用独立的 fixture source set，避免 fixture 被 Gradle 当作测试运行，同时仍位于 test runtime classpath（ClassGraph 能扫描到）：

```groovy
sourceSets {
    testFixtures {
        java.srcDir 'src/testFixtures/java'
        compileClasspath += sourceSets.main.output
        runtimeClasspath += sourceSets.main.output
    }
}
dependencies {
    testImplementation sourceSets.testFixtures.output
}
```

需要的 fixture：

| Fixture | 用途 |
|---|---|
| `fixtures/GroupFixture` | 类级/方法级 `@Test(groups=...)`、`@Test(enabled=false)`、helper 方法、继承、重载 |
| `fixtures/BrowserFixture` | `@Chrome`/`@Firefox`/`@IgnoreChrome` 的类级/方法级组合 |
| `fixtures/RetryFixture` | 方法级 `@Retry(2)`、类级 `@Retry(1)`、方法覆盖类、负值 |
| `fixtures/DescriptionFixture` | `@Description` 类级/方法级/空白 |
| `fixtures/AuthorFixture` | 已有；补充重载方法、多个 `@author`、`@author:` |
| `fixtures/TestDiscoveredClass` | 类级 `@Test`、类名以 Test 开头（用于 `ClassFinder`） |
| `fixtures/TestDiscoveredMethodClass` | 仅方法级 `@Test`、类名以 Test 开头 |
| `fixtures/TestDisabledClass` | `@Test(enabled=false)` |
| `fixtures/TestNotPrefixedClass` | `@Test` 但类名不以 Test 开头（应被排除） |
| `fixtures/DefaultPackageFixture` | 默认包类，用于验证 `getPackage()==null` 的 NPE |

### 1.4 断言与桩

项目没有 Mockito/AssertJ。建议：

- 断言继续使用 `org.testng.Assert`。
- `ITestResult`、`ITestNGMethod`、`ITestAnnotation` 用 `java.lang.reflect.Proxy` 或手写轻量 stub，避免新增依赖。
- 如果 listener 测试数量较多，可加 `testImplementation 'org.mockito:mockito-core:5.x'`，但不是必需。

### 1.5 Checkstyle

`checkstyle` 插件默认覆盖 `src/test/java`，新测试必须满足 `google-style.xml`（行宽 120、import 顺序、Javadoc 规则等）。提交前跑 `./gradlew checkstyleTest`。

---

## 2. 全量类目录

说明：
- **可测性**分为：`纯函数`、`需 fixture 文件`、`需 TestNG 运行时`、`需 Allure 上下文`、`需浏览器`、`难测试（静态初始化/全局状态）`、`死代码`。
- 每个类列出 2–4 个用例；标 `[需浏览器]` 等后缀的用例不纳入默认单测任务。
- 标 `[当前会失败]` 的用例用于暴露已确认或高度可疑的缺陷。

### 2.1 `annotations`（15 个类，12–15 LOC）

| 类 | 方法 | 状态/依赖 | 可测性 |
|---|---|---|---|
| `com.github.ansonliao.selenium.annotations.Author` | `String[] value()` | RUNTIME, TYPE+METHOD | 纯元数据 |
| `...annotations.Description` | `String value()` | RUNTIME, TYPE+METHOD | 纯元数据 |
| `...annotations.Headless` | `String value()` | RUNTIME, TYPE+METHOD | 纯元数据 |
| `...annotations.Incognito` | `String value()` | RUNTIME, TYPE+METHOD | 纯元数据 |
| `...annotations.PageName` | `String value()` | RUNTIME, TYPE only | 纯元数据 |
| `...annotations.Retry` | `int maxRetry()` 默认 0 | RUNTIME, TYPE+METHOD | 纯元数据 |
| `...annotations.URL` | `String value()` | RUNTIME, TYPE+FIELD+METHOD | 纯元数据 |
| `...annotations.browser.Chrome/Firefox/Edge/InternetExplorer` | `value()`, `type()` | RUNTIME, TYPE+METHOD | 纯元数据 |
| `...annotations.browser.IgnoreChrome/IgnoreFirefox/IgnoreEdge/IgnoreInternetExplorer` | `value()`, `type()` | RUNTIME, TYPE+METHOD | 纯元数据 |

**候选测试（一个 `AnnotationMetaTest` 覆盖全部）**：

1. 反射遍历两个注解包，断言所有注解都有 `@Retention(RUNTIME)`。
2. 浏览器注解的 `@Target` 包含 `TYPE` 和 `METHOD`；`@URL` 额外包含 `FIELD`；`@PageName` 只有 `TYPE`。
3. 所有 `Ignore*` 注解简单名以 `IGNORE` 开头（这是 `BrowserUtils` 的隐式契约）。
4. `@Retry.maxRetry()` 默认值为 0；`@Author.value()` 默认是 `{""}`（注意不是空数组）。

风险：注解 target/retention 一旦回退，整套基于反射的发现和浏览器过滤会静默失效。

### 2.2 `driver` 包

#### `com.github.ansonliao.selenium.driver.SelenideConfigFactory` — 172 LOC

- 可见性：**package-private final class**（测试类必须放在 `com.github.ansonliao.selenium.driver` 包）。
- 方法：`static SelenideConfig create(String, Method)`、`static String normalizeBrowser(String)`、`static String normalizeHubUrl(String)`；private `buildCapabilities`、`extensionsAsFiles`、`incognitoArgument`。
- 静态/全局：无自身静态状态；读取 `SEConfigs` 单例、`CapsUtils` 静态 `DocumentContext`、系统属性 `user.dir`。
- 外部依赖：SelenideConfig、Selenium Options、caps JSON 文件、配置属性/环境变量。
- 可测性：**纯函数为主，easy**；`create` 不需要启动浏览器，`new SelenideDriver(config)` 也不会启动。caps 场景需要 fixture 文件 + 静态重置。

**无需浏览器的用例**：

1. `normalizeBrowser` 表驱动：`"chrome"`、`"CHROME"`、`" Chrome "`、`"firefox"`、`"edge"`、`"ie"`、`"internetexplorer"`、`"Internet Explorer"` → 对应规范名；`null`、`""`、`" "`、`"safari"`、`"opera"` → `IllegalArgumentException`（注意 `" "` 会走到 default 分支，错误消息包含原始输入）。
2. `normalizeHubUrl` 表驱动：`null`/`""` → `""`；`http://grid:4444` → `.../wd/hub`；`.../wd/hub` 不变；`.../` → `.../wd/hub`。**[当前会失败]** `.../wd/hub/` 会变成 `.../wd/hub/wd/hub`；`.../WD/HUB` 会重复追加；带前后空格的 URL 不 trim。建议修成：trim → 去尾部 `/` → 大小写不敏感判断 `wd/hub`。
3. `create("chrome", plainMethod)`：`browser()=="chrome"`、`headless()==false`、`screenshots()==false`、`savePageSource()==false`、`reportsFolder()=="target/screenshots"`、`remote()` 为 null、capabilities 是 `ChromeOptions`。
4. `create(null, plainMethod)` 与 `create("", plainMethod)` 都回退到 `SEConfigs.defaultBrowser()`（默认 `Chrome` → `chrome`）。
5. `create("chrome", @Headless @Incognito method)`：`headless()==true`，`ChromeOptions.getArguments()` 含 `--incognito`；Firefox 含 `-private`；Edge 含 `--inprivate`。
6. `create("ie", @Headless @Incognito method)`：`headless()==false`，capabilities 是 `InternetExplorerOptions`，无 private 参数，并记录 warn。
7. **[当前会失败/设计缺陷]** 类级 `@Headless` / `@Incognito`：`create` 只检查 `method.isAnnotationPresent(...)`，不看 `method.getDeclaringClass()`；但两个注解的 `@Target` 都包含 `TYPE`，README 也宣称类级可用。用类级注解 fixture 断言当前 `headless()==false` 且无 incognito 参数；修复后改为 true。
8. caps fixture：`CHROME.cli_args` 含重复项、空白项、`null` 项 → 断言 trim + 去重；**[当前会失败]** `null` 会被 `String.valueOf` 变成字符串 `"null"` 而不是被过滤。`caps` 中的键写入 `getCapability`；`extensions` 被加入 ChromeOptions。
9. **[当前会失败]** `create(" IE ", plainMethod)` / `create("internetexplorer", ...)`：`browserKey` 用的是原始 `resolvedBrowser.toUpperCase()`，不是规范化后的 `browser`，导致 caps 查找键为 `" IE "` / `"INTERNETEXPLORER"`，与 JSON 中的 `IE` 不匹配。建议以 `browser` 计算 key。

**需要浏览器**：无（创建 config 不启动 driver）。若断言 `SelenideDriver` 真正打开 URL，则属于浏览器测试。

#### `com.github.ansonliao.selenium.driver.SelenideManager` — 68 LOC

- 方法：`createDriver(String, Method)`、`getSelenideDriver()`、`getWebDriver()`、`hasDriver()`、`hasStartedDriver()`、`closeDriver()`（全部 public static）。
- 静态/全局：`private static final ThreadLocal<SelenideDriver> DRIVER`；调用 `SelenideAllureSteps.install()/uninstall()`（SelenideLogger 自身也是 ThreadLocal）。
- 外部依赖：Selenide 驱动生命周期、Allure-Selenide、TestNG `Method`。
- 可测性：**ThreadLocal 生命周期 easy**；真正启动/关闭浏览器需要浏览器。

**无需浏览器的用例**：

1. 新线程未创建 driver：`hasDriver()==false`、`hasStartedDriver()==false`、`getWebDriver()==null`、`getSelenideDriver()` 抛 `IllegalStateException` 且消息包含 "No Selenide driver"。
2. `createDriver("chrome", plainMethod)` 后：`hasDriver()==true`、`hasStartedDriver()==false`（未启动浏览器）、`getSelenideDriver()` 非空、当前线程 `SelenideLogger.hasListener(LISTENER_NAME)==true`。
3. **[需验证]** `createDriver` 后立即 `getWebDriver()`：Javadoc 声称返回 null，但 Selenide 7 的 `SelenideDriver.getWebDriver()` 在未 `open()` 时可能抛 `IllegalStateException`。该用例用于固定真实行为；若抛异常，应修 `getWebDriver()` 为 `hasStartedDriver() ? driver.getWebDriver() : null`，否则 `SeleniumParallel.startWebDriver()` 这类调用会直接失败。
4. 线程隔离：线程 A `createDriver`，线程 B 断言 `hasDriver()==false` 且 `getSelenideDriver()` 抛异常；两线程的 listener 互不影响。
5. 重复创建：连续 `createDriver` 两次得到不同实例；`closeDriver()` 后 `hasDriver()==false`、listener 被移除；再次 `closeDriver()` 不抛异常（幂等）。
6. 非法浏览器：`createDriver("safari", method)` 抛 `IllegalArgumentException`，且因为 `closeDriver()` 先执行，原 driver 已被清除（失败后不残留）。

**需要浏览器**：`hasStartedDriver()==true`、真实 `close()` 行为、崩溃后的清理。

### 2.3 `parallel` 包

#### `com.github.ansonliao.selenium.parallel.ClassFinder` — 164 LOC

- 方法：`findAllTestClassesInPackage(String)`、`find(String)`、private `find(File,String)`、`findAnnotatedTestClasses(List<Class<?>>)`、`findAllTestNGTestClasses(String...)`、`createClass(String)`、`getAllClassNames()`（全部 public static）。
- 静态/全局：`private static final TESTNG_TEST_CLASS_PREFIX`（**类加载时**从配置读取并 lower/trim）、`private static volatile ScanResult scanResult`（DCL 单例 + shutdown hook）、`BAD_PACKAGE_ERROR`。
- 外部依赖：ClassGraph 全 classpath 扫描、`Thread.currentThread().getContextClassLoader().getResource()`、目录文件系统、TestNG `@Test`。
- 可测性：**中等偏难**（静态扫描 + 配置固化）；`createClass`、`find`、`findAllTestClassesInPackage` 可单独测。

**无需浏览器的用例**：

1. `find("com.github.ansonliao.selenium.utils")` 返回非空且包含 `AuthorUtils`；`find("no.such.pkg")` 抛 `IllegalArgumentException`，消息含包名。
2. `findAllTestClassesInPackage("testcases")` 包含 `Test_Demo`；用 fixture 证明简单名**包含** "test" 即命中（如 `ContestHelper` 也会被误收，属于语义缺陷）。
3. `createClass("java.lang.String")` 返回 `String`；`createClass("does.not.Exist")` 返回 null（当前吞掉 `ClassNotFoundException` 并打印栈）。
4. `findAllTestNGTestClasses()`：包含 `testcases.Test_Demo`（方法级 @Test、类名以 Test 开头）；排除 `AuthorUtilsTest`（类名不以 test 开头，尽管它是 TestNG 测试）。带 `packages` 参数时只保留**精确包名**，子包不包含；不存在的包返回空集合而不是抛异常。
5. `findAllTestNGTestClasses` 的 enabled 语义：类级 `@Test(enabled=false)` 排除；仅方法级 `@Test(enabled=false)` 的类**仍会被包含**（因为第二个查询只检查类级注解）；fixture 固定该行为。
6. `findAnnotatedTestClasses`：只按**全限定名** `startsWith("test")` 过滤，`testcases.Foo` 命中，`com.foo.TestX` 不命中（即使简单名以 Test 开头）。
7. `getAllClassNames()` 非空、包含已知类；连续调用返回内容一致（新 List，同一底层扫描结果）。
8. **[当前会失败/需隔离]** `testng.class.prefix` 在类加载后再修改无效；用独立 classloader 或 `reset` 钩子验证。另测默认包类触发 `getPackage()==null` 的 NPE。

**需要浏览器**：无。

#### `com.github.ansonliao.selenium.parallel.MethodFinder` — 51 LOC

- 方法：`findAllAnnotatedTestMethodInClass(Class)`、`findMethodInClass(Class)`、`findTestNGMethodInClassByGroup(Class,String)`（public static）。
- 静态/全局：仅 logger。
- 外部依赖：反射、TestNG 注解。
- 可测性：**纯反射，easy**。

**用例**：

1. fixture 类含 2 个 `@Test` + 1 个 helper：`findAllAnnotatedTestMethodInClass` 只返回 2 个；`findMethodInClass` 返回全部 declared methods。
2. 继承：父类有 `@Test` 方法，子类未声明 → 结果**不含**父类方法（`getDeclaredMethods` 语义），固定该行为。
3. **[当前会失败]** 类级 `@Test(groups={"SMOKE"})` 且含 helper：`findTestNGMethodInClassByGroup(clazz,"SMOKE")` 返回**所有** declared methods（含 helper），因为命中类级 group 时直接 `getDeclaredMethods()`。该结果流入 `TestNGFilter` 后会在 `method.getAnnotation(Test.class).enabled()` 处 NPE。
4. 类级 group 不匹配时，只返回方法级 `@Test` 且 group 匹配的方法；无匹配返回空；`null` groupName 返回空。
5. `@Test(enabled=false)` 仍会被 `findAllAnnotatedTestMethodInClass` 返回（enabled 过滤在 `TestNGFilter` 层），固定分层契约。
6. `null` Class → NPE；`getDeclaredMethods` 包含 synthetic/bridge 方法，断言过滤策略（当前不过滤）。

#### `com.github.ansonliao.selenium.parallel.SeleniumParallel` — 145 LOC

- 方法：`findUrl(Method)`、`getUrl()`、`startWebDriver(Method)`、`getDriver()`、`openUrl(String)`、`protected takeScreenShot(String)`、`@Deprecated getAuthors(String,Method)`、`getTestGroups(Object)`、`getDescription(Object)`、`withBoldHTML(String)`。
- 静态/全局：`PROJECT_ROOT_DIR = System.getProperty("user.dir")`、`SCREENSHOT_DIR`（类加载时计算）；实例字段 `browserName`、`url`（可变、非线程安全）。
- 外部依赖：Selenium/Selenide、文件系统、注解。
- 可测性：纯方法 easy；截图/驱动需要浏览器。
- 备注：**整个类在主代码中无引用**，是 3.0 之前的公共基类；若不再承诺兼容，应删除或标记 `@Deprecated`。

**无需浏览器的用例**：

1. `findUrl`：方法级 `@URL` 优先于类级；无方法注解时回退类级；都没有时返回 null，且会把之前的 `url` 字段重置为 null（测试实例状态）。
2. `getTestGroups(Class)` 返回类级 groups；`getTestGroups(Method)` 返回类级 ∪ 方法级、去重；`getTestGroups(new Object())` 抛 `ClassCastException`。
3. `getDescription`：方法级优先、类级回退、无注解返回 null、值 trim；`withBoldHTML("x")` → `<b>x</b>`，`""`/`"  "` → `""`，`null` → NPE（当前未判空）。
4. `getAuthors`（deprecated）与 `AuthorUtils.getMethodAuthors` 结果一致。
5. 线程安全：两个线程共享同一 `SeleniumParallel` 实例调用 `findUrl`，`url` 字段互相覆盖（固定该缺陷；建议改为返回局部变量）。

**需要浏览器**：`startWebDriver`、`getDriver`、`openUrl`、`takeScreenShot`。

#### `com.github.ansonliao.selenium.parallel.SeleniumParallelTestListener` — 123 LOC

- 实现 `IClassListener, IInvokedMethodListener, ISuiteListener`；方法 `beforeInvocation`、`afterInvocation`、`onStart`、`onFinish`、`onBeforeClass`、`onAfterClass`、private `openRemoteURL`。
- 静态/全局：`TESTNG_BROWSER_PARAM_KEY`（类加载时从配置读取）。
- 外部依赖：TestNG 运行时、Selenide、Allure、文件系统。
- 可测性：**需 TestNG 运行时**；用程序化 TestNG 或 `IInvokedMethod`/`ITestResult` stub 可避免浏览器。

**用例**：

1. `beforeInvocation` 对非测试方法（`isTestMethod()==false`，例如 `@BeforeMethod`）直接返回，不创建 driver、不写 Allure。
2. 浏览器参数解析：`XmlTest` 参数 `browser=firefox` → 使用 firefox；参数缺失或空 → `SEConfigs.defaultBrowser()`。
3. `beforeInvocation` 后 `SelenideManager.hasDriver()==true`；`add.browser.group.to.report=true` 时 Allure tag 包含浏览器名（需 Allure 上下文断言，或 refactor 出 groups 计算）。
4. **[设计缺陷]** 方法级 `@URL` 会自动 `open`，但**类级 `@URL` 不会**（与 `SeleniumParallel.findUrl` 的类级回退不一致）。用一个只有类级 `@URL` 的 fixture + 未启动 driver 的断言固定行为（不真正导航）。
5. `onBeforeClass` 会创建 `target/screenshots/<package>/<class>/<browser>` 目录；`getXmlTest()==null` 时回退默认浏览器；默认包类触发 `getPackage()==null` NPE。
6. `afterInvocation` 仅在 `status==FAILURE` 且 `hasStartedDriver()` 时截图；成功/跳过/无 driver 时不调用 `ScreenshotManager`（用 stub 验证）。

**需要浏览器**：真正 `open` URL、失败截图、`hasStartedDriver()==true` 分支。

### 2.4 `testng` 包

#### `com.github.ansonliao.selenium.testng.TestNGFilter` — 200 LOC

- 方法：`run()`、`filterClassesByTestingClassList()`、`filterTestNGMethodByTestGroups()`、`filterTestNGMethodByBrowsers()`、`getBrowser2TestNGClass2TestNGMethodMap()`；private `addIncludedDefaultBrowser`。
- 静态/全局：`private static List<Class<?>> testngClasses`、`private static Multimap<Class<?>,Method> testNGClass2MethodMap`，**static 块**在类加载时调用 `ClassFinder.findAllTestNGTestClasses(...)`；`run()` 会原地修改这两个字段。
- 外部依赖：配置、ClassGraph、反射、TestNG 注解、`BrowserUtils`。
- 可测性：**难测试**（static 初始化 + 全局可变 + 配置缓存）。必须加注入/重置钩子，或把纯过滤逻辑抽成接受 `(classes, config)` 的方法。

**用例（注入 `testngClasses` 后）**：

1. 无任何过滤配置：`run()` 返回的 map 中，每个 `@Test` 方法都出现在其支持浏览器下；默认浏览器补充逻辑正确（无浏览器注解 → 默认浏览器）。
2. `testing.testng.classes`：大小写不敏感匹配 canonical name；带引号的值应被 `removeQuoteMark` 后匹配。OWNER 默认使用 `SplitAndTrimTokenizer`，会先 trim，因此普通 `-D` 值应已正确；测试用于固定该契约（若以后换 tokenizer 或直接注入 List 则可能回归）。内部类注意 canonical name 用 `.` 而 `Class.getName()` 用 `$`。
3. `testing.test.groups`：方法级 group 命中；类级 group 命中 → **[当前会失败]** 包含 helper 方法并在 `getAnnotation(Test.class).enabled()` 处 NPE。
4. `testing.browser.names=CHROME`：移除仅 `@Firefox` 的方法；`@IgnoreChrome` 的方法被移除；多浏览器注解保留交集。
5. `run.by.browsers=CHROME,FIREFOX`：忽略方法上的浏览器注解，把每个方法分配到两个浏览器；小写值应被 uppercase 后匹配。OWNER 会 trim，因此带空格的 `-D` 值通常已正确；代码在 `Sets.intersection` 之前没有显式 trim，测试用于固定该依赖。
6. `run()` 幂等性：连续调用两次不应累积旧方法（**[当前会失败]** `testNGClass2MethodMap` 从不清空，过滤结果会叠加）。
7. `addIncludedDefaultBrowser`：方法只有 `@IgnoreChrome` 且默认 Chrome → 该方法无浏览器（被跳过）；方法无任何浏览器注解 → 默认 Chrome；方法 `@Firefox` + `@IgnoreFirefox` → 无浏览器。
8. `runByBrowsers` 含不支持的值（如 `OPERA`）→ 返回空 map（不会抛异常）。

**需要浏览器**：无；需要 TestNG 注解与类加载。

#### `com.github.ansonliao.selenium.testng.TestNGRetry` — 65 LOC

- 方法：`retry(ITestResult)`、`getRetryCount()`、`static getMaxRetryCount()`。
- 静态/全局：`private static int maxRetryCount`（**跨线程共享**）、实例 `retryCount` 从 1 开始。
- 外部依赖：TestNG `ITestResult`/`ITestNGMethod`、`@Retry` 注解。
- 可测性：**需 TestNG 类型**，但可用 stub/Proxy 或程序化 TestNG，无需浏览器。

**用例**：

1. 方法级 `@Retry(maxRetry=2)`：`retry()` 连续返回 `true, true, false`，`getRetryCount()==3`。
2. 类级 `@Retry(maxRetry=1)` 作为回退；方法级注解覆盖类级；无注解 → 第一次就 false。
3. 负值 `maxRetry=-1` 被钳制为 0；`maxRetry=0` 第一次返回 false。
4. **[当前会失败/并发缺陷]** 两个 `TestNGRetry` 实例（maxRetry=1 与 maxRetry=3）在两个线程中交替调用，`maxRetryCount` 静态字段互相覆盖，导致重试次数错误。修复方向：改为实例字段（在 `retry()` 中解析后保存）。
5. 数据提供者分支：`if (maxRetryCount - retryCount == 1) retryCount = 1;` 在正常路径不可达（第一次就会 return true），属于死代码；用参数化序列证明永不触发。
6. `getMaxRetryCount` 在未调用 `retry` 前为 0（全局默认）。

**需要 TestNG 运行时**：若不想手写 `ITestResult` stub，可用程序化 TestNG + 失败 fixture 断言调用次数。

#### `com.github.ansonliao.selenium.testng.RetryListener` — 20 LOC

- 实现 `IAnnotationTransformer`；`transform(ITestAnnotation, Class, Constructor, Method)`。
- 无状态。
- 可测性：**纯逻辑 easy**，用 `Proxy` 实现 `ITestAnnotation` 即可。

**用例**：

1. `getRetryAnalyzerClass()==null` → `setRetryAnalyzer(TestNGRetry.class)` 被调用一次。
2. 已有自定义 analyzer → 不被覆盖。
3. 方法/构造器为 null（类级注解场景）→ 仍设置 analyzer，不抛 NPE。
4. 与程序化 TestNG 集成：失败 fixture + `RetryListener` → 实际重试次数符合 `@Retry`（[需 TestNG 运行时]）。

#### `com.github.ansonliao.selenium.testng.TestResultListener` — 71 LOC

- 继承 `TestListenerAdapter`；覆盖 `onTestFailure`、`onTestSkipped`、`onTestSuccess`、`onFinish`；private `getTestRealClass`、`getTestMethod`、`getTestBrowser`。
- 静态/全局：无自身静态；调用 `SelenideManager.closeDriver()`（ThreadLocal）。
- 外部依赖：TestNG `ITestResult`/`ITestContext`、配置。
- 可测性：**需 TestNG 类型**；stub 即可，无需运行时。

**用例**：

1. 预绑定 driver（`SelenideManager.createDriver`）后调用 `onTestSuccess`/`onTestFailure`/`onTestSkipped` → 三种结果都调用 `closeDriver()`，`hasDriver()==false`。
2. `getTestBrowser`：`getTestContext()==null` → 默认浏览器；`getCurrentXmlTest()==null` → 默认；XmlTest 参数缺失 → 默认；参数存在 → 参数值。可用反射或日志捕获，建议 refactor 为包级方法。
3. `getTestRealClass`/`getTestMethod` 为 null 的畸形 result → NPE（固定并决定是否防御）。
4. `onFinish` 目前只调用 `super`，无额外行为；验证不会关闭 driver（避免误清理）。
5. 继承的 `TestListenerAdapter` 会把每个结果存入内部 List；大量测试时内存增长（设计观察，非单测）。

#### `com.github.ansonliao.selenium.testng.TestNGRunner` — 38 LOC

- 方法：`static Run()`、`static Run(XmlSuite)`；private `logFigletFont`。
- 静态/全局：无；但 `Run()` 会触发 `XmlSuiteBuilder` → `TestNGFilter` 全量扫描。
- 外部依赖：TestNG 运行时、jfiglet。
- 可测性：**需 TestNG 运行时**；`Run(XmlSuite)` 可测。

**用例**：

1. `Run(new XmlSuite())`（无 test）正常返回，不抛异常。
2. `Run(suite)` 包含一个平凡通过测试 + 默认 listener → 测试执行且状态为 PASSED（[需 TestNG 运行时]）。
3. `Run()` 无参：全量扫描 + 生成 suite，断言不抛异常（慢，可标记为 integration）。
4. Figlet 字体加载失败路径不可测（资源内置），忽略。

#### `com.github.ansonliao.selenium.testng.XmlSuiteBuilder` — 52 LOC

- 方法：`build()`、`setXmlSuiteGenerateInfo(XmlSuite)`、`addXmlSuiteListeners(XmlSuite)`、`addXmlSuiteTests(XmlSuite)`。
- 静态/全局：通过 `XmlTestBuilder` 的静态 suite 间接有全局状态。
- 外部依赖：TestNG XML 模型、配置。
- 可测性：前两个方法**纯**；`build()` 依赖扫描。

**用例**：

1. `setXmlSuiteGenerateInfo(new XmlSuite())`：name=`Selenium Web UI Test`、parallel=`TESTS`、preserveOrder=false、threadCount=`availableProcessors()`、verbose=2。
2. `addXmlSuiteListeners`：默认两个 listener 被加入、去重、引号被移除。默认值中第二个 listener 前有空格；OWNER 1.0.12 使用 `SplitAndTrimTokenizer`，预期会被 trim，测试用于固定该行为（若换 tokenizer，`XmlSuite.addListener` 会得到带前导空格的类名，TestNG 加载失败）。
3. `build()`：suite 至少包含一个 `XmlTest`（依赖测试类扫描），且包含配置的 listeners。
4. 自定义 `testng.listeners`（含重复/引号/空格）→ 去重且 trim。

#### `com.github.ansonliao.selenium.testng.XmlTestBuilder` — 83 LOC

- 方法：`build()`、`getXmlSuite()`、`setXmlSuite(XmlSuite)`。
- 静态/全局：`private static XmlSuite xmlSuite`（可变、非线程安全）。
- 外部依赖：`XmlClassBuilder` → `TestNGFilter` 全量扫描、配置。
- 可测性：**难测试**；建议把“按大小切分 XmlClass”的纯逻辑抽成包级方法后测。

**用例（抽出纯逻辑后）**：

1. 25 个 class、size=10 → 3 个 XmlTest，分别 10/10/5；名称 `Selenium Test - CHROME`、`... CHROME 2`、`... CHROME 3`。
2. 恰好 10 个 → 1 个 XmlTest，10 个 class；0 个 → 不创建空 XmlTest。
3. size 配置为 0 → **[当前会失败]** `size / 0` 抛 `ArithmeticException`；负数 → 循环行为异常。应校验 `>=1`。
4. 每个 XmlTest 都带 `browser` 参数（key 可配置）和 `preserveOrder` 配置值。
5. 未调用 `setXmlSuite` 直接 `build()` → `new XmlTest(null)` 或 NPE；固定顺序依赖，建议构造器注入 suite。

#### `com.github.ansonliao.selenium.testng.XmlClassBuilder` — 29 LOC

- 方法：`static Multimap<String, XmlClass> build()`。
- 静态/全局：无自身；依赖 `TestNGFilter.run()` 的静态状态。
- 外部依赖：TestNG XML、反射。
- 可测性：**需 TestNGFilter 注入**。

**用例**：

1. 同一类两个 `@Test` 方法 → 一个 `XmlClass`，包含 2 个 `XmlInclude`。
2. **[需验证]** 重载的两个 `@Test` 方法同名 → `XmlInclude` 是否去重；若 `XmlInclude` 未实现 equals，`distinct()` 不会生效，最终可能包含重复 include。
3. 两个类的同名方法 → 各自一个 `XmlClass`，互不合并。
4. 同一方法支持多个浏览器 → 每个浏览器 map 下都存在对应 `XmlClass`。

### 2.5 `utils` 包

#### `com.github.ansonliao.selenium.utils.BrowserUtils` — 136 LOC

- 方法：`getSupportedBrowsers()`、`getSupportedIgnoreBrowsers()`、`getClassSupportedBrowsers`、`getClassBrowsers`、`getClassIgnoredBrowsers`、`getMethodSupportedBrowsers(Method)`、`getMethodSupportedBrowsers(Class,Method)`、`getMethodBrowsers`、`getMethodIgnoredBrowsers`、`filterBrowsersFromAnnotations`、`filterBrowsersIgnoredFromAnnotations`（全部 public static，部分 synchronized）。
- 静态/全局：无字段（logger 除外）；依赖 `ClassFinder.getAllClassNames()` 的 ClassGraph 扫描。
- 外部依赖：ClassGraph、配置、注解。
- 可测性：**中等**；纯过滤方法 easy，完整解析需要 classpath 上有注解类（本项目 main 类路径已有）。

**用例**：

1. `getSupportedBrowsers()` 包含 `CHROME`、`FIREFOX`、`EDGE`、`INTERNETEXPLORER`，且不包含任何 `IGNORE*`。
2. `getSupportedIgnoreBrowsers()` 返回 `CHROME`、`FIREFOX`、`EDGE`、`INTERNETEXPLORER`。
3. `filterBrowsersFromAnnotations`：传入 `@Chrome`、`@Firefox`、`@Test`、`@Headless`、`@Incognito`、`@Author` → 当前会得到 `CHROME`、`FIREFOX`、`AUTHOR`（**非浏览器注解泄漏**）；建议只保留白名单/`type()` 匹配。
4. `filterBrowsersIgnoredFromAnnotations`：`@IgnoreChrome` → `CHROME`；若配置 `browser.ignore.anntation.prefix=ignore`（小写）→ 当前返回空，因为注解简单名已 uppercase（**大小写缺陷**）。
5. 类级/方法级组合：类 `@Chrome` + 方法 `@Firefox` → 两者；方法 `@IgnoreChrome` → 移除 Chrome；类 `@IgnoreFirefox` + 方法 `@Firefox` → 空。
6. 继承：`@Chrome` 未标注 `@Inherited`，子类 `getClassBrowsers(Sub.class)` 为空（固定行为，避免误以为会继承）。
7. `getMethodSupportedBrowsers` 对无注解方法 → 空集合；`null` method → NPE。

#### `com.github.ansonliao.selenium.utils.TestGroupUtils` — 59 LOC

- 方法：`getClassTestGroups(Class<?>)`、`getMethodTestGroups(Method)`（public static）。
- 静态/全局：无；读取 `SEConfigs.testingTestGroups()`。
- 外部依赖：TestNG `@Test`、配置。
- 可测性：**easy**（配置覆盖用例需要 reset）。

**用例**：

1. 类 `@Test(groups={"A","A","B"})` → `[A,B]`（distinct、顺序按声明）。
2. 类无 `@Test` → 空列表（当前是不可变 `emptyList()`，断言不可变）。
3. 方法级 groups 与类级 groups 取并集、去重。
4. 方法不是 `@Test` → 只返回类级 groups。
5. `testing.test.groups` 非空 → 无论注解如何都返回配置值（顺序/引号处理）。
6. `null` class/method → NPE（固定）。

#### `com.github.ansonliao.selenium.utils.AnnotationUtils` — 22 LOC

- 方法：`getClassAnnotations(Class)`、`getMethodAnnotations(Method)`（public static synchronized）。
- 无状态；**主代码无引用（死代码）**。
- 可测性：纯函数 easy。

**用例**：有注解的类/方法返回非空 Set；无注解返回空 Set；`null` 入参 NPE；重复注解（`@Repeatable` 场景）行为。**建议删除该类而不是补测**。

#### `com.github.ansonliao.selenium.utils.AuthorUtils` — 160 LOC

- 方法：`getMethodAuthors(Method)`、`getClassAuthors(Class<?>)`（public static）；private 注解/Javadoc 合并逻辑。
- 静态/全局：`SOURCE_ROOT = new File(user.dir, "src")`（类加载时）、`QDOX_LOCK`、嵌套 `JavaProjectHolder.INSTANCE`（lazy、不可重置）。
- 外部依赖：qdox 源码解析、文件系统、`@Author`。
- 可测性：**已有 4 个测试**；新增用例需要源码 fixture（放在 `src/test/java` 即可被 qdox 解析）。

**补充用例**：

1. 方法上多个 `@author` Javadoc 标签 + `@Author` 数组 → 全部返回、去重、保持发现顺序。
2. `@Author({" ", ""})` / Javadoc 空白 → 被过滤；`getClassAuthors` 返回 `Optional.empty()`。
3. **[当前会失败/缺陷]** 重载方法：qdox 按方法名 `findFirst`，两个同名重载方法会拿到第一个的 Javadoc 作者；用两个不同 `@author` 的重载 fixture 固定。
4. 嵌套类：`Class.getName()` 是 `Outer$Inner`，qdox 使用 `Outer.Inner`，`getClassByName` 可能找不到 → 只返回注解作者（不抛异常）。
5. `@author:` 变体标签被识别（`isAuthorTag` 同时匹配 `author` 和 `author:`）。
6. 类级注解 + 类级 Javadoc 合并；方法级作者存在时**不**回退类级。
7. 并发：多线程同时调用不同方法，qdox 解析不抛 `ConcurrentModificationException`（`QDOX_LOCK` 只保护 `getClassByName`，builder 初始化是安全的）。

#### `com.github.ansonliao.selenium.utils.CapsUtils` — 145 LOC

- 方法：`isJsonFileEmpty()`、`isPathExists(String)`、`getCaps(String)`、`getCliArgs(String)`、`getExtensions(String)`、`getEmulation(String)`（public static synchronized）。
- 静态/全局：`wdCapsFilePath`、`DocumentContext documentContext`（**static 块**读取 `SEConfigs.capsPath()`）、`public static Configuration GSON_CONFIGURATION`（可变且实际未使用）。
- 外部依赖：json-path、文件系统、配置。
- 可测性：**需 fixture 文件 + 静态重置**；坏文件会导致 `ExceptionInInitializerError`。

**用例（每个场景需要独立 JVM 或 `reload` 钩子）**：

1. 文件不存在 → `isJsonFileEmpty()==true`；`getCaps/getCliArgs/getExtensions/getEmulation` 返回空 Map/List（非 null）。
2. 合法文件 → 各 getter 返回预期值；browser key 大小写敏感（`chrome` vs `CHROME`）。
3. `{"CHROME": {}}`（浏览器节点存在但 `caps`/`cli_args` 缺失）：当前 `JsonPath.parse(new File(...))` 使用**默认 Configuration**（不含 `DEFAULT_PATH_LEAF_TO_NULL`），缺失叶子抛 `PathNotFoundException` → `isPathExists` false → getter 返回空容器，调用方安全。**契约测试**：缺失 key 必须返回空 Map/List（不能返回 null）。注意 `GSON_CONFIGURATION` 字段虽然配置了 `DEFAULT_PATH_LEAF_TO_NULL`，但**从未被使用**；一旦有人把 parse 改成使用它，`read` 对缺失叶子返回 null，`SelenideConfigFactory` 的 `new ArrayList<>(null)` / `caps.forEach` 会 NPE。该用例同时守护这个潜在回归。
4. `{}` → `isJsonFileEmpty()` 当前返回 false（`read("$").toString()` 是 `"{}"`），语义应为 empty；固定并修正。
5. 根节点为 JSON `null` → `documentContext.read("$")` 返回 null → `isJsonFileEmpty` NPE。
6. **[当前会失败]** 空文件/坏 JSON → static 块只 catch `IOException`，`JsonPath.parse` 抛 `InvalidJsonException` → `ExceptionInInitializerError`，后续所有测试不可用；应改为记录错误并置空 context。
7. `isPathExists` 边界：`null` → NPE；`"   "` → false；`"$['chrome']"` → 当前被错误拼成 `"$.$['chrome']"`；`"caps"` → 自动补 `$.`。
8. 值类型错误（`cli_args` 是字符串而非数组）→ `read(path, List.class)` 抛 `ConversionException`；固定并决定是否降级为空列表。
9. `wd.caps.file` 带引号 → 未 `removeQuoteMark`，文件找不到（与其它配置项不一致）。

#### `com.github.ansonliao.selenium.utils.MyFileUtils` — 39 LOC

- 继承 `org.apache.commons.io.FileUtils`；方法：`createScreenshotFolderForBrowser(Class,String)`、`isFileExisted(String)`（public static synchronized）。
- 静态/全局：`PROJECT_ROOT_DIR`、`SCREENSHOT_DIR`（类加载时）。
- 外部依赖：文件系统。
- 可测性：**easy（写 target/ 临时目录）**。

**用例**：

1. 对普通类 + `"chrome"` 创建目录，路径为 `target/screenshots/<package>/<SimpleName>/chrome`；重复调用幂等。
2. 默认包类 → `clazz.getPackage()==null` → NPE（固定；建议判空）。
3. `isFileExisted(null)`/`""`/不存在路径 → false；存在的文件 → true；存在的**目录** → true（方法名与语义不符，固定）。
4. 目标路径被同名文件占用 → `mkdirs()` 返回 false 且被忽略，返回不存在的 `File`（静默失败）。

#### `com.github.ansonliao.selenium.utils.SEConfig` — 63 LOC

- `@Deprecated`；方法：`getInstance()`、`getString(String)`、`getInt(String)`、`getBoolean(String)`、`isKeyExisted(String)`。
- 静态/全局：`private static SEConfig instance = new SEConfig()`（**类加载即读取配置**）、`Config config`。
- 外部依赖：Typesafe Config、classpath 属性文件、环境变量。
- 可测性：**难测试/死代码**；主代码无引用。
- 建议：**删除**，统一用 `SEConfigs`（OWNER），不要为它补测。

若保留，最小用例：`getString("")` 返回 `""`；未知 key → `getString` 抛 `ConfigException` / `isKeyExisted` false；`getBoolean` 对 `"true"/"false"/""` 的结果；环境变量名 `a.b` → `A_B`（需子进程，进程内无法设置 env）。

#### `com.github.ansonliao.selenium.utils.StringUtils` — 18 LOC

- 方法：`removeQuoteMark`、`isNullOrEmpty`、`isNotNullAndNotEmpty`（public static synchronized）。
- 无状态；纯函数。
- 可测性：**easy，优先补**（所有配置/URL 处理的基础）。

**用例**：

1. `removeQuoteMark`：`"abc"`/`'abc'` → `abc`；`"'abc'"` → `abc`；内部引号 `a"b` 不变；`null` → null；`""` → `""`；单个 `"` → `""`；只有前引号 `"abc` → `abc`；只有后引号 `abc"` → `abc`。
2. `isNullOrEmpty`：`null`、`""` → true；`" "`、`"\t"` → **false**（不 trim，这是 caps/hub URL 空格缺陷的根源）。
3. `isNotNullAndNotEmpty` 与上互补。
4. 线程安全无意义（synchronized 纯函数），可忽略。

#### `com.github.ansonliao.selenium.utils.config.SEConfigs` — 112 LOC

- 方法：`getConfigInstance()`（public static synchronized）；嵌套 `SEConfiguration` 接口（OWNER）。
- 静态/全局：`private static SEConfiguration config`（**首次访问后缓存**）；OWNER 从 `System.getProperties()` + `System.getenv()` 取值；`@Config.Sources` 加载 classpath `seleniumextensions.properties` / `se.properties`。
- 外部依赖：OWNER、classpath 资源、系统属性/环境变量。
- 可测性：**默认值 easy；覆盖需 reset**。

**用例**：

1. 默认值：`defaultBrowser()=="Chrome"`、`capsPath()=="caps/caps.json"`、`testngTestClassPrefix()=="test"`、`browserIgnoreAnnotationPrefix()=="IGNORE"`、`browserAnnotationPackage()=="com.github.ansonliao.selenium.annotations.browser"`。
2. `testngListeners()` 默认返回 **2 个已 trim 的 FQN**。默认字符串第二个元素前有空格；OWNER 1.0.12 的 `SplitAndTrimTokenizer` 会 trim，因此应正常，测试用于固定该行为（避免以后换 tokenizer 后 `XmlSuiteBuilder` 生成非法 listener 名）。
3. 列表默认值：`runByBrowsers()`、`testingPackageNames()`、`testingBrowserNames()`、`testingTestGroups()`、`testingTestNGClasses()` 为空列表（不是 `[""]`）。注意 `SplitAndTrimTokenizer` 对 `""` 会 `split` 出 `[""]`，因此必须验证 OWNER 在值为空字符串时是否返回空列表；若返回 `[""]`，`TestNGFilter` 会误判为“有过滤”并产出空套件。
4. `-Ddefault.browser=firefox`（在首次访问前）→ `"firefox"`；`-Dtesting.test.groups=A,B` → `[A,B]`；带空格 `A, B` 的 trim 行为需固定（OWNER 可能保留空格）。
5. 缓存：连续两次 `getConfigInstance()` 返回同一对象；首次访问后再设系统属性不生效（固定并决定是否加 reset）。
6. 文件优先级：`seleniumextensions.properties` 优先于 `se.properties`（需要 test resources fixture；注意会全局影响其它测试，建议用独立 classloader）。

### 2.6 `json` 包

#### `com.github.ansonliao.selenium.json.JsonLoader` — 72 LOC

- 实现 `org.aeonbits.owner.loaders.Loader`；方法：`accept(URI)`、`load(Properties, URI)`、`defaultSpecFor(String)`；private 递归 `parseJsonElement`。
- 无状态；**未见 ServiceLoader 注册（`META-INF/services` 不存在），主代码也未引用 → 死代码**。
- 可测性：纯函数/临时文件 easy。

**用例**：

1. `accept`：`file:/tmp/a.json`、`.JSON` → true；`.properties` → false；`classpath:foo.json` / 相对 URI → false（`toURL` 抛 `MalformedURLException` 被捕获）。
2. `defaultSpecFor("x")` → `x.json`。
3. `load` 嵌套对象 → 点分 key；原始值数组 → `"a, b"`；对象数组 → `key[0].x`；空数组不写入 key；嵌套数组元素被静默丢弃。
4. **[当前会失败]** JSON `null` 值 → `element.getAsString()` 对 `JsonNull` 抛 `UnsupportedOperationException`；顶层数组 → 写入空字符串 key。
5. 文件不存在 → `IOException`；非法 JSON → `JsonSyntaxException`。
6. 布尔/数字 → 字符串形式。

#### `com.github.ansonliao.selenium.json.JsonParser` — 85 LOC

- 方法：`getGsonInstance()`、`getJsonElement(JsonElement,String)`、`isNodeExisted(JsonElement,String)`；`main`。
- 静态/全局：`GSON_INSTANCE`（不可变引用）。
- 无外部依赖；**主代码无引用（死代码）**。
- 可测性：纯函数 easy。

**用例**：

1. 嵌套对象 `a.b.c`、数组 `arr.1.x`（**1-based**）→ 正确元素；`arr.0` → `IndexOutOfBoundsException`（固定 1-based 语义）。
2. 缺失叶子 → null → `isNodeExisted==false`；**[当前会失败]** 缺失中间节点（`a` 不存在，路径 `a.b`）→ 返回 `JsonNull`，`isNodeExisted==true`（误判存在）。
3. 显式 JSON `null` → `JsonNull`，`isNodeExisted==true`（与缺失区分）。
4. 连续点/空括号/空白 key 被跳过；对 primitive 取子路径 → 直接返回 primitive（`isNodeExisted` 为 true，语义可疑）。
5. `getJsonElement(null,"a")` → `JsonNull`；`isNodeExisted(null,...)` → false；`isNodeExisted(json,"")` → 非 null 即 true。
6. `main` 硬编码 `caps/caps.json`，属于生产代码中的调试入口，建议删除。

### 2.7 `report` 包

#### `com.github.ansonliao.selenium.report.AllureReport` — 73 LOC

- 方法：`describeTest(Method,String,List<String>,List<String>)`、`step(String)`、`attachScreenshot(String)`；private `descriptionOf`。
- 静态/全局：Allure 的 ThreadLocal 生命周期。
- 外部依赖：Allure 运行时、文件系统。
- 可测性：`attachScreenshot` 缺失文件分支 easy；标签/steps 需要 **Allure 上下文**。

**用例**：

1. `attachScreenshot("/no/such/file.png")` → 不抛异常、不调用 Allure（只 warn）。
2. `attachScreenshot(null)` → `Path.of(null)` NPE（固定；建议判空）。
3. 已存在的临时 PNG → 启动 Allure test case 后调用，断言结果中有一个 `image/png` attachment（[需 Allure 上下文]）。
4. `describeTest`：方法级 `@Description` 覆盖类级；空白描述不写；`authors=null`/`groups=null` 不抛；browser/owner/tag 标签写入结果（[需 Allure 上下文]）。
5. `attachScreenshot("x.jpeg")` → 当前以 `image/png` 附加（MIME 与扩展名不一致）；若保留 `.jpeg` 来源需修正。

#### `com.github.ansonliao.selenium.report.SelenideAllureSteps` — 41 LOC

- 方法：`install()`、`uninstall()`；常量 `LISTENER_NAME`。
- 静态/全局：`private static final AllureSelenide LISTENER`（跨线程共享）；SelenideLogger 内部 ThreadLocal。
- 可测性：**easy**；已有 2 个测试。

**补充用例**：

1. 未安装时 `uninstall()` 不抛异常。
2. 安装 → 卸载 → 再安装，最终 `hasListener==true`。
3. 线程隔离：线程 A 安装，线程 B `hasListener==false`；两线程各自卸载互不影响。
4. 通过反射或行为断言 listener 的 `screenshots(false)` / `savePageSource(false)`（避免 Selenide 额外截图）。

### 2.8 `internal` 包

#### `com.github.ansonliao.selenium.internal.ScreenshotManager` — 47 LOC

- 方法：`capture(Class<?>, String, String)`。
- 静态/全局：`PROJECT_ROOT_DIR`、`SCREENSHOT_DIR`（类加载时）。
- 外部依赖：AShot、`WDManager.getDriver()`、`ImageIO`、文件系统。
- 可测性：**需浏览器**；路径拼接逻辑可抽出后纯测。

**用例**：

1. **[需浏览器]** 启动 driver 后 `capture` 生成 PNG，路径为 `target/screenshots/<package>/<class>/<browser>/<prefix>_<timestamp>.png`。
2. 无 driver → `WDManager.getDriver()` 返回 null → AShot NPE（固定）。
3. 目标目录不存在 → `ImageIO.write` 抛 `IIOException`，被 catch 后仍返回不存在的路径（**静默失败**）；建议返回 `Optional`/抛异常。
4. 路径拼接（抽成包级 `pathFor(...)` 后）：前缀含 `/`、浏览器名含空格、同一毫秒两次调用（时间戳冲突）。

#### `com.github.ansonliao.selenium.internal.OptionalConsumer` — 27 LOC

- 方法：`of(Optional<T>)`、`ifPresent(Consumer<T>)`、`orElse(Runnable)`。
- 无状态；**主代码无引用（死代码）**。
- 可测性：纯函数 easy。

**用例**：present → `ifPresent` 执行、`orElse` 不执行；empty → 反之；链式 `ifPresent(...).orElse(...)`；`of(null)` 后调用 → NPE。**建议删除**。

#### `com.github.ansonliao.selenium.internal.OptionalEx` — 26 LOC

- 方法：`ifPresent(Optional<T>, Consumer<? super T>)`、`orElse(Runnable)`。
- 无状态；**主代码无引用（死代码）**。
- 可测性：纯函数 easy。

**用例**：present/empty 的 consumer/runner 执行矩阵；`ifPresent(null, c)` → NPE；返回对象的 `orElse` 仅在 absent 时执行。**建议删除**。

### 2.9 `factory` 包

#### `com.github.ansonliao.selenium.factory.WDManager` — 19 LOC

- 方法：`getDriver()`、`quitAndClearDriver()`（public static synchronized）。
- 静态/全局：无自身字段，代理 `SelenideManager` 的 ThreadLocal。
- 可测性：**easy**。

**用例**：

1. 新线程 → `getDriver()==null`。
2. `SelenideManager.createDriver` 后 → `getDriver()==SelenideManager.getWebDriver()`（注意：未 `open` 时 Selenide 可能抛异常，见 SelenideManager 用例 3）。
3. `quitAndClearDriver()` → `getDriver()==null`，重复调用幂等。
4. 线程隔离：线程 A 有 driver，线程 B 为 null。

---

## 3. 优先级表

> `[P0]` = 立即补，能暴露高影响缺陷；`[P1]` = 核心逻辑，建议同批；`[P2]` = 有价值但依赖重构/运行时；`[P3]` = 低风险/死代码；`[P4]` = 建议删除而非测试。

| class | testability | candidate tests | risk covered |
|---|---|---|---|
| **[P0] `driver.SelenideConfigFactory`** (172) | 包级可见；纯函数 + caps fixture；无需浏览器 | 浏览器名归一化表；hub URL 尾斜杠/大小写/空格；`create` 的 headless/incognito/IE 规则；caps key 使用原始名导致 `" IE "` 失配；`null`/空白 cli_args 变 `"null"` | 所有浏览器启动参数、Grid URL、caps 查找错误；当前有 3 个可确认缺陷 |
| **[P0] `utils.CapsUtils`** (145) | 需 fixture + 静态重置；坏文件会 `ExceptionInInitializerError` | 缺失文件全空；`{"CHROME":{}}` 返回 null 导致调用方 NPE；`{}` 被判非空；根 `null` NPE；坏 JSON 初始化失败；`isPathExists` 的 null/空白/`$[...]` | 浏览器 capabilities/args/extensions 全部来源；坏配置会让整个 JVM 测试不可用 |
| **[P0] `driver.SelenideManager`** (68) | ThreadLocal 生命周期 easy；真实开关浏览器需浏览器 | 未绑定→异常/null；创建后 `hasDriver/hasStartedDriver`；`getWebDriver` 未 open 的真实行为；线程隔离；重复创建/关闭幂等；非法浏览器不残留 | 并行线程串 driver、driver 泄漏、listener 未安装/未移除 |
| **[P0] `testng.TestNGFilter`** (200) | 难；static 初始化 + 可变全局，需注入/重置 | 无过滤全量；`testing.testng.classes` 空格；group 命中类级注解导致 helper NPE；browser filter/ignore；`run.by.browsers` 空格；`run()` 非幂等；默认浏览器补充 | 生成空套件、漏跑/多跑测试、套件构建 NPE；3 个高影响缺陷 |
| **[P0] `testng.TestNGRetry`** (65) | 需 TestNG 类型/stub；无需浏览器 | `@Retry(2)` 的 true/true/false；类级回退/方法覆盖；负值钳制；**static maxRetryCount 并发串扰**；dataprovider 分支不可达 | 重试次数错误、并行下不同 `@Retry` 互相污染 |
| **[P0] `utils.BrowserUtils`** (136) | 中等；classpath 已有注解；纯过滤方法 easy | 支持/忽略浏览器集合；`@Author` 被当浏览器；ignore 前缀大小写；类级+方法级并集/差集；`@Inherited` 缺失 | 测试分配到错误浏览器、忽略注解失效、未知注解污染 |
| **[P0] `utils.config.SEConfigs`** (112) | 默认值 easy；覆盖需 reset | 默认值全表；listener 默认值是否 trim；空列表 vs `[""]`；`-D` 覆盖；缓存语义；文件优先级 | 配置错误导致全部测试不跑/跑错浏览器；listener 类名非法 |
| **[P0] 测试基建：Gradle/CI** | 构建脚本 | 默认 `test` 排除 `testcases.Test_Demo`；新增 `browserTest`；`checkstyleTest`；`forkEvery`/reset 策略 | 新增单测无法在 CI 运行；浏览器 demo 阻塞构建 |
| **[P1] `parallel.ClassFinder`** (164) | 中等；静态扫描/前缀固化 | 包扫描命中/缺失包；`Test*` 前缀语义；类级/方法级/disabled；包精确匹配；`createClass` 返回 null；默认包 NPE；前缀不可变 | 测试发现漏类/多类、包过滤错误、ClassNotFound 静默 |
| **[P1] `parallel.MethodFinder`** (51) | 纯反射 easy | 只返回 `@Test`；继承方法不返回；类级 group 返回全部 helper（缺陷）；方法级 group；`enabled=false` 分层 | group 过滤 NPE、漏跑继承测试、包含非测试方法 |
| **[P1] `testng.XmlTestBuilder`** (83) | 难；静态 suite + 扫描；建议抽纯分片 | 25/10→10/10/5；恰好 10；0 个；size=0 除零；browser 参数；未设 suite 的 NPE | 套件分片错误/丢类、空 `<test>`、除零崩溃 |
| **[P1] `testng.XmlClassBuilder`** (29) | 需 TestNGFilter 注入 | 方法→XmlInclude；重载同名去重；跨类同名；多浏览器映射 | 重载测试丢失/重复、include 与类不匹配 |
| **[P1] `utils.TestGroupUtils`** (59) | easy；配置覆盖需 reset | 类级 groups 去重；无 `@Test` 空列表；方法+类并集；非 `@Test` 方法；`testing.test.groups` 覆盖 | Allure tag/分组过滤错误、不可变列表被修改 |
| **[P1] `utils.AuthorUtils`** (160) | 已有测试；需源码 fixture | 多 `@author` 合并；空白过滤；**重载方法取错 Javadoc**；嵌套类；`@author:`；并发 | owner 标签错误、重载作者串值、源码不可用时不崩 |
| **[P1] `parallel.SeleniumParallelTestListener`** (123) | 需 TestNG 运行时/stub | 非测试方法跳过；browser 参数回退；driver 创建；类级 `@URL` 不打开；`onBeforeClass` 建目录；失败才截图 | URL 未打开（类级）、`@BeforeMethod` 无 driver、截图路径错误 |
| **[P1] `testng.TestResultListener`** (71) | 需 TestNG 类型/stub | 成功/失败/跳过都 close；browser 回退；null test class；`onFinish` 不 close | driver 泄漏、错误浏览器日志、NPE |
| **[P1] `utils.StringUtils`** (18) | 纯函数 easy | 引号去除边界；`" "` 非空；单引号/单侧引号；null | 所有配置/URL 空格与引号处理的基础缺陷 |
| **[P2] `testng.XmlSuiteBuilder`** (52) | 前两个方法纯；`build()` 需扫描 | suite 名称/并行/线程数/verbose；listener 去重与 trim；`build()` 至少一个 test | 套件元数据错误、listener 未注册 |
| **[P2] `testng.RetryListener`** (20) | Proxy 即可 | null analyzer→设置；已有 analyzer 不覆盖；方法/构造器 null；集成重试 | `@Retry` 不生效/覆盖用户配置 |
| **[P2] `testng.TestNGRunner`** (38) | 需 TestNG 运行时 | 空 suite 正常返回；一个通过测试；无参全量扫描 | 运行器挂起/异常、listener 未生效 |
| **[P2] `utils.MyFileUtils`** (39) | 文件系统 easy | 目录结构/幂等；默认包 NPE；`isFileExisted` 文件/目录；mkdirs 失败静默 | 截图目录缺失导致截图静默失败 |
| **[P2] `report.AllureReport`** (73) | 缺失文件分支 easy；标签需 Allure | 缺失文件 no-op；null 路径 NPE；临时 PNG 附加；描述/标签；jpeg 被标 png | 失败截图丢失、Allure 元数据错误 |
| **[P2] `json.JsonParser`** (85) | 纯函数 easy；死代码 | 1-based 数组；缺失叶子/中间节点；显式 null；primitive 子路径 | 若复活该工具，路径存在性判断错误 |
| **[P2] `parallel.SeleniumParallel`** (145) | 纯方法 easy；driver/截图需浏览器 | `findUrl` 优先级/重置；groups 并集；description trim；`withBoldHTML` null NPE；共享实例字段竞态 | 旧 API 的 URL/groups/描述行为；线程安全 |
| **[P2] `factory.WDManager`** (19) | easy | 无 driver→null；代理同一实例；quit 幂等；线程隔离 | 兼容层返回错误 driver/泄漏 |
| **[P2] `internal.ScreenshotManager`** (47) | 需浏览器；路径可抽纯 | 真实截图；无 driver NPE；目录缺失静默失败；时间戳/路径 | 失败截图丢失、路径错误 |
| **[P2] `report.SelenideAllureSteps`** (41) | easy；已有 2 个 | 未安装 uninstall；重装；线程隔离；listener 选项 | listener 重复/残留、并行线程串步骤 |
| **[P3] `json.JsonLoader`** (72) | 临时文件 easy；死代码 | `accept` 后缀/URI；嵌套/数组/空数组；null 值异常；顶层数组空 key | OWNER JSON 配置解析（若启用） |
| **[P3] `annotations` (15 个)** | 纯元数据 | retention/target；`IGNORE` 前缀约定；`@Retry` 默认 0；`@Author` 默认 `{""}` | 反射发现/浏览器过滤静默失效 |
| **[P3] `utils.AnnotationUtils`** (22) | 纯函数；死代码 | 有/无注解；null NPE | 无（建议删除） |
| **[P3] `internal.OptionalConsumer`** (27) | 纯函数；死代码 | present/empty/链式/null | 无（建议删除） |
| **[P3] `internal.OptionalEx`** (26) | 纯函数；死代码 | present/empty/null | 无（建议删除） |
| **[P4] `utils.SEConfig`** (63) | 难；deprecated + 死代码 | `getString("")`；未知 key；boolean 解析 | 与 `SEConfigs` 重复，建议删除 |
| **[P4] `ClassFinder` 遗留方法** | 死代码 | `findAllTestClassesInPackage`/`findAnnotatedTestClasses`/`find` | 目录扫描被 ClassGraph 取代，建议删除 |
| **[P4] `SeleniumParallel` 遗留方法** | 死代码/旧 API | `getAuthors`/`takeScreenShot`/`withBoldHTML` | 与 `AuthorUtils`/`ScreenshotManager` 重复，建议删除或标 deprecated |

---

## 4. 建议的实施顺序

### 阶段 1：基建 + P0 纯逻辑（1–2 天）
1. Gradle 排除 `testcases.Test_Demo`，新增 `browserTest`。
2. 加 `SEConfigs.reset()`、`CapsUtils.reload()`、`TestNGFilter` 注入/重置、`XmlTestBuilder.reset()`。
3. 新增 `StringUtilsTest`、`SelenideConfigFactoryTest`（normalize 表 + create 规则）、`BrowserUtilsTest`、`SelenideManagerTest`。
4. 新增 `CapsUtilsTest`（独立 JVM 或 reload）：缺失文件、`{"CHROME":{}}`、`{}`、坏 JSON。
5. 修复测试暴露的 `normalizeHubUrl`、caps key、null cli_args 问题。

### 阶段 2：TestNG 套件生成（2–3 天）
1. `TestNGFilterTest`（注入 fixture）+ 修复 group 路径 NPE、trim、非幂等。
2. `TestNGRetryTest` + 把 `maxRetryCount` 改为实例字段。
3. `MethodFinderTest`、`XmlTestBuilderTest`（抽出分片纯函数）、`XmlClassBuilderTest`。
4. `TestGroupUtilsTest`、`SEConfigsTest`（默认值 + reset 后覆盖）。

### 阶段 3：监听器与报告（1–2 天）
1. `SeleniumParallelTestListenerTest`（程序化 TestNG + 无浏览器）。
2. `TestResultListenerTest`、`RetryListenerTest`。
3. `AllureReportTest`（缺失文件 + Allure 生命周期）、`SelenideAllureStepsTest` 补充。
4. `MyFileUtilsTest`、`WDManagerTest`。

### 阶段 4：清理与低价值（按需）
1. 删除死代码：`SEConfig`、`AnnotationUtils`、`OptionalConsumer`、`OptionalEx`、`JsonParser`、`JsonLoader`、`ClassFinder` 遗留方法、`SeleniumParallel` 遗留方法、`CapsUtils.main`/`JsonParser.main`。
2. 若决定保留，再补对应 P3 测试。

---

## 5. 缺陷/边界清单（按严重度）

| # | 位置 | 现象 | 建议 |
|---|---|---|---|
| 1 | `CapsUtils` + `SelenideConfigFactory` | `GSON_CONFIGURATION` 配置了 `DEFAULT_PATH_LEAF_TO_NULL` 但从未使用；若启用，缺失叶子会返回 null，随后 `caps.forEach` / `new ArrayList<>(null)` NPE。当前默认 Configuration 走 `PathNotFoundException`，getter 返回空容器，行为安全但脆弱 | 统一使用一个明确的 Configuration；getter 用 `read == null ? empty : value` 防御；加契约测试固定“缺失 key → 空容器” |
| 2 | `CapsUtils` static 块 | 空文件/坏 JSON 抛 `InvalidJsonException`，只 catch `IOException` → `ExceptionInInitializerError` | catch `RuntimeException`，置空 context 并 warn |
| 3 | `CapsUtils.isJsonFileEmpty` | `{}` 被视为非空；根 `null` NPE | 用 `documentContext.json()`/空 Map 判断 |
| 4 | `SelenideConfigFactory.normalizeHubUrl` | `/wd/hub/`、`/WD/HUB`、前后空格 → 重复路径或非法 URL | trim、去尾斜杠、大小写不敏感 |
| 5 | `SelenideConfigFactory.buildCapabilities` | caps key 用原始 browser 字符串，`" IE "`/别名与 JSON key 不匹配 | 用 `normalizeBrowser` 结果作为 key |
| 6 | `SelenideConfigFactory` cli_args | `null` 元素 → `"null"` 字符串参数 | filter `Objects::nonNull` |
| 7 | `TestNGFilter` group 路径 | 类级 `@Test(groups=...)` 命中时 `MethodFinder` 返回所有方法，随后 `getAnnotation(Test.class).enabled()` NPE | 只返回 `@Test` 方法，或判空 |
| 8 | `TestNGFilter` 配置解析 | 代码未显式 trim（依赖 OWNER 的 `SplitAndTrimTokenizer`）；`run()` 状态累积；内部类 canonical name 与 `$` 名不一致 | 显式 `trim → removeQuoteMark → uppercase`；每次 `run()` 重置 map；统一类名格式 |
| 9 | `TestNGRetry` | `maxRetryCount` 是 static，并行线程互相覆盖 | 改为实例字段 |
| 10 | `BrowserUtils` | 任意注解（如 `@Author`）都会进入浏览器集合；ignore 前缀大小写敏感 | 只接受 `type()`/白名单；前缀统一 uppercase |
| 11 | `ClassFinder` | 仅匹配简单名以 `test` 开头，`LoginTest` 被漏掉；包过滤只精确匹配；`createClass` 返回 null 后可 NPE | 支持 `*Test`/`*Tests` 或注解优先；包前缀匹配；null 过滤 |
| 12 | `MethodFinder` | 类级 group 命中时返回 helper 方法；继承方法永不返回 | 只返回 `@Test`；决定是否扫描父类 |
| 13 | `AuthorUtils` | 重载方法按名字取第一个 Javadoc | 按参数类型匹配 qdox `JavaMethod` |
| 14 | `SeleniumParallelTestListener` | 只处理方法级 `@URL`，类级 `@URL` 不打开；`@BeforeMethod` 拿不到 driver | 统一 URL 解析；在 `beforeInvocation` 之外暴露 driver 或改用 `@BeforeClass` |
| 15 | `MyFileUtils` | 默认包类 `getPackage()==null` NPE；`mkdirs()` 失败静默 | 判空；检查返回值 |
| 16 | `ScreenshotManager` | `IOException` 被吞，返回不存在的路径 | 抛异常/返回 Optional；`afterInvocation` 检查文件存在 |
| 17 | `AllureReport` | `.jpeg` 以 `image/png` 附加；`null` 路径 NPE | 按扩展名推断 MIME；判空 |
| 18 | `XmlTestBuilder` | `size=0` 除零；`xmlSuite` 静态顺序依赖；`XmlTest` 用 HashSet 导致顺序不稳定 | 校验 `size>=1`；构造器注入 suite；用 LinkedHashSet |
| 19 | `SEConfigs` 默认 listeners | 第二个值前有空格；OWNER 1.0.12 会 trim，但这是隐式依赖 | 用测试固定 trim 行为；去掉默认值中的空格更稳妥 |
| 20 | `JsonParser` | 数组 1-based；缺失中间节点返回 `JsonNull` 被误判存在 | 改 0-based；区分 missing/JsonNull |
| 21 | `JsonLoader` | `null` 值抛 `UnsupportedOperationException`；顶层数组写入空 key | 处理 `isJsonNull`；顶层数组用索引 key |
| 22 | `SelenideManager.getWebDriver` | 创建 driver 但未 `open` 时行为与 Javadoc 不一致（可能抛异常） | `hasStartedDriver()` 判断 |
| 23 | `StringUtils.isNullOrEmpty` | `" "` 被认为非空，导致 hub/caps/浏览器名空格缺陷 | 明确是否 trim；配置入口统一 trim |
| 24 | `SeleniumParallel` | 实例字段 `url` 非线程安全；`withBoldHTML(null)` NPE | 局部变量；判空 |
| 25 | `SelenideConfigFactory` / `SeleniumParallelTestListener` | 只读取方法级 `@Headless`/`@Incognito`/`@URL`，类级注解被忽略（`@Target` 声明支持 TYPE） | 用 `method.getDeclaringClass()` 回退；与 `SeleniumParallel.findUrl` 的类级回退保持一致 |

---

## 6. 不在本次范围

- 真实浏览器矩阵（Chrome/Firefox/Edge/IE）的端到端测试。
- Allure HTML 报告的视觉/内容快照。
- Selenium Grid 真实远程执行。
- 性能/稳定性压测。

这些应放入独立的 `browserTest`/integration 任务，不进入默认 `test`。

