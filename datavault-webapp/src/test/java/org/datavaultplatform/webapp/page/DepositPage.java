package org.datavaultplatform.webapp.page;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DepositPage extends Page {
    private String depositName;

    public DepositPage(WebDriver driver, String depositName) {
        super(driver);
        this.depositName = depositName;
        verifyBreadcrumb("Deposit: " + depositName);
    }

    public DepositPage waitForStatus(String status) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.textToBe(By.cssSelector(".job-status"), status));
        return this;
    }

    public DepositPage clickRetrievalsTab() throws InterruptedException {
        WebElement link = driver.findElement(By.xpath("//a[@href='#retrievals']"));
        focusAndClick(link);
        // Absolutely no amount of waiting or clicking seems to make the above link works - the tab doesn't get shown
        return this;
    }

    public DepositPage waitForRetrievalStatus(String status) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // See the note in clickRetrievalsTab - the table is not visible so getText() returns nothing
        wait.until(ExpectedConditions.attributeToBe(By.cssSelector("#retrievals tbody tr td:nth-child(2)"), "innerHTML", status));
        return this;
    }

    public RetrievePage clickRetrieveData() throws InterruptedException {
        WebElement submit = driver.findElement(By.cssSelector(".btn-primary"));
        Thread.sleep(1000);
        submit.click();
        return new RetrievePage(driver, depositName);
    }
}

