package org.datavaultplatform.webapp.page;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

public abstract class Page {
    protected final WebDriver driver;

    public Page(WebDriver driver) {
        this.driver = driver;
    }

    protected void verifyBreadcrumb(String expected) {
        try {
            WebElement welcome = driver.findElement(By.cssSelector(".breadcrumb .active"));
            if (! expected.equals(welcome.getText())) {
                throw new IllegalStateException(String.format("Not on %s: Breadcrumb was %s, expected %s", this.getClass().getSimpleName(), welcome.getText(), expected));
            }
        }
        catch(WebDriverException e) {
            throw new IllegalStateException(String.format("Not on %s: Breadcrumb not found", this.getClass().getSimpleName()), e);
        }
    }

    public LoginPage logout(String user) {
        driver.findElement(By.partialLinkText(user)).click();
        driver.findElement(By.linkText("Logout")).click();
        return new LoginPage(driver);
    }

    public void focusAndClick(WebElement element) {
        // Some elements (for unknown reasons) don't seem to properly be submitted when they are clicked
        // Clicking them twice works, but can sometimes cause problems if *both* clicks register
        // This instead gives the element focus before clicking, which also seems to fix the issue
        ((JavascriptExecutor)driver).executeScript("var input = arguments[0]; input.focus();", element);
        element.click();
    }
}

