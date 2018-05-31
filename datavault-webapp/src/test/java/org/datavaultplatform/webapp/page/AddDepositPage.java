package org.datavaultplatform.webapp.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AddDepositPage extends Page {
    private WebElement loginForm;

    public AddDepositPage(WebDriver driver) {
        super(driver);
        verifyBreadcrumb("Create new deposit");
    }

    public AddDepositPage typeDepositNote(String note) {
        WebElement noteTestBox = driver.findElement(By.name("note"));
        noteTestBox.sendKeys(note);
        return this;
    }

    public AddDepositPage clickDataStorage() {
        // For reasons I don't understand, this has to be clicked twice before Selenium registers the dialog opens
        driver.findElement(By.xpath("//button[@data-target='#add-from-storage']")).click();
        driver.findElement(By.xpath("//button[@data-target='#add-from-storage']")).click();
        return this;
    }

    public AddDepositPage selectFirstFileFromStorageTreeDialog() {
        // This should really be made a bit more general
        // Only works if data exists in local filestore
        WebElement tree = driver.findElement(By.cssSelector("#add-from-storage .modal-body .ui-fancytree li"));
        tree.findElement(By.cssSelector(".fancytree-expander")).click();
        WebElement tree2 = tree.findElement(By.cssSelector(".ui-fancytree li"));
        tree2.findElement(By.cssSelector(".fancytree-expander")).click();
        WebElement tree3 = tree2.findElement(By.cssSelector(".ui-fancytree li"));
        tree3.findElement(By.cssSelector(".fancytree-title")).click();
        return this;
    }

    public AddDepositPage submitDataStorageDialog() throws InterruptedException {
        driver.findElement(By.cssSelector("#add-from-storage .modal-footer .btn-primary")).click();
        Thread.sleep(1000);
        return this;
    }

    public DepositPage submit(String depositName) {
        driver.findElement(By.cssSelector("#deposit-submit-btn")).click();
        return new DepositPage(driver, depositName);
    }

    public DepositPage depositFirstLocalFile() throws InterruptedException {
        String depositName = "Deposit";
        typeDepositNote(depositName);
        clickDataStorage();
        selectFirstFileFromStorageTreeDialog();
        submitDataStorageDialog();
        return submit(depositName);
    }
}


