package org.datavaultplatform.webapp;


import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import org.datavaultplatform.webapp.page.FirstRunPage;
import org.datavaultplatform.webapp.page.LoginPage;

@Ignore
public class LoginFT extends BaseFT {
    @Test
    public void testLogin() {
        driver.get(host + "/datavault-webapp/");
        LoginPage page = new LoginPage(driver);
        FirstRunPage page2 = page.typeUsername("user30")
                                 .typePassword("password30")
                                 .submitLoginExpectingFirstRun();

        WebElement title = driver.findElement(By.cssSelector("h1.title"));
        Assert.assertEquals(title.getText(), "Data Vault");
        page2.logout("user30");
    }

    @Test
    public void testLoginError() {
        driver.get(host + "/datavault-webapp/");
        LoginPage page = new LoginPage(driver);
        page = page.typeUsername("user1")
                   .typePassword("password2")
                   .submitLoginExpectingError();

        Assert.assertEquals(page.getError(), "Error:\nInvalid username or password!");
    }
}
