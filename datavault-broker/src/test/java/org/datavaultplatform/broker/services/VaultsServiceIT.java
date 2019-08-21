package org.datavaultplatform.broker.services;

import static org.hamcrest.Matchers.is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.util.Date;
import java.util.List;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.VaultDAO;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
//@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("it")
@ContextConfiguration(locations = {
        "file:src/test/resources/datavault-broker-root.xml" 
        })
@TransactionConfiguration(defaultRollback=true,transactionManager="transactionManager")  
public class VaultsServiceIT {
    @Autowired
    private VaultsService vaultsService;
    
    @Test
    public void checkVaultCount() {
        int prevVaultCount = vaultsService.count("admin1");
        
        Vault vault = new Vault("Vault Test");
        vault.setDescription("Vault for test");
        vault.setGrantEndDate(new Date());
        vault.setReviewDate(new Date());
        vaultsService.addVault(vault);
        
        int newVaultCount = prevVaultCount + 1;
        Assert.assertThat(vaultsService.count("admin1"), is(newVaultCount));
    }
}