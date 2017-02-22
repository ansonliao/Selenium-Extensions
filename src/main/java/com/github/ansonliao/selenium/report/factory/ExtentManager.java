package com.github.ansonliao.selenium.report.factory;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.net.URL;

/**
 * Created by ansonliao on 17/2/2017.
 */
public class ExtentManager {
    private static ExtentReports extentReports;
    private static String filePath =
            System.getProperty("user.dir") + File.separator + "target" + File.separator + "ExtentReports.html";

    public synchronized static ExtentReports getExtentReports() {
        if (extentReports == null) {
            extentReports = new ExtentReports();
            extentReports.attachReporter(getHtmlReporter());
            extentReports.setSystemInfo("Selenium Java Version", "1.8");
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
                System.getProperty("user.dir") + File.separator + "target" + File.separator + "extent.xml");
//        try {
//            FileUtils.copyURLToFile(inputUrl, dest);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        htmlReporter.loadXMLConfig(
//                System.getProperty("user.dir") + File.separator + "target" + File.separator + "extent.xml");
        htmlReporter.config().setChartVisibilityOnOpen(true);
        htmlReporter.config().setDocumentTitle("Maaii Selenium Web Test");
        htmlReporter.config().setReportName("Maaii Selenium Web Test");
        htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
        htmlReporter.config().setTheme(Theme.STANDARD);
        return htmlReporter;
    }

}
