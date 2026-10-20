# AGENTS.md

Working agreement for AI agents and contributors in this repository.

## Rules

### 1. Every change gets its own commit

After completing a change, create a Git commit for it so the history stays
reviewable and any change can be traced or rolled back. Do not leave the
working tree dirty when a task is finished, and do not mix unrelated changes
into a single commit.

### 2. Every change ships with tests

Every change must add or update the tests that cover it. Before handing work
over to the user, the whole verification must pass:

```bash
./gradlew test checkstyleMain checkstyleTest
```

`./gradlew browserTest` drives a real browser and is not part of the default
gate. Run it (and say so) only when a change touches the demo in
`src/test/java/testcases`.

### 3. Keep documents in the workspace

Plans, design notes and reports belong in this repository — plans live in
[`docs/plans/`](docs/plans/), next to the code they describe. Do not leave them
in an assistant's private state directory such as `~/.workbuddy/plans`: a
document that only exists there is invisible to the repository, to reviewers
and to the next machine.

## Project conventions

| Item | Value |
| --- | --- |
| Language level | Java 21 (`options.release = 21`) |
| Build | Gradle (`./gradlew`), single module |
| Test framework | TestNG, run through the `test` task |
| Style gate | Checkstyle with [`google-style.xml`](google-style.xml), max 120 columns, zero warnings allowed |
| Coverage | JaCoCo, report generated after `test` |
| Reports | Allure (`allure-testng`, `allure-selenide`) |

### Test layout

- `src/test/java/.../*Test.java` — unit tests, no browser required.
- `src/test/java/.../testfixtures/` — fixtures shared by the unit tests. They
  are deliberately outside the `*Test` naming pattern so Gradle never runs
  them directly.
- `src/test/java/testcases/` — browser demo, excluded from `test` unless
  `-PbrowserTests` is passed.

### Useful commands

```bash
./gradlew test                      # unit tests only
./gradlew checkstyleMain checkstyleTest
./gradlew test --tests "com.github.ansonliao.selenium.json.JsonParserTest"
./gradlew browserTest               # requires a local browser and driver
```
