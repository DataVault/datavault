package org.datavaultplatform.webapp;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;

public class DepositFT {
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

    private void login() {
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
    }

    private boolean isStorageDefined() {
        if ( onVaultsPage() ) {
            return true;
        }
        List<WebElement> success = driver.findElements(By.cssSelector(".jumbotron .text-success"));
        return success.size() >= 2;
    }

    private void defineStorage() {
        driver.findElement(By.linkText("define your storage options")).click();
        driver.findElement(By.xpath("//a[@data-target='#add-filestoreLocal']")).click();
        driver.findElement(By.cssSelector("#add-filestoreLocal .modal-footer .btn-primary")).click();
        driver.findElement(By.cssSelector(".container .btn-primary")).click();
    }

    private boolean onVaultsPage() {
        List<WebElement> breadcrumbs = driver.findElements(By.cssSelector("ol.breadcrumb"));
        return ! breadcrumbs.isEmpty() && breadcrumbs.get(0).getText().equals("My Vaults");
    }

    private boolean isVaultDefined() {
        if ( onVaultsPage() ) {
            List<WebElement> vaults = driver.findElements(By.linkText("My Vault"));
            return !vaults.isEmpty();
        }
        return false;
    }

    private void addVault() {
        driver.findElement(By.linkText("here")).click();

        WebElement nameTestBox = driver.findElement(By.name("name"));
        nameTestBox.sendKeys("My Vault");

        WebElement descriptionTestBox = driver.findElement(By.name("description"));
        descriptionTestBox.sendKeys("A vault of useful things");

        WebDriverWait wait = new WebDriverWait(driver, 1);

        driver.findElement(By.xpath("//button[@data-id='datasetID']")).click();
        // For some unknown reason, this one doesn't show up unless it's clicked twice
        driver.findElement(By.xpath("//button[@data-id='datasetID']")).click();
        WebElement menu = driver.findElement(By.cssSelector(".dataset-select div.dropdown-menu"));
        wait.until(ExpectedConditions.visibilityOf(menu));
        menu.findElement(By.linkText("Sample dataset 1")).click();

        driver.findElement(By.xpath("//button[@data-id='policyID']")).click();
        menu = driver.findElement(By.cssSelector(".retentionPolicy-select div.dropdown-menu"));
        wait.until(ExpectedConditions.visibilityOf(menu));
        menu.findElement(By.partialLinkText("Default University Policy")).click();

        driver.findElement(By.xpath("//button[@data-id='groupID']")).click();
        menu = driver.findElement(By.cssSelector(".group-select div.dropdown-menu"));
        wait.until(ExpectedConditions.visibilityOf(menu));
        menu.findElement(By.linkText("Humanities and Social Science")).click();

        driver.findElement(By.cssSelector(".btn-primary")).click();

        driver.findElement(By.linkText("My Vaults")).click();
    }

    private void addDeposit() throws Exception {
        driver.findElement(By.linkText("My Vault")).click();

        driver.findElement(By.cssSelector(".btn-primary")).click();

        WebElement noteTestBox = driver.findElement(By.name("note"));
        noteTestBox.sendKeys("Deposit 1");

        driver.findElement(By.xpath("//button[@data-target='#add-from-storage']")).click();
        driver.findElement(By.xpath("//button[@data-target='#add-from-storage']")).click();

        WebElement tree = driver.findElement(By.cssSelector("#add-from-storage .modal-body .ui-fancytree li"));
        tree.findElement(By.cssSelector(".fancytree-expander")).click();
        WebElement tree2 = tree.findElement(By.cssSelector(".ui-fancytree li"));
        tree2.findElement(By.cssSelector(".fancytree-expander")).click();
        WebElement tree3 = tree2.findElement(By.cssSelector(".ui-fancytree li"));
        tree3.findElement(By.cssSelector(".fancytree-title")).click();

        driver.findElement(By.cssSelector("#add-from-storage .modal-footer .btn-primary")).click();

        Thread.sleep(1000);
        driver.findElement(By.cssSelector("#deposit-submit-btn")).click();
    }

    private void checkDepositStatus() {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.textToBe(By.cssSelector(".job-status"), "Complete"));
    }

    private void logout() {
        driver.findElement(By.partialLinkText("user1")).click();
        driver.findElement(By.linkText("Logout")).click();
    }

    @Test
    public void testDeposit() throws Exception {
        login();
        if (! isStorageDefined() ) {
            defineStorage();
        }
        Assert.assertTrue(isStorageDefined());
        if (! isVaultDefined() ) {
            addVault();
        }
        Assert.assertTrue(isVaultDefined());
        addDeposit();
        checkDepositStatus();
        logout();
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
