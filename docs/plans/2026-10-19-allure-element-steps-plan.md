# 在 Allure 报告中显示 Selenide 元素名称（元素级 Steps）

仓库: `/Users/ansonliao/Documents/repo/github/Selenium-Extensions`
分支: `feature/3.0.0-modernization`
目标: Selenide 的每个元素操作自动成为 Allure step，并显示 `.as("Submit button")` / `@As` 定义的元素名，例如 `$("Submit button") click()`，而不是裸 CSS selector。

## 已确认决策

| 决策 | 结论 |
|---|---|
| Step 文本格式 | 使用官方 `io.qameta.allure:allure-selenide:3.0.0`，格式为 `$("Submit button") click()` |
| `open` 事件 | 保留现有 `Open URL: ...` step，过滤 Selenide 自身 `open` 事件，避免重复 |
| 集成范围 | 内置到库中，所有消费者自动获得，无需额外配置 |

## 已核实事实

- `allure-selenide:3.0.0` 已发布，要求 Selenide 7.x / Java 17+，与当前 Selenide 7.19.0、Allure 3.0.0 兼容；其 POM 不传递 Selenide。
- `AllureSelenide` 的 step 名称 = `event.toString()`；`SelenideLog.toString()` = `$("<element>") <subject>`。
- **重要更正**：`SelenideLogger.run(String source, String subject, Runnable)` 的参数顺序是 **source/element 在前**。`Navigator` 调用 `run("open", absoluteUrl, ...)`，因此 open 事件的 `getElement()` = `"open"`、`getSubject()` = URL。过滤必须用 `getElement()`，不能用 `getSubject()`。
- `SelenideLogger` 的监听器是 **ThreadLocal**：必须在 TestNG worker 线程（`SelenideManager.createDriver`）注册，静态初始化块无效。
- `AllureSelenide` 默认 `screenshots(true)` / `savePageSource(true)` 使用静态 `WebDriverRunner`，与本库的实例 `SelenideDriver` 不兼容，必须关闭；失败截图继续由现有 `ScreenshotManager` 负责。
- Selenide 7.19.0 的元素命名 API：`SelenideElement.as(String)`、字段注解 `com.codeborne.selenide.As`（不存在 `@Name`）。

## 实施步骤

### 1. `build.gradle`
在 `dependencies` 中、`allure-testng` 之后添加：

```groovy
implementation 'io.qameta.allure:allure-selenide:3.0.0'
```

选择 `implementation`：`SelenideAllureSteps` 的公开 API 不暴露 Allure Selenide 类型，消费者运行时仍会获得该依赖。

### 2. 新增 `src/main/java/com/github/ansonliao/selenium/report/SelenideAllureSteps.java`

```java
package com.github.ansonliao.selenium.report;

import com.codeborne.selenide.logevents.LogEvent;
import com.codeborne.selenide.logevents.LogEventListener;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;

/**
 * Registers the official Allure Selenide listener on the current thread.
 * Selenide listeners are ThreadLocal, so this is invoked per TestNG worker
 * thread from {@link com.github.ansonliao.selenium.driver.SelenideManager}.
 */
public final class SelenideAllureSteps {

    public static final String LISTENER_NAME = "selenium-extensions-allure-selenide";
    private static final String OPEN_ELEMENT = "open";

    private static final LogEventListener LISTENER =
            new OpenEventFilter(new AllureSelenide()
                    .screenshots(false)
                    .savePageSource(false));

    private SelenideAllureSteps() {
    }

    public static void install() {
        if (!SelenideLogger.hasListener(LISTENER_NAME)) {
            SelenideLogger.addListener(LISTENER_NAME, LISTENER);
        }
    }

    public static void uninstall() {
        SelenideLogger.removeListener(LISTENER_NAME);
    }

    static boolean isOpenEvent(LogEvent event) {
        return event != null && OPEN_ELEMENT.equals(event.getElement());
    }

    private static final class OpenEventFilter implements LogEventListener {
        private final LogEventListener delegate;

        private OpenEventFilter(LogEventListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void beforeEvent(LogEvent event) {
            if (!isOpenEvent(event)) {
                delegate.beforeEvent(event);
            }
        }

        @Override
        public void afterEvent(LogEvent event) {
            if (!isOpenEvent(event)) {
                delegate.afterEvent(event);
            }
        }
    }
}
```

要点：open 事件必须同时跳过 `beforeEvent` 与 `afterEvent`，否则 `AllureSelenide.afterEvent` 会错误地 `stopStep()`。

### 3. `driver/SelenideManager.java` 接线
- `createDriver(...)`：`DRIVER.set(driver)` 之后调用 `SelenideAllureSteps.install()`。
- `closeDriver()`：先 `SelenideAllureSteps.uninstall()`，再关闭 driver；去掉 `driver == null` 的提前 return，确保监听器总能移除。
- `SeleniumParallel.startWebDriver` 走同一个 `createDriver`，无需额外修改。

### 4. `src/test/java/testcases/Test_Demo.java`
为元素命名并覆盖点击流程：

```java
SelenideDriver driver = SelenideManager.getSelenideDriver();

SelenideElement username = driver.$("#username").as("Username field");
SelenideElement password = driver.$("#password").as("Password field");
SelenideElement submit = driver.$("button[type=submit]").as("Submit button");

username.shouldBe(visible).setValue("tomsmith");
password.shouldBe(visible).setValue("SuperSecretPassword!");
submit.click();

driver.$(".flash.success").as("Flash message")
        .shouldBe(visible)
        .shouldHave(text("You logged into a secure area!"));
```

预期 `build/allure-results/*-result.json` 中 `f2` 的 steps：
`Open URL: ...`（仅一条）、`$("Username field") should be(visible)`、`$("Username field") set value(tomsmith)`、`$("Password field") ...`、`$("Submit button") click()`、`$("Flash message") ...`。
不应出现 `$("open") http...`。

### 5. 文档
- `README.md`：
  - 技术栈表 Reporting 行补 `allure-selenide 3.0.0`
  - 新增 “Selenide element steps” 子节：`.as(...)`、`@As`、step 格式、无别名退化为 selector、open 过滤、截图归属、仅 Selenide API 会记录
  - “Using Selenide in tests” 示例改为带 `.as(...)`
- `CHANGELOG.md`：3.0.0 `### Added` 增加 Selenide element steps 条目

### 6. 可选回归测试
`src/test/java/com/github/ansonliao/selenium/report/SelenideAllureStepsTest.java`：
- `install()` 幂等（连续两次后 `hasListener` 为 true）
- `uninstall()` 后为 false
- `isOpenEvent(new SelenideLog("open", "https://..."))` 为 true；`new SelenideLog("Submit button", "click()")` 为 false

## 验证

```bash
cd /Users/ansonliao/Documents/repo/github/Selenium-Extensions
./gradlew checkstyleMain checkstyleTest
./gradlew clean test --tests 'testcases.Test_Demo'
python3 - <<'EOF'
import json, glob
for f in glob.glob('build/allure-results/*-result.json'):
    d = json.load(open(f))
    if d.get('name') == 'f2':
        print([s['name'] for s in d.get('steps', [])])
EOF
./gradlew allureReport
```

检查点：
- 出现 `$("Submit button") click()` 等元素 step
- 只有一条 `Open URL: ...`，没有 `$("open") ...`
- step 上没有 `Screenshot` / `Page source` 附件（截图仍由 `ScreenshotManager` 在失败时附加）
- JDK 21 / 25 执行 `./gradlew build -x test` 均通过

## 风险与边界

- 仅 Selenide API（`driver.$()`）产生 step；`WDManager.getDriver()` 的原生 Selenium 操作不会记录。
- 未命名的元素显示 selector，例如 `$("#username") set value(tomsmith)`；需通过 `.as()` / `@As` 命名。
- 密码等参数会出现在 step 名称中；生产测试应避免真实敏感值。
- 消费者不要再用其他名字注册第二个 `AllureSelenide`，否则会产生重复 step。
- `allure-selenide` 必须与 Allure 3.x 对齐，不可降级到 2.x。
- Selenide 升级后需回归 `open` 事件判断（当前 `getElement()` 为 `"open"`）。

## 关键文件

- 修改：`build.gradle`、`src/main/java/com/github/ansonliao/selenium/driver/SelenideManager.java`、`src/test/java/testcases/Test_Demo.java`、`README.md`、`CHANGELOG.md`
- 新增：`src/main/java/com/github/ansonliao/selenium/report/SelenideAllureSteps.java`、可选 `src/test/java/com/github/ansonliao/selenium/report/SelenideAllureStepsTest.java`
