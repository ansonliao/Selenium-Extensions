package io.github.ansonliao.selenium.report;

import org.testng.annotations.Test;

public class AllureReportTest {

    @Test
    public void attachScreenshotMissingFileIsNoOp() {
        // A missing file must return early (warn log) and never touch Allure,
        // so it is safe to call outside an Allure test context.
        try {
            AllureReport.attachScreenshot("/no/such/file.png");
        } catch (Exception e) {
            throw new AssertionError(
                    "attachScreenshot should ignore a missing file instead of throwing", e);
        }
    }
}
