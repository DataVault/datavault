package org.datavaultplatform.webapp;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;

public class BaseFT {
    private static String screenShotFolderPath = "target/surefire-reports/errorScreenshots";
    protected String host;
    protected WebDriver driver;

    @BeforeClass
    public void beforeTest() throws Exception {
        host = System.getProperty("test.url", "http://localhost:58080");
        String remoteDriverUrl = System.getProperty("test.selenium.driver.url", null);
        String browser = System.getProperty("test.selenium.browser", "firefox");

        File screenshotDir = new File(screenShotFolderPath);
        if (screenshotDir.exists()) {
            screenshotDir.delete();
        }
        screenshotDir.mkdirs();

        if ("firefox".equals(browser)) {
            FirefoxOptions options = new FirefoxOptions();
            options.setHeadless(true);
            if (remoteDriverUrl != null) {
                driver = new RemoteWebDriver(new URL(remoteDriverUrl), options);
                ((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
            }
            else {
                driver = new FirefoxDriver(options);
            }
        }
        else if ("chrome".equals(browser)) {
            ChromeOptions options = new ChromeOptions();
            options.setHeadless(true);
            if (remoteDriverUrl != null) {
                driver = new RemoteWebDriver(new URL(remoteDriverUrl), options);
                ((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
            }
            else {
                driver = new ChromeDriver(options);
            }
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
    }

    protected void takeScreenshot(String prefix) throws IOException {
        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File(screenShotFolderPath, prefix + "_"
                + System.currentTimeMillis() +  ".png"));
    }

    @AfterMethod(dependsOnMethods={"takeScreenShotOnFailure"})
    public void logout() {
        try {
            driver.get(host + "/datavault-webapp/");
            driver.findElement(By.partialLinkText("user")).click();
            driver.findElement(By.linkText("Logout")).click();
        } catch (Exception e) {
            // Attempt to logout, but don't care if it doesn't work
        }
    }

    @AfterMethod
    public void takeScreenShotOnFailure(ITestResult testResult) throws IOException {
        if (testResult.getStatus() == ITestResult.FAILURE) {
            takeScreenshot(testResult.getName());
        }
    }

    @AfterClass
    public void afterTest() {
        driver.quit();
    }
}

