package org.datavaultplatform.webapp.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class VaultsPage extends Page {
    private WebElement loginForm;

    public VaultsPage(WebDriver driver) {
        super(driver);
        verifyBreadcrumb("My Vaults");
    }

    public VaultPage clickVault(String vaultName) {
        driver.findElement(By.linkText(vaultName)).click();
        return new VaultPage(driver, vaultName);
    }
}

