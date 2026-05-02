package listeners;

import base.BaseTest;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import utils.ScreenshotUtil;

import java.io.File;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            Object instance = result.getInstance();
            if (instance instanceof BaseTest) {
                BaseTest base = (BaseTest) instance;
                if (base.getDriver() != null) {
                    String path = ScreenshotUtil.takeScreenshot(base.getDriver(), result.getName());
                    if (path != null) {
                        String fileName = new File(path).getName();
                        String relPath = "reports/screenshots/" + fileName;
                        Reporter.log("<a href='" + relPath + "'>Screenshot</a>");
                        Reporter.log("<br/><img src='" + relPath + "' height='300'/>\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // no-op
    }

    @Override
    public void onStart(ITestContext context) {
        // no-op
    }

    @Override
    public void onFinish(ITestContext context) {
        // no-op
    }
}
