# Selenium-Extensions：代码风格门禁（Skill + pre-push Hook）

## 目标

在代码 push 之前建立可重复、可强制、可自动修复的风格门禁：

1. **单一裁判**：继续以 Checkstyle + `google-style.xml` 为唯一事实来源，不引入 Spotless / google-java-format。
2. **自动化**：项目级 Skill 负责“检测 → 解析 → 按类修复 → 复检 → 汇报”的完整闭环。
3. **强制化**：版本化的 pre-push hook 在 `git push` 前跑 Checkstyle，违规即拦截。
4. **本次一并完成基线归一化**（含 import 改为 static 优先），交付时仓库处于零违规状态。

## 已核实的现状

| 项目 | 事实 |
|---|---|
| 构建 | Gradle 9.6.1 wrapper，Java 21，`java-library` + `maven-publish` |
| Checkstyle | `10.26.1`，`configFile = google-style.xml`，`ignoreFailures = false`，`maxWarnings = 0`，`maxErrors = 0` |
| 实际风格 | 缩进 **4 空格**、行宽 **120**、import 顺序 `com.google → 第三方 → java → static`、禁止 tab、禁止 star import（warning 但因 maxWarnings=0 同样致命） |
| 源码规模 | 48 个 Java 文件 / 2,693 行；最长行 119（行宽零欠账）；无 star import |
| static import | 17 个文件使用，改造为 static 优先后需机械重排 |
| CI | `.github/workflows/build.yml` 执行 `./gradlew build -x test`，已包含 checkstyle |
| Git | 当前为 worktree（`.git` 是文件），`core.hooksPath` 未设置，无任何 hook |
| Skill 现状 | 项目内无 `.workbuddy/skills`；用户级 skill 位于 `~/.workbuddy/skills` |

## 方案总览

```
风格契约层   google-style.xml（改为 static 优先）
     │
执行层       .workbuddy/skills/java-style-guard/   ← Skill：诊断 + 定向修复
     │         ├── scripts/style_gate.sh          ← 跑 Checkstyle 并输出可读违规清单
     │         └── scripts/reorder_imports.py     ← 4 组 import 的确定性排序器
     │
强制层       .githooks/pre-push + scripts/install-hooks.sh
             .github/workflows/build.yml（已有，不改）
```

## 实施步骤

### 0. 等待其它任务结束并同步分支（前置条件）

当前同一仓库存在两个活动面，必须先等其静默，避免语义冲突：

| 位置 | 分支 | 状态 |
|---|---|---|
| `/Users/ansonliao/Documents/repo/github/Selenium-Extensions` | `feature/3.0.0-modernization` | 领先本 worktree 1 个提交（`4704a76`），工作区干净，16 分钟前仍有提交 |
| `/Users/ansonliao/WorkBuddy/Worktrees/Selenium-Extensions/master-e0513d3e` | `worktree/3.0.0-modernization-wip` | 会话 `95575` 心跳已停 26 分钟；26 个 Java 文件 + `google-style.xml` 未提交改动 |
| 本 worktree | `workbuddy/feature-3-0-0-modernization-bb228eef` | 干净，`8c2e384`，落后 `feature` 分支 1 个提交 |

执行规则：

1. 监测上述两处的 `HEAD`、文件 mtime、`.git/index`、`*.lock` 与其它会话心跳；**连续 5 分钟无变化**才视为静默。
2. 静默后先 `git merge --ff-only feature/3.0.0-modernization`，把本分支对齐到最新提交，再开始改文件。
3. `scripts/install-hooks.sh` 会写入仓库级 `core.hooksPath`，**放到最后一步执行**，避免在其他 worktree 推送时中途生效。
4. 若等待超过 45 分钟仍未静默，暂停并向用户确认，不擅自开始。

### 1. 风格契约：import 改为 static 优先

修改 `google-style.xml:151-152`：

```xml
<property name="customImportOrderRules"
          value="STATIC###SPECIAL_IMPORTS###THIRD_PARTY_PACKAGE###STANDARD_JAVA_PACKAGE"/>
```

保留 `specialImportsRegExp=com.google` 与 `sortImportsInGroupAlphabetically=true`。目标分组顺序：

1. `import static ...`（组内字母序）
2. `com.google.*`
3. 其他第三方（`org.*`、`com.codeborne.*` 等）
4. `java.*` / `javax.*`

> 注意：Checkstyle 的 `CustomImportOrder` 要求组名与规则串完全匹配，`STATIC` 必须显式出现在规则串中，否则静态导入会被当作普通导入处理。改完先跑一次 `checkstyleMain` 验证配置本身合法。

### 2. 创建项目级 Skill

目录：`.workbuddy/skills/java-style-guard/`

```
java-style-guard/
├── SKILL.md
├── references/
│   └── checkstyle-rules.md
└── scripts/
    ├── style_gate.sh
    └── reorder_imports.py
```

**SKILL.md frontmatter**

```yaml
---
name: java-style-guard
description: Enforce and auto-fix the Selenium-Extensions Java style contract
  (Checkstyle 10 + google-style.xml: 4-space indent, 120-column limit,
  static-first imports) before commit or push. This skill should be used when
  Java sources are added or refactored, when Checkstyle fails, or when
  preparing a branch for push.
agent_created: true
---
```

**SKILL.md 正文结构**

1. **触发条件**：Java 源码改动后、`./gradlew check`/`checkstyle*` 失败时、commit/push 前、用户说“format / fix style / 风格检查”。
2. **风格契约速查表**：缩进 4、续行 4/8、行宽 120、import 四组、禁止 `*`、必须大括号、单行单语句、运算符换行在行首（`OperatorWrap=NL`）、`.` 换行在行首（`SeparatorWrap DOT=nl`）、禁止 tab、Javadoc 缩进。
3. **标准工作流**
   - 定范围：默认 `git diff --name-only HEAD` + 未跟踪的 `*.java`；用户说全量时用 `git ls-files '*.java'`。
   - 跑门禁：`bash .workbuddy/skills/java-style-guard/scripts/style_gate.sh`。
   - 读违规：脚本把 `build/reports/checkstyle/{main,test}.xml` 解析成 `file:line:col [RULE] message`，按文件分组。
   - 按固定顺序修：**import → 缩进/空白 → 大括号/语句结构 → 行宽 → 命名/Javadoc**。每修完一类立即复跑，避免一次改太多无法定位。
   - 零违规后跑 `./gradlew compileJava compileTestJava -q` 确认没改坏。
   - 输出汇报表：修改文件数、按规则统计的修复数、剩余违规、执行的命令、结果。
4. **硬性约束**
   - 不得为通过检查而修改 `google-style.xml`（除非用户明确要求改规则）。
   - 不得使用 `// CHECKSTYLE:OFF`、`@SuppressWarnings("checkstyle:...")` 或降低 `maxWarnings`。
   - 不得顺手重排无关文件；单个文件内也不得做与违规无关的格式化。
   - 行宽超限优先“提取变量 / 提前换行”，不要为凑行宽改变语义。
5. **失败处理**：Gradle 下载失败、JDK 版本不符、报告缺失时的判断与提示。

**references/checkstyle-rules.md**

逐条列出 `google-style.xml` 中真正会触发的规则：规则名 → 约束 → 修复手法 → 正反例。重点覆盖：

- `LineLength` 120
- `Indentation` 4/0/4/4/4/2
- `CustomImportOrder` + `AvoidStarImport`
- `NeedBraces`、`LeftCurly`、`RightCurly`（含 alone）
- `WhitespaceAround`、`EmptyLineSeparator`、`GenericWhitespace`
- `OperatorWrap(NL)`、`SeparatorWrap(DOT=nl, COMMA=EOL)`
- `OneStatementPerLine`、`MultipleVariableDeclarations`
- `FileTabCharacter`、`EmptyCatchBlock`、`CommentsIndentation`
- `MethodName` / `ParameterName` / `TypeName` 的实际正则（注意它们是从其他项目继承的宽松正则，不要按 Google 默认规则误判）

**scripts/style_gate.sh**

- `set -euo pipefail`，定位 repo root（`git rev-parse --show-toplevel`）。
- 执行 `./gradlew checkstyleMain checkstyleTest --console=plain`。
- 无论成功失败，用内联 Python 解析 `build/reports/checkstyle/main.xml` 与 `test.xml`，输出：
  ```
  src/main/java/.../Foo.java
    42:9  [CustomImportOrder] ...
    87:121 [LineLength] ...
  ```
- 汇总 `N violations in M files`；零违规输出 `CHECKSTYLE OK`。
- 退出码：0 通过 / 1 有违规 / 2 报告缺失或构建失败（附排查提示）。
- 支持 `--all`（默认就是全量，保留参数位以便未来做增量）。

**scripts/reorder_imports.py**

- 纯标准库，Python 3.13，无第三方依赖。
- 用法：`reorder_imports.py [--write|--check] <files...>`；`--check` 只报 diff 并以 1 退出。
- 分类规则与 `google-style.xml` 完全一致：static(0) → `com.google.*`(1) → 其他(2) → `java|javax`(3)，组内 ASCII 字母序。
- 保留 `package` 与文件头注释；组间恰好一个空行；幂等（连续跑两次无 diff）。
- 安全边界：遇到 import 块内注释、非标准行时跳过该文件并报警，不强行改写。

### 3. pre-push Hook

**`.githooks/pre-push`**（提交时保持可执行位 755）

- `set -euo pipefail`。
- 从 stdin 读取 `local_ref local_sha remote_ref remote_sha`：
  - `remote_sha` 全 0（新分支）→ `base = git merge-base HEAD origin/master`；失败则视为“全量”。
  - 否则 `base = remote_sha`。
- `git diff --name-only "$base" "$local_sha"`；若改动不涉及 `src/**/*.java`、`google-style.xml`、`build.gradle`，直接放行（文档类提交零开销）。
- 否则执行 `./gradlew checkstyleMain checkstyleTest --console=plain`；失败时打印：
  ```
  ✗ Checkstyle failed — push blocked.
    Fix:  ./gradlew checkstyleMain checkstyleTest
    Help: .workbuddy/skills/java-style-guard/SKILL.md
    HTML: build/reports/checkstyle/main.html
  ```
  并以 1 退出。
- 不提供 `SKIP` 环境变量；紧急情况用 `git push --no-verify`（README 中注明这是审计可见的例外）。

> Worktree 说明：`core.hooksPath=.githooks` 是仓库级配置、各 worktree 共享；相对路径会解析到各自 worktree 根目录。老分支没有该目录时 Git 会静默跳过，不会报错。

**`scripts/install-hooks.sh`**

```bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-push
git config --get core.hooksPath   # 回显确认
```

幂等、可在任意 worktree 执行。

### 4. 基线归一化与验证

按顺序执行：

```bash
# 4.1 改规则后先验证配置合法
./gradlew checkstyleMain checkstyleTest

# 4.2 机械重排 17 个含 static import 的文件
python3 .workbuddy/skills/java-style-guard/scripts/reorder_imports.py --write \
  $(git ls-files 'src/**/*.java')

# 4.3 复跑门禁，修剩余违规（预计主要是 import 分组与个别空行）
bash .workbuddy/skills/java-style-guard/scripts/style_gate.sh

# 4.4 编译与完整校验（跳过需要浏览器的测试）
./gradlew clean build -x test

# 4.5 安装 hook 并验证
bash scripts/install-hooks.sh
```

**Hook 实测**：临时在一个 Java 文件插入两空格缩进 → `git push` 应被拦截 → 还原后放行。测试不要真的推到远端，可用 `git push --dry-run` 或直接以样例 stdin 调用 hook。

### 5. 文档

- `README.md` 新增 `## Code style` 小节（放在 `## Requirements` 之后）：
  - 规则一句话总结（4 空格 / 120 列 / static-first）。
  - `./gradlew checkstyleMain checkstyleTest` 与 `bash scripts/install-hooks.sh`。
  - 被 hook 拦截时的三条排查命令。
- `CHANGELOG.md` 顶部新增 `## [Unreleased]` → `### Changed`：
  - “Import order is now static-first (Checkstyle `CustomImportOrder`); added a pre-push style gate and the `java-style-guard` project skill.”

## 文件清单

| 操作 | 路径 | 说明 |
|---|---|---|
| 修改 | `google-style.xml` | `customImportOrderRules` 改为 static 优先 |
| 修改 | 17 个含 static import 的 `.java` | 仅调整 import 块顺序 |
| 新建 | `.workbuddy/skills/java-style-guard/SKILL.md` | Skill 主流程与硬性约束 |
| 新建 | `.workbuddy/skills/java-style-guard/references/checkstyle-rules.md` | 规则 → 修复手法速查 |
| 新建 | `.workbuddy/skills/java-style-guard/scripts/style_gate.sh` | 跑 Checkstyle + 解析 XML 报告 |
| 新建 | `.workbuddy/skills/java-style-guard/scripts/reorder_imports.py` | 四组 import 确定性排序 |
| 新建 | `.githooks/pre-push` | 推送前强制 Checkstyle |
| 新建 | `scripts/install-hooks.sh` | 安装 `core.hooksPath` |
| 修改 | `README.md` | Code style 小节 |
| 修改 | `CHANGELOG.md` | Unreleased 记录 |

## 验收标准

1. `./gradlew checkstyleMain checkstyleTest` → `BUILD SUCCESSFUL`，0 violations。
2. `./gradlew clean build -x test` → `BUILD SUCCESSFUL`（compile + checkstyle + jars）。
3. `reorder_imports.py --check` 对全仓返回 0；连续两次 `--write` 无 diff（幂等）。
4. 故意制造违规后 `git push` 被 hook 拦截，提示信息包含修复命令；还原后放行。
5. `.workbuddy/skills/java-style-guard/SKILL.md` frontmatter 可被解析，`name` 与目录名一致。
6. `git status` 中只包含预期改动，无 `build/`、`.gradle/` 等产物。

## 风险与边界

- **不引入自动格式化器**：Eclipse formatter 无法稳定复刻 `OperatorWrap=NL`、`SeparatorWrap DOT=nl`、`CustomImportOrder` 的组合，产出可能与 Checkstyle 冲突；因此修复由 Skill 按规则定向完成，而不是“一键全量重排”。
- **Checkstyle 覆盖面有限**：它不检查命名语义、异常处理、并发正确性。Skill 只承诺“零 Checkstyle 违规”，不承诺架构级重构。
- **Hook 可被 `--no-verify` 绕过**：这是 Git 的固有限制；CI 的 `./gradlew build -x test` 仍是最终兜底。
- **首次 Gradle 运行成本**：需下载依赖/校验工具；hook 首次执行可能 30s+，之后增量约数秒。
- **`.` 开头的 `.workbuddy` 会被提交**：只提交 `skills/` 子目录，不提交 `memory/` 等本机数据。
