package utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.io.File;
import java.io.IOException;

public class ScreenshotUtil {
    public static String takeScreenshot(WebDriver driver, String testName) {
        if (driver == null) {
            return null;
        }
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String dirPath = System.getProperty("user.dir") + File.separator + "reports" + File.separator + "screenshots";
        String path = dirPath + File.separator + testName + ".png";
        File destination = new File(path);
        try {
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            FileUtils.copyFile(src, destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }
}