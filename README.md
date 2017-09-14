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
    <version>1.0.0</version>
</dependency>
```

## Parallel
Introduce [TestNG](http://testng.org/doc/index.html) framework to support Selenium test case parallel run. 
Parallel mode is by Test, the tag `<test>` of TestNG test suite xml.

## Visual TestNG test suite XML generation
No need to provide TestNG test suite XMl file to start the test, TestNG test suite XML file will be generated programmatically.

## Multiple browsers support
Browser support:

- **Chrome**: Mac, Linux/Unix, Windows
- **FireFox**: Mac, Linux/Unix, Windows
- **Edge**: Windows
- **InternetExplorer**: Windows
- **Safari**: In planning...

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

## Run Test
To run the simple test, please run the command below.
```bash
mvn clean test -Dtest=MyExecutorRunner
```

