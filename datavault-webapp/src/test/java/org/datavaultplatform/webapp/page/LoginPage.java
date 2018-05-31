package org.datavaultplatform.webapp.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage extends Page {
    private WebElement loginForm;

    public LoginPage(WebDriver driver) {
        super(driver);
        verifyLoginForm();
    }

    private void verifyLoginForm() {
        // This should really test something simpler, e.g. heading or title, but neither of those are present
        // Check login screen is loaded
        loginForm = driver.findElement(By.cssSelector("form.form-signin"));
        if(! loginForm.isDisplayed()) {
            throw new IllegalStateException("Not on login page");
        }
    }

    public String getError() {
        WebElement errorBox = driver.findElement(By.cssSelector("div.alert-danger"));
        return errorBox.getText();
    }

    public LoginPage typeUsername(String username) {
        WebElement usernameTestBox = driver.findElement(By.name("username"));
        usernameTestBox.sendKeys(username);
        return this;
    }

    public LoginPage typePassword(String password) {
        WebElement passwordTestBox = driver.findElement(By.name("password"));
        passwordTestBox.sendKeys(password);
        return this;
    }

    private void submit() {
        // Click Sign In button
        driver.findElement(By.cssSelector(".btn-primary")).click();
    }

    public VaultsPage submitLogin() {
        submit();
        return new VaultsPage(driver);
    }

    public FirstRunPage submitLoginExpectingFirstRun() {
        submit();
        return new FirstRunPage(driver);
    }

    public LoginPage submitLoginExpectingError() {
        submit();
        return new LoginPage(driver);
    }
}
