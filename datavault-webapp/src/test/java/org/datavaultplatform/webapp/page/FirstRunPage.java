package org.datavaultplatform.webapp.page;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FirstRunPage extends Page {
    private WebElement loginForm;

    public FirstRunPage(WebDriver driver) {
        super(driver);
        verifyWelcome();
    }

    private void verifyWelcome() {
        WebElement welcome = driver.findElement(By.cssSelector(".jumbotron p"));
        if (! "Welcome to the Data Vault!".equals(welcome.getText())) {
            throw new IllegalStateException("Not on first-run page. Text was: " + welcome.getText());
        }
    }

    public boolean isDatasetDefined() {
        List<WebElement> success = driver.findElements(By.cssSelector(".jumbotron .text-success"));
        return success.size() >= 1;
    }

    public boolean isStorageDefined() {
        List<WebElement> success = driver.findElements(By.cssSelector(".jumbotron .text-success"));
        return success.size() >= 2;
    }

    public boolean isVaultDefined() {
        List<WebElement> success = driver.findElements(By.cssSelector(".jumbotron .text-success"));
        return success.size() >= 3;
    }

    public FilestoresPage clickDefineStorage() {
        driver.findElement(By.linkText("define your storage options")).click();
        return new FilestoresPage(driver);
    }

    public AddVaultPage clickAddVault() {
        driver.findElement(By.linkText("here")).click();
        return new AddVaultPage(driver);
    }
}

