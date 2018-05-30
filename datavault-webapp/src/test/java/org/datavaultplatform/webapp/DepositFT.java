package org.datavaultplatform.webapp;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.datavaultplatform.webapp.page.DepositPage;
import org.datavaultplatform.webapp.page.FirstRunPage;
import org.datavaultplatform.webapp.page.LoginPage;
import org.datavaultplatform.webapp.page.VaultsPage;

public class DepositFT extends BaseFT {
    @Test
    public void testDepositFirstRun() throws Exception {
        // This test currently assumes it's the first time this user has logged-in, they have no storage or vaults defined, and so we create a local filestore and a vault called "My Vault"
        // Any other circumstance, it'll fail, e.g. they've already created storage/vaults
        driver.get(host + "/datavault-webapp/");
        LoginPage page = new LoginPage(driver);
        page = page.typeUsername("user1")
                   .typePassword("password1");

        FirstRunPage firstRunPage = page.submitLoginExpectingFirstRun();
        firstRunPage = firstRunPage.clickDefineStorage()
                                   .clickAddLocalFilestore()
                                   .submitLocalFilestoreDialog()
                                   .submit();

        Assert.assertTrue(firstRunPage.isStorageDefined());
        VaultsPage vaultsPage = firstRunPage.clickAddVault()
                                 .addSampleVault()
                                 .clickMyVaults();

        DepositPage depositPage = vaultsPage.clickVault("My Vault")
                                            .clickDeposit()
                                            .depositFirstLocalFile();

        depositPage.waitForStatus("Complete");
        depositPage.logout("user1");
    }

    @Test(dependsOnMethods={"testDepositFirstRun"}, alwaysRun=true)
    public void testDeposit() throws Exception {
        // This test currently assumes it's not the first time this user has logged in and they have created a local filestore and a vault called "My Vault"
        // Any other circumstance, it'll fail, e.g. they've defined a vault with a different name, or added SFTP storage etc.
        driver.get(host + "/datavault-webapp/");
        LoginPage page = new LoginPage(driver);
        page = page.typeUsername("user1")
                   .typePassword("password1");

        VaultsPage vaultsPage = page.submitLogin();
        DepositPage depositPage = vaultsPage.clickVault("My Vault")
                                            .clickDeposit()
                                            .depositFirstLocalFile();

        depositPage.waitForStatus("Complete");
        depositPage.logout("user1");
    }
}
