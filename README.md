# Selenium-Extensions

This is a extension for Selenium in Java to extend Selenium for make Selenium test case can be run in parallel, and make it easy to support multiple browsers.
Integrated beautiful and powerful HTML test report: [ExtentReports](http://extentreports.com/).

## Parallel
Introduce [TestNG](http://testng.org/doc/index.html) framework to support Selenium test case parallel run.
Parallel mode is by Test, the tag <test> of TestNG xml.

## Visual TestNG test suite XML generation
No need to provide TestNG test suite XMl file to start the test, TestNG test suite XML file will be generated programmatically.

## Multiple browsers in Runtime:
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

## Dynamic URL support for test method:
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