package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.executor.MyExecutor;
import org.testng.xml.XmlSuite;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by ansonliao on 23/2/2017.
 */
public class ParallelThread {
    MyExecutor myExecutor;

    public ParallelThread() {
        myExecutor = new MyExecutor();
    }

    public void run(String packageName) {
        myExecutor.getAllTestMethods(packageName);
        XmlSuite xmlSuite = myExecutor.createXmlSuite();
        System.out.println(xmlSuite.toXml());
        myExecutor.testNGRun(asList(xmlSuite));
    }

    public void run(String packageName, List<String> testClassList) {
        myExecutor.getAllTestMethods(packageName, testClassList);
        XmlSuite xmlSuite = myExecutor.createXmlSuite();
        System.out.println(xmlSuite.toXml());
        myExecutor.testNGRun(asList(xmlSuite));
    }
}