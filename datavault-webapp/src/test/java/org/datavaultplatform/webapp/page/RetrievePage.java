package org.datavaultplatform.webapp.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class RetrievePage extends Page {
    private String depositName;

    public RetrievePage(WebDriver driver, String depositName) {
        super(driver);
        this.depositName = depositName;
        verifyBreadcrumb("Retrieve data");
    }

    public RetrievePage typeRetrieveNote(String note) {
        WebElement noteTestBox = driver.findElement(By.name("note"));
        noteTestBox.sendKeys(note);
        return this;
    }

    public RetrievePage selectTargetDirectory() throws InterruptedException {
        WebElement tree = driver.findElement(By.cssSelector(".ui-fancytree li"));
        tree.findElement(By.cssSelector(".fancytree-title")).click();
        tree.findElement(By.cssSelector(".fancytree-title")).click();
        Thread.sleep(1000);
        return this;
    }

    public DepositPage submit() throws InterruptedException {
        focusAndClick(driver.findElement(By.cssSelector(".btn-primary")));
        return new DepositPage(driver, depositName);
    }
}
