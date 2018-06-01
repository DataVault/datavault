package org.datavaultplatform.webapp.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

public abstract class Page {
    protected WebDriver driver;
    private WebElement loginForm;

    public Page(WebDriver driver) {
        this.driver = driver;
    }

    protected void verifyBreadcrumb(String expected) {
        try {
            WebElement welcome = driver.findElement(By.cssSelector(".breadcrumb .active"));
            if (! expected.equals(welcome.getText())) {
                throw new IllegalStateException(String.format("Not on %s: Breadcrumb was %s, expected %s", this.getClass(), welcome.getText(), expected));
            }
        }
        catch(WebDriverException e) {
            throw new IllegalStateException(String.format("Not on %s: Breadcrumb not found", this.getClass()), e);
        }
    }

    public LoginPage logout(String user) {
        driver.findElement(By.partialLinkText(user)).click();
        driver.findElement(By.linkText("Logout")).click();
        return new LoginPage(driver);
    }
}

