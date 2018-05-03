package org.datavaultplatform.webapp;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;

public class HeadlessChromiumSeleniumTest {
    private static String screenShotFolderPath = "target/surefire-reports/errorScreenshots";
    private WebDriver driver;
    
    @BeforeTest
    public void beforeTest() {
        File screenshotDir = new File(screenShotFolderPath);
        if (screenshotDir.exists()) {
            screenshotDir.delete();
        }
        screenshotDir.mkdirs();
        
        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(true);
        driver = new FirefoxDriver(options);
    }
    
    @Test
    public void testLoginErrorPage() {
        driver.get("http://localhost:58080/datavault-webapp/");
        
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
        
//        WebElement title = driver.findElement(By.cssSelector("h1.title"));
//        Assert.assertEquals(title.getText(), "Data Vault");
        
        WebElement errorBox = driver.findElement(By.cssSelector("div.alert-danger"));
        
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
