package org.datavaultplatform.webapp.page;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class VaultPage extends Page {
    private String vaultName;

    public VaultPage(WebDriver driver, String vaultName) {
        super(driver);
        verifyBreadcrumb("Vault: " + vaultName);
    }

    public VaultsPage clickMyVaults() {
        driver.findElement(By.linkText("My Vaults")).click();
        return new VaultsPage(driver);
    }

    public AddDepositPage clickDepositData() {
        driver.findElement(By.cssSelector(".btn-primary")).click();
        return new AddDepositPage(driver);
    }

    public DepositPage clickLastSuccessfulDeposit() {
        List<WebElement> successes = driver.findElements(By.cssSelector(".text-success"));
        WebElement lastSuccess = successes.get(successes.size() - 1);
        WebElement depositLink = lastSuccess.findElement(By.xpath("./ancestor::tr[1]/td/a"));
        String depositName = depositLink.getText();
        depositLink.click();
        return new DepositPage(driver, depositName);
    }
}
