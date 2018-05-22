package org.datavaultplatform.webapp;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;

public class LoginFT {
    private static String screenShotFolderPath = "target/surefire-reports/errorScreenshots";
    private String host;
    private WebDriver driver;

    @BeforeTest
    public void beforeTest() throws Exception {
        host = System.getProperty("test.url", "http://localhost:58080");
        String remoteDriverUrl = System.getProperty("test.selenium.driver.url", null);

        File screenshotDir = new File(screenShotFolderPath);
        if (screenshotDir.exists()) {
            screenshotDir.delete();
        }
        screenshotDir.mkdirs();

        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(true);
        if (remoteDriverUrl != null) {
            driver = new RemoteWebDriver(new URL(remoteDriverUrl), options);
        }
        else {
            driver = new FirefoxDriver(options);
        }
    }

    @Test
    public void testLogin() {
        driver.get(host + "/datavault-webapp/");

        // Check login screen is loaded
        WebElement loginForm = driver.findElement(By.cssSelector("form.form-signin"));
        Assert.assertTrue(loginForm.isDisplayed());

        // Enter Name
        WebElement usernameTestBox = driver.findElement(By.name("username"));
        usernameTestBox.sendKeys("user1");

        // Enter Password
        WebElement passwordTestBox = driver.findElement(By.name("password"));
        passwordTestBox.sendKeys("password1");

        // Click Sign In button
        driver.findElement(By.cssSelector(".btn-primary")).click();

        WebElement title = driver.findElement(By.cssSelector("h1.title"));
        Assert.assertEquals(title.getText(), "Data Vault");

        driver.findElement(By.partialLinkText("user1")).click();
        driver.findElement(By.linkText("Logout")).click();
    }

    @Test
    public void testLoginError() {
        driver.get(host + "/datavault-webapp/");

        // Check login screen is loaded
        WebElement loginForm = driver.findElement(By.cssSelector("form.form-signin"));
        Assert.assertTrue(loginForm.isDisplayed());

        // Enter Name
        WebElement usernameTestBox = driver.findElement(By.name("username"));
        usernameTestBox.sendKeys("user1");

        // Enter Password
        WebElement passwordTestBox = driver.findElement(By.name("password"));
        passwordTestBox.sendKeys("password2");

        // Click Sign In button
        driver.findElement(By.cssSelector(".btn-primary")).click();

        WebElement errorBox = driver.findElement(By.cssSelector("div.alert-danger"));
        Assert.assertEquals(errorBox.getText(), "Error:\nInvalid username or password!");

    }

    @AfterMethod
    public void takeScreenShotOnFailure(ITestResult testResult) throws IOException {
        if (testResult.getStatus() == ITestResult.FAILURE) {
            File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File(screenShotFolderPath, testResult.getName() + "_"
                    + System.currentTimeMillis() +  ".png"));
        }
    }

    @AfterTest
    public void afterTest() {
        driver.quit();
    }
}
