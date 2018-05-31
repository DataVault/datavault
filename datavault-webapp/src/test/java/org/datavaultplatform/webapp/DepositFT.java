package org.datavaultplatform.webapp;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.datavaultplatform.webapp.page.DepositPage;
import org.datavaultplatform.webapp.page.FirstRunPage;
import org.datavaultplatform.webapp.page.LoginPage;
import org.datavaultplatform.webapp.page.VaultsPage;

public class DepositFT extends BaseFT {
    @Test
    public void testDeposit() throws Exception {
        // This test currently assumes one of two things:
        // * It's the first time this user has logged-in, they have no storage or vaults defined, and so we create a local filestore and a vault called "My Vault"
        // * It's not the first time they've logged in and both of those things have happened already
        // Any other circumstance, it'll fail, e.g. they've defined a vault with a different name, or added SFTP storage etc.
        driver.get(host + "/datavault-webapp/");
        LoginPage page = new LoginPage(driver);
        page = page.typeUsername("user1")
                   .typePassword("password1");

        VaultsPage vaultsPage = null;
        try {
            vaultsPage = page.submitLogin();
        }
        catch(IllegalStateException e) {
            FirstRunPage firstRunPage = new FirstRunPage(driver);
            if (! firstRunPage.isStorageDefined() ) {
                firstRunPage = firstRunPage.clickDefineStorage()
                                           .clickAddLocalFilestore()
                                           .submitLocalFilestoreDialog()
                                           .submit();
            }
            Assert.assertTrue(firstRunPage.isStorageDefined());
            if ( !firstRunPage.isVaultDefined() ) {
                vaultsPage = firstRunPage.clickAddVault()
                                         .addSampleVault()
                                         .clickMyVaults();
            }
        }

        DepositPage depositPage = vaultsPage.clickVault("My Vault")
                                            .clickDeposit()
                                            .depositFirstLocalFile();

        depositPage.waitForStatus("Complete");
        depositPage.logout("user1");
    }
}
