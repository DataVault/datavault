package org.datavaultplatform.broker.services;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.Vault;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
public class VaultsServiceIT extends BaseDatabaseTest {
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
        vault.setContact("vault contact");
        vault.setDescription("Vault for test");
        vault.setGrantEndDate(new Date());
        vault.setReviewDate(new Date());
        vault.setSnapshot("This is a dummy snapshot");
        vaultsService.addVault(vault);
        
        int newVaultCount = prevVaultCount + 1;
        assertThat(vaultsService.count("admin1")).isEqualTo(newVaultCount);
    }
}