package org.datavaultplatform.webapp.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FilestoresPage extends Page {
    private WebElement loginForm;

    public FilestoresPage(WebDriver driver) {
        super(driver);
        verifyBreadcrumb("Storage Options");
    }

    public FilestoresPage clickAddLocalFilestore() {
        driver.findElement(By.xpath("//a[@data-target='#add-filestoreLocal']")).click();
        return this;
    }

    public FilestoresPage submitLocalFilestoreDialog() {
        if (! driver.findElement(By.cssSelector("#add-filestoreLocal")).isDisplayed()) {
            throw new IllegalStateException("Local Filestore dialog not shown");
        }
        driver.findElement(By.cssSelector("#add-filestoreLocal .modal-footer .btn-primary")).click();
        return this;
    }

    public FirstRunPage submit() {
        driver.findElement(By.cssSelector(".container .btn-primary")).click();
        return new FirstRunPage(driver);
    }
}
