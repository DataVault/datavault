package org.datavaultplatform.webapp.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class VaultsPage extends Page {

    public VaultsPage(WebDriver driver) {
        super(driver);
        verifyBreadcrumb("My Vaults");
    }

    public VaultPage clickVault(String vaultName) {
        driver.findElement(By.linkText(vaultName)).click();
        return new VaultPage(driver, vaultName);
    }
}

