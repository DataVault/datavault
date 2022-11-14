package org.datavaultplatform.webapp.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class FilestoresPage extends Page {

    public FilestoresPage(WebDriver driver) {
        super(driver);
        verifyBreadcrumb("Storage Options");
    }

    public FilestoresPage clickAddLocalFilestore() throws InterruptedException {
        focusAndClick(driver.findElement(By.xpath("//a[@data-target='#add-filestoreLocal']")));
        Thread.sleep(1000);
        return this;
    }

    public FilestoresPage submitLocalFilestoreDialog() throws InterruptedException {
        if (! driver.findElement(By.cssSelector("#add-filestoreLocal")).isDisplayed()) {
            throw new IllegalStateException("Local Filestore dialog not shown");
        }
        driver.findElement(By.cssSelector("#add-filestoreLocal .modal-footer .btn-primary")).click();
        Thread.sleep(1000);
        return this;
    }

    public FirstRunPage submit() {
        driver.findElement(By.cssSelector(".container .btn-primary")).click();
        return new FirstRunPage(driver);
    }
}
