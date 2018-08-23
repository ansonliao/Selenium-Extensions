# Selenium-Extensions
[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/saikrishna321/AppiumTestDistribution)
[![badge-jdk](https://img.shields.io/badge/jdk-8-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![Build Status](https://travis-ci.org/ansonliao/Selenium-Extensions.svg?branch=master)](https://travis-ci.org/ansonliao/Selenium-Extensions/builds/)

This is a extension for Selenium in Java to extend Selenium for make Selenium test case can be run in parallel, and make it easy to support multiple browsers.
Integrated beautiful and powerful HTML test report: [ExtentReports](http://extentreports.com/).

## Maven Dependency
Add the below dependencies in your `pom.xml` (Master)

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
    <version>2.3.7-SNAPSHOT</version>
</dependency>
```

## Parallel
Introduce [TestNG](http://testng.org/doc/index.html) framework to support Selenium test case parallel run. 
Parallel mode is by Test, the tag `<test>` of TestNG test suite xml.

## Visual TestNG test suite XML generation
No need to provide TestNG test suite XMl file to start the test, TestNG test suite XML file will be generated programmatically.

## Report
### Testing Report

After test completed, Extents test report can be found `target/ExtentReports.html`.

### Screenshot for test fail
Screenshot can be found at `target/screenshots` for the test failed.

## Download WebDriver binary automatically
`Selenium-Extensions` will download `WebDriver` binaries for Test automatically, 
no need download the binary manually before the test start.

### WebDriverManager

For download the webdriver binary, introduced the dependency `webdrivermanager` of `io.github.bonigarcia`, for more detail the setting for the `Web Driver Manager`, please refer to the official document: [Read Me](https://github.com/bonigarcia/webdrivermanager/), and the configuration file: [Configuration](https://github.com/bonigarcia/webdrivermanager#configuration) .

## Multiple browsers support
Browser support:

- **Chrome**: Mac, Linux, Windows
- **FireFox**: Mac, Linux, Windows
- **PhantomJs**: Mac, Linux, Windows
- **Opera**: Mac, Linux, Windows
- **Edge**: Windows
- **InternetExplorer**: Windows
- **Safari**: DEPRECATED

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

Even, you can annotated your test class:
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

From the above code, test class `BrowserTest` was annotated by `@Chrome`, so all test method of TestNG of test class `BrowserTest` will be run at browser `Chrome`.
So finally, test method:
* `f1()`: will be run at browsers `Edge` and `Chrome` (if Windows OS platform);
* `f2()`: will be run at browsers `Firefox` and `Chrome`.

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

When the test start (test cases `f1`, `f2`), the URL will be open automatically, URL `https://www.google.com/` will be 
launched for test case `f1`, URL `https://www.wordpress.com` will be launched for test case `f2`.

also, you can annotate test class by `@URL`, such as:
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

Above two test cases `f1`, `f2` will be lauch URL `http://www.google.com` automatically.

## Run test case

You need to create the `runner` to trigger the testing run.

Below is a sample, it is the simplest sample run all test cases, only run method `TestNGRunner.Run()`.

```java
public class MyTestRunner {
    @Test
    public void run() {
        TestNGRunner.Run();
    }
}
```

When test runner created, we can trigger the test by `Maven` command, as below.

```bash
mvn clean test -Dtest=MyTestRunner
```

## Filter
`Selenium-Extentsions` support run the test case by filters.
The filter includes:

- *All*: run test cases in test project
- *Package*: run test cases by specify packages
- *Test Group*: run test cases by specify test groups which in TestNG test annotation `@Test`
- *Test class*: run test cases by specify test classes which are TestNG test class
- *Browser*: run test cases by specify browser, such as `CHROME`, `FIREFOX`

### Full configuration list
You can create your owned configuration and named to `seleniumextentsions.properties` or `se.properties`.
Please note that make sure put your owned configuration file to the resources directory of under `src` that the main Java code (`src/main/resources`) or test Java code (`src/test/resources`).

| **Key** | **Description** | **Value** | **Default Value**|
| --- | --- | ---| --- |
| add.browser.group.to.report | Whether add testing browser to ExtentReports as a group of ExtentReport | Boolean: `true` / `false` | `false` (Java keyword in boolean, case sensitive) |
| run.by.browsers | Run all TestNG test by the specified browser(s). This setting will ignore the existing browser annotation of TestNG test class, and will resign all valid TestNG test class to this setting browser |  `CHROME`, `FIREFOX`, `IE`, `OPERA`, `PHAMTOMJS`, `EDGE`, `INTEREXPLORER`| null |
| default.browser | Setting the default browser for the valid TestNG test case which without any valid browser annotation| `CHROME`, `FIREFOX`, `IE`, `OPERA`, `PHAMTOMJS`, `EDGE`, `INTEREXPLORER` | `CHROME` (no case sensitive) |
| browser.annotation.package | The package that places all browser annotations and all ignore browser annotations. | string | `com.github.ansonliao.selenium.annotations.browser` |
| test.tag.class.size.of.testngxml | The setting for how many TestNG test class will be held for each `Test` tag of TestNG XML | positive integer | 10 |
| testing.package.names | Specified what test script under the package(s)  will be run | string or string of list | nulll |
| testing.browser.names | Run the test cases which specified annotated by browser annotation match to this setting | string or string of list | null |
| testing.test.groups | Run the test cases which are assigned to the testing group of TestNG of this setting | string or string of list | null |
| testing.testng.classes | Specified what valid TestNG testing class will be executed | string or string of list | null |
| testng.listeners | Provided your owned TestNG listener(s) | string or string of list | `com.github.ansonliao.selenium.testng.TestResultListener`, `com.github.ansonliao.selenium.parallel.SeleniumParallelTestListener` |
| testng.class.prefix | Specified the specificed TestNG testing class that the leading, the TestNG testing class scanned will only filter the testing class which the class name starts with the setting | string | `test` (no case sensitive) |

> Please note that, for the setting can be string of list, please use comma (`,`) as the separator

### Sample configuration: `src/test/resources/seleniumextensions.properties`

```
add.browser.group.to.report=true
run.by.browsers=CHROME, FIREFOX
default.browser=CHROME		// can be removed
browser.annotation.package=com.github.ansonliao.selenium.annotations.browser	// can be removed
testing.package.names=example.phase1, example.phase2
test.tag.class.size.of.testngxml=20
testing.test.groups=REGRESSION, SMOKE
testng.listeners=example.listeners.mylistener1, example.listeners.mylistener2

```

### Provide  filters as Maven command line argument

also you can provide the configuration by Maven's command line argument:

Execute `Smoke` test case (TestNG test group includes `SMOKE`) only

```bash
mvn clean test -Dtest=your_test_runner -Dtesting.test.groups=SMOKE
```

Or have a package that place all `Smoke` test case together and the package name is `example.smoke`:

```bash
mvn clean test -Dtest=your_test_runner -Dtesting.package.names=example.smoke
```

 ### Provide filters programmatically

In your Test Runner, before calling `TestNGRunner.Run(...)`, you can set the configuration via set the values of `System.Properties` or `System.envs` of Java:

```java
public class RegressionRunner {
    @Test
    public void runner() {
        System.setProperty("testing.test.groups", "REGRESSION");
        TestNGRunner.run();
    }
}

public class SmokeRunner {
    @Test
    public void runner() {
        System.setProperty("testing.test.groups", "SMOKE");
    }
}

public class ChromeTestRunner {
    @Test
    public void runner() {
        System.setProperty("testing.browser.names", "CHROME");
        TestNGRunner.run();
    }
}

```



#### Different between `default.browser` and `testing.browser.names`

- `testing.browser.names`: The program will look for the valid TestNG testing class and testing method's browser annotations and check the browser annotation of test case whether contained in the setting of `testing.browser.names`, and then execute the test case which annotated by the browser annotation contains in the setting.
- `run.by.browsers`: The programm will look for the valid TestNG test case and then ignore the browser annotation(s), and then add browser(s) of `run.by.browsers` as the new browser annotation(s) for the valid TestNG test case.

**Sample:**

Let's say we have two test methods below:

```java
public void TestCaseSample {
    
    @Test(groups={"Regresion", "SMOKE"})
    @Chrome
    public void regression1() {
        // testing code here
    }
    
    @Test(groups={"Regression", "BVT"})
	@Firefox
    public void regression2() {
        // testing code here
    }
}
```

1. Setting `run.by.browsers=EDGE, PHANTOMJS` applied, those two test cases will be executed against to browser `Edge`, `PhantomJS`
2. Setting `testing.browser.names=FIREFOX` applied, only `regression2()` of those two test cases will be executed, because only test case `regression2()` have browser `Firefox` annotation
3. Setting `testing.browser.names=PHANTOMJS` applied, no test case will be executed, because no test case annotated to browser `PHANTOMJS`


