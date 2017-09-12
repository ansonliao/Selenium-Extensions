package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.executor.MyExecutor;
import org.apache.log4j.Logger;
import org.testng.xml.XmlSuite;

import java.util.List;

import static java.util.Arrays.asList;


public class ParallelThread {
    private static final Logger logger = Logger.getLogger(ParallelThread.class);
    MyExecutor myExecutor;

    public ParallelThread() {
        myExecutor = new MyExecutor();
    }

    public void run(String packageName) {
        myExecutor.getAllTestMethods(packageName);
        XmlSuite xmlSuite = myExecutor.createXmlSuite();
        System.out.println(xmlSuite.toXml());
        logger.info(xmlSuite.toXml());
        myExecutor.testNGRun(asList(xmlSuite));
    }

    public void run(String packageName, List<String> testClassList) {
        myExecutor.getAllTestMethods(packageName, testClassList);
        XmlSuite xmlSuite = myExecutor.createXmlSuite();
        System.out.println(xmlSuite.toXml());
        logger.info(xmlSuite.toXml());
        myExecutor.testNGRun(asList(xmlSuite));
    }
}