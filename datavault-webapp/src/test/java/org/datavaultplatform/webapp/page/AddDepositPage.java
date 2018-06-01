package org.datavaultplatform.webapp.page;

import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AddDepositPage extends Page {

    public AddDepositPage(WebDriver driver) {
        super(driver);
        verifyBreadcrumb("Create new deposit");
    }

    public AddDepositPage typeDepositNote(String note) {
        WebElement noteTestBox = driver.findElement(By.name("note"));
        noteTestBox.sendKeys(note);
        return this;
    }

    public AddDepositPage clickDataStorage() throws InterruptedException {
        focusAndClick(driver.findElement(By.xpath("//button[@data-target='#add-from-storage']")));
        Thread.sleep(1000);
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

    public AddDepositPage uploadFile(File file) throws InterruptedException {
        // Selenium cannot interract with the File Upload dialog
        // Instead, we have to type into the invisible <input type="file">
        // This should work on all types of Driver, but has been seen to cause crashes with Firefox

        WebElement input = driver.findElement(By.xpath("//input[@type='file']"));
        // make it visible
        ((JavascriptExecutor)driver).executeScript("var input = arguments[0]; input.style = '';", input);
        input.sendKeys(file.getAbsolutePath());
        Thread.sleep(1000);
        return this;
    }

    public AddDepositPage submitDataStorageDialog() throws InterruptedException {
        driver.findElement(By.cssSelector("#add-from-storage .modal-footer .btn-primary")).click();
        Thread.sleep(1000);
        return this;
    }

    public DepositPage submit(String depositName) throws InterruptedException {
        focusAndClick(driver.findElement(By.cssSelector("#deposit-submit-btn")));
        Thread.sleep(1000);
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

    public DepositPage depositUploadedFile() throws InterruptedException {
        String depositName = "Deposit";
        String resourcesDir = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources";
        String testFile = "banjo.jpg";
        File file = new File(resourcesDir, testFile);
        if(!file.exists()) {
            throw new IllegalStateException(String.format("Test file %s does not exist", file));
        }
        typeDepositNote(depositName);
        uploadFile(file);
        return submit(depositName);
    }
}


