package com.github.ansonliao.selenium.internal.interrupt;

import com.aventstack.extentreports.Status;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;

public class Sleep {

    public static synchronized void bySecond(int time) {
        byMillisecond(time * 1000);
    }

    public static synchronized void bySecondWithNoLog(int time) {
        byMillisecondWithNoLog(time * 1000);
    }

    public static synchronized void byMillisecond(int time) {
        try {
            Thread.sleep(time);
            ExtentTestManager.getExtentTest().log(
                    Status.INFO,
                    "Action: Sleep, Duration: "
                            .concat(String.valueOf(time))
                            .concat(" millisecond"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void byMillisecondWithNoLog(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
