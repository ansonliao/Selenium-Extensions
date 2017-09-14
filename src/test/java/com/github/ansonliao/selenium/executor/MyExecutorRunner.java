package com.github.ansonliao.selenium.executor;

import com.github.ansonliao.selenium.parallel.ParallelThread;
import org.testng.annotations.Test;


public class MyExecutorRunner {

    @Test
    public void testRunner() {
        String packageName = "com.github.ansonliao.selenium.executor";
        ParallelThread parallelThread = new ParallelThread();
        parallelThread.run(packageName);
    }
}
