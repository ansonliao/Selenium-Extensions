package com.github.ansonliao.selenium.report.factory;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.net.URL;

public class ExtentManager {
    private static final String PROJECT_ROOT_DIR = System.getProperty("user.dir");
    private static final String FILE_SEPARATOR = File.separator;
    private static ExtentReports extentReports;
    private static String filePath = PROJECT_ROOT_DIR
            .concat(FILE_SEPARATOR)
            .concat("target")
            .concat(FILE_SEPARATOR)
            .concat("ExtentReports.html");

    public synchronized static ExtentReports getExtentReports() {
        if (extentReports == null) {
            extentReports = new ExtentReports();
            extentReports.attachReporter(getHtmlReporter());
            extentReports.setSystemInfo("Selenium Java Version", System.getProperty("java.version"));
            extentReports.setSystemInfo("Environment", "Prod");
            extentReports.setSystemInfo("Selenium Java Client", "3");
        }

        return extentReports;
    }

    private static ExtentHtmlReporter getHtmlReporter() {
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(filePath);
        URL inputUrl = null;
        try {
            inputUrl = Thread.currentThread().getContextClassLoader().getResource("extent.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        File dest = new File(
                PROJECT_ROOT_DIR
                        .concat(FILE_SEPARATOR)
                        .concat("target")
                        .concat(FILE_SEPARATOR)
                        .concat("extent.xml"));

        htmlReporter.config().setChartVisibilityOnOpen(true);
        htmlReporter.config().setDocumentTitle("Selenium Web UI Test");
        htmlReporter.config().setReportName("Selenium Web UI Test");
        htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
        htmlReporter.config().setTheme(Theme.STANDARD);
        return htmlReporter;
    }
}
