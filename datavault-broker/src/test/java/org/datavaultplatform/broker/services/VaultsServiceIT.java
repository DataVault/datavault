package org.datavaultplatform.broker.services;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.config.MockRabbitConfig;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.Vault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@TestPropertySource(properties = {
    "broker.scheduled.enabled=false",
    "broker.rabbit.enabled=false"
})
@Import(MockRabbitConfig.class)
@Slf4j
public class VaultsServiceIT extends BaseReuseDatabaseTest {
    @Autowired
    private VaultsService vaultsService;

    @Autowired
    private RolesAndPermissionsService rolesAndPermissionsService;

    @MockBean
    AdminDepositService adminDepositService;
    
    @Autowired
    RetentionPoliciesService retentionPoliciesService;
    
    RetentionPolicy retentionPolicy;
    
    @Autowired
    Clock clock;

    @BeforeEach
    void setup() {
        retentionPolicy = new RetentionPolicy();
        retentionPolicy.setEngine("engine!");
        retentionPolicy.setName("RETENTION POLICY 111");
        retentionPolicy.setDescription("RETENTION POLICY 111 DEC");
        retentionPolicy.setMinRetentionPeriod(1);
        retentionPolicy.setExtendUponRetrieval(false);
        retentionPoliciesService.addRetentionPolicy(retentionPolicy);
    }

    @Test
    public void checkVaultCount() {
        RoleAssignment isAdminRoleAssignment = new RoleAssignment();
        isAdminRoleAssignment.setRole(rolesAndPermissionsService.getIsAdmin());
        isAdminRoleAssignment.setUserId("admin1");
        rolesAndPermissionsService.createRoleAssignment(isAdminRoleAssignment);

        int prevVaultCount = vaultsService.count("admin1");
        
        Vault vault = new Vault("Vault Test", clock);
        vault.setContact("vault contact");
        vault.setDescription("Vault for test");
        vault.setGrantEndDate(LocalDate.now());
        vault.setReviewDate(LocalDate.now(clock));
        vault.setSnapshot("This is a dummy snapshot");
        vault.setRetentionPolicy(retentionPolicy);
        vaultsService.addVault(vault);
        
        int newVaultCount = prevVaultCount + 1;
        assertThat(vaultsService.count("admin1")).isEqualTo(newVaultCount);
    }

}