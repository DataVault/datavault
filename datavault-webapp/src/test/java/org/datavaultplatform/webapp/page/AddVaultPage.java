package org.datavaultplatform.webapp.page;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AddVaultPage extends Page {

    public AddVaultPage(WebDriver driver) {
        super(driver);
        verifyBreadcrumb("Create new vault");
    }

    public AddVaultPage typeVaultName(String name) {
        WebElement nameTestBox = driver.findElement(By.name("name"));
        nameTestBox.sendKeys(name);
        return this;
    }

    public AddVaultPage typeDescription(String description) {
        WebElement descriptionTestBox = driver.findElement(By.name("description"));
        descriptionTestBox.sendKeys(description);
        return this;
    }

    public AddVaultPage selectDataset(String dataset) {
        driver.findElement(By.xpath("//button[@data-id='datasetID']")).click();
        // For some unknown reason, this one doesn't show up unless it's clicked twice
        driver.findElement(By.xpath("//button[@data-id='datasetID']")).click();
        WebElement menu = driver.findElement(By.cssSelector(".dataset-select div.dropdown-menu"));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        wait.until(ExpectedConditions.visibilityOf(menu));
        menu.findElement(By.linkText(dataset)).click();
        return this;
    }

    public AddVaultPage selectRetentionPolicy(String policy) {
        driver.findElement(By.xpath("//button[@data-id='policyID']")).click();
        WebElement menu = driver.findElement(By.cssSelector(".dataset-select div.dropdown-menu"));
        menu = driver.findElement(By.cssSelector(".retentionPolicy-select div.dropdown-menu"));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        wait.until(ExpectedConditions.visibilityOf(menu));
        menu.findElement(By.partialLinkText(policy)).click();
        return this;
    }

    public AddVaultPage selectGroup(String group) {
        driver.findElement(By.xpath("//button[@data-id='groupID']")).click();
        WebElement menu = driver.findElement(By.cssSelector(".dataset-select div.dropdown-menu"));
        menu = driver.findElement(By.cssSelector(".group-select div.dropdown-menu"));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        wait.until(ExpectedConditions.visibilityOf(menu));
        menu.findElement(By.linkText(group)).click();
        return this;
    }

    public VaultPage submit(String vaultName) {
        driver.findElement(By.cssSelector(".btn-primary")).click();
        return new VaultPage(driver, vaultName);
    }

    public VaultPage addSampleVault() {
        String vaultName = "My Vault";
        typeVaultName(vaultName);
        typeDescription("A vault of useful things");
        selectDataset("Sample dataset 1");
        selectRetentionPolicy("Default University Policy");
        selectGroup("Humanities and Social Science");
        return submit(vaultName);
    }
}
