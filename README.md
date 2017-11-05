# Selenium-Extensions

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
    <version>2.1.0</version>
</dependency>
```

## Parallel
Introduce [TestNG](http://testng.org/doc/index.html) framework to support Selenium test case parallel run. 
Parallel mode is by Test, the tag `<test>` of TestNG test suite xml.

## Visual TestNG test suite XML generation
No need to provide TestNG test suite XMl file to start the test, TestNG test suite XML file will be generated programmatically.

## Test Report
After test completed, Extents test report can be found `target/ExtentReports.html`.

### Screenshot for test fail
Screenshot can be found at `target/screenshots` for the test failed.

## Download WebDriver binary automatically
`Selenium-Extensions` will download `WebDriver` binaries for Test automatically, 
no need download the binary manually before the test start.

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
public class BrowserTest extends UserBaseTest {

    @Test
    @Chrome
    public void f1() {
        openUrl(getUrl());
        System.out.println("I'm in f1 - " + browserName + " : " + Thread.currentThread().getId());
    }

    @Test
    @Firefox
    public void f2() {
        openUrl(getUrl());
        System.out.println("I'm in f2 - "+ browserName + " : " + Thread.currentThread().getId());
    }
}
```

Even, you can annotated your test class:
```java
@Chrome
public class BrowserTest extends UserBaseTest {

    @Test
    @Edge
    public void f1() {
        openUrl(getUrl());
        System.out.println("I'm in f1 - " + browserName + " : " + Thread.currentThread().getId());
    }

    @Test
    @Firefox
    public void f2() {
        openUrl(getUrl());
        System.out.println("I'm in f2 - "+ browserName + " : " + Thread.currentThread().getId());
    }
}
```

From the above code, test class `BrowserTest` was annotated by `@Chrome`, so all test method of TestNG of test class `BrowserTest` will be run at browser `Chrome`.
So finally, test method:
* `f1()`: will be run at browsers `Edge` and `Chrome` (if Windows OS platform);
* `f2()`: will be run at browsers `Firefox` and `Chrome`.

## Dynamic URL support for test method
```java
public class BrowserTest extends UserBaseTest {

    @Test
    @Chrome
    @URL("https://www.google.com/")
    public void f1() {
        openUrl(getUrl());
        System.out.println("I'm in f1 - " + browserName + " : " + Thread.currentThread().getId());
    }

    @Test
    @Firefox
    @URL("https://www.wordpress.com")
    public void f2() {
        openUrl(getUrl());
        System.out.println("I'm in f2 - "+ browserName + " : " + Thread.currentThread().getId());
    }
}
```

also, you can annotate test class by `@URL`, such as:
```java
@URL("http://www.google.com")
public class BrowserTest extends UserBaseTest {

    @Test
    @Chrome
    public void f1() {
        openUrl(getUrl());
        System.out.println("I'm in f1 - " + browserName + " : " + Thread.currentThread().getId());
    }

    @Test
    @Firefox
    public void f2() {
        openUrl(getUrl());
        System.out.println("I'm in f2 - "+ browserName + " : " + Thread.currentThread().getId());
    }
}
```

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

## Setting your filter

The keywords for you to setting the filters:

- Package: `testPackages`
- Test Group: `testGroups`
- Test Class: `testClasses`
- Browsers: `testBrowsers`


There are two ways to setting your filter for package list, test groups list, test class list, and browser list.

1. Setting by `System Property`, can set `System Property` by `mvn` command or `export` method of Unix/Linux OS:
    
    Maven:
    ```bash
    mvn clean test -Dtest=MyTestRunner -DtestBrowsers="CHROME, FIREFOX, SAFARI" -DtestClasses="com.example.Test_HomePage.java, com.example.Test_LoginPage.java"
    ```
    
    Export:
    ```bash
    export testClasses="com.example.Test_HomePage.java, com.example.Test_LoginPage.java"
    export testBrowsers="CHROME, FIREFOX, SAFARI"
    export testPackages="com.example.test1, com.example.test2"
    export testGroups="@SMOKE, @REGRESSION, @BLOCKER"
    ```   
    
    after that, add method call in your test runner Java class, before `TestNGRunner.Run()`, sample is below.
    
    ```java
    public class MyTestRunner {
    
        @Test
        public void run() {
            DefaultSettingUtils.set();     // try to fetch filters from system properties setting
            TestNGRunner.Run();
        }
    }
    ```

2. Setting by programming

    Setting custom filter programmatically is easy, `DefaultSettingUtils` can approach it.
    Below is the sample.
    
    ```java
    public class MyTestRunner {
    
        @Test
        public void run() {
            List<String> packageList = com.google.common.collect.Lists.newArrayList("com.example.test1", com.example.test2);
            List<String> groupList = com.google.common.collect.Lists.newArrayList("@SMOKE", "@REGRESSION", "@BLOCKER");
            DefaultSettingUtils.setTestingPackages(packageList);
            DefaultSettingUtils.setTestingGroups(groupList);
            
            TestNGRunner.Run();
        }
    }
    ```
    
    For example, we can create a test runner for `Smoke Test`, to run the test cases tagged as `@SMOKE` test group.
    
    ```java
    public class SmokeTestRunner {
    
        @Test
        public void run() {
            List<String> groupList = com.google.common.collect.Lists.newArrayList("@SMOKE");
            DefaultSettingUtils.setTestingGroups(groupList);
            
            TestNGRunner.Run();
        }
    }
    ```
    
    or in Jenkins, export `testGroups` before Maven command to trigger the test run.
    
    ```bash
    export testGroups="@SMOKE"
    ```
    
    Make sure the test runner includes: `DefaultSettingUtils.set();`
    
    ```java
    public class SmokeTestRunner {
        
            @Test
            public void run() {
                DefaultSettingUtils.set();
                TestNGRunner.Run();
            }
    }
    ```
    
    And then the command to trigger the test by Maven command:
    
    ```bash
    mvn clean test -Dtest=SmokeTestRunner
    ```

### Importance: Put all items of packages, test classes, test groups, test browsers in a string,
separated by comma `,`. please check below:

```bash
# below are wrong
$: mvn test -Dtest=MyTestRunner -DtestGroups="@SMOKE" -DtestGroups="@BLOCKER"


$: export testGroups="@SMOKE"
$: export testGroups="@BLOCKER"
$: export testPackages="com.example.test1"
$: export testpackages="com.example.test2"
```

below are correct

```bash
$: mvn test -Dtest=MyTestRunner -DtestGroups="@SMOKE, @BLOCKER" 

$: export testGroups="@SMOKE, @BLOCKER"
$: export testPackages="com.example.test1, com.example.test2"
```

## Default Browser
If you test case is not annotated by any browser (test method and test class without any browser annotation),
the default browser will be set to `CHROME`, it means that the test case will be added `CHROME` to its supported browser running list.

## Change Default Browser
`Selenium-Extensions` provide a method to change default browser.

Similar custom filters, change default browser can be approached by setting `System Property`, `export`, and prgramming.

The keyword to change the default browser is: `defaultBrowser`.

- Change default browser by Maven Command

    ```bash
    mvn clean test -Dtest=MyTestRunner -DdefaultBrowser="FIREFOX"
    ```

- Change default browser by export system environment variable

    ```bash
    export defaultBrowser="FIREFOX"
    ```

- Change default browser programmatically

    ```java
    public class SmokeTestRunner {
            
        @Test
        public void run() {
            DefaultSettingUtils.setDefaultBrowser("FIREFOX");
            TestNGRunner.Run();
        }
    }
    ```
