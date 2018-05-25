package org.datavaultplatform.webapp.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class VaultPage extends Page {
    private WebElement loginForm;
    private String vaultName;

    public VaultPage(WebDriver driver, String vaultName) {
        super(driver);
        verifyBreadcrumb("Vault: " + vaultName);
    }

    public VaultsPage clickMyVaults() {
        driver.findElement(By.linkText("My Vaults")).click();
        return new VaultsPage(driver);
    }

    public AddDepositPage clickDeposit() {
        driver.findElement(By.cssSelector(".btn-primary")).click();
        return new AddDepositPage(driver);
    }
}
