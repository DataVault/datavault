package org.datavaultplatform.webapp.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DepositPage extends Page {
    private WebElement loginForm;
    private String depositName;

    public DepositPage(WebDriver driver, String depositName) {
        super(driver);
        verifyBreadcrumb("Deposit: " + depositName);
    }

    public DepositPage waitForStatus(String status) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.textToBe(By.cssSelector(".job-status"), status));
        return this;
    }
}

