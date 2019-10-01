package org.datavaultplatform.broker.services;

import static org.hamcrest.Matchers.is;

import org.datavaultplatform.common.model.RoleAssignment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.util.Date;

import org.datavaultplatform.common.model.Vault;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
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

    @Autowired
    private RolesAndPermissionsService rolesAndPermissionsService;

    @Test
    public void checkVaultCount() {
        RoleAssignment isAdminRoleAssignment = new RoleAssignment();
        isAdminRoleAssignment.setRole(rolesAndPermissionsService.getIsAdmin());
        isAdminRoleAssignment.setUserId("admin1");
        rolesAndPermissionsService.createRoleAssignment(isAdminRoleAssignment);

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