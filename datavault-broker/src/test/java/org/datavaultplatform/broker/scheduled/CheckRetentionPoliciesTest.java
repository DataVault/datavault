package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.RetentionPoliciesService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.Vault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckRetentionPoliciesTest {

    public static final String VAULT_1_ID = "vault1-id";
    public static final String VAULT_2_ID = "vault2-id";
    public static final String VAULT_3_ID = "vault3-id";
    public static final String VAULT_1_NAME = "vault1-name";
    public static final String VAULT_2_NAME = "vault2-name";
    public static final String VAULT_3_NAME = "vault3-name";

    @Mock
    VaultsService mVaultsService;

    Vault vault1;
    Vault vault2;
    Vault vault3;

    Clock clock;

    CheckRetentionPolicies checkRetentionPolicies;

    RetentionPolicy retPol1;
    
    @Captor
    ArgumentCaptor<String> argVaultId1;

    @Captor
    ArgumentCaptor<String> argVaultId2;

    @BeforeEach
    void setup() {
        clock = Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneOffset.UTC);
        
        retPol1 = new RetentionPolicy("vault1-retPol"){
            @Override
            public Integer getID() {
                return 111;
            }
        };
        vault1 = createVault(VAULT_1_ID);
        vault1.setName(VAULT_1_NAME);
        vault1.setRetentionPolicy(retPol1);

        vault2 = createVault(VAULT_2_ID);
        vault2.setName(VAULT_2_NAME);

        vault3 = createVault(VAULT_3_ID);
        vault3.setName(VAULT_3_NAME);
        
        lenient().when(mVaultsService.getVaults()).thenReturn(Arrays.asList(vault1, null, vault2, vault3));
        lenient().when(mVaultsService.getVault(VAULT_1_ID)).thenReturn(vault1);
        lenient().when(mVaultsService.getVault(VAULT_2_ID)).thenReturn(vault2);
        lenient().when(mVaultsService.getVault(VAULT_3_ID)).thenReturn(null);
        checkRetentionPolicies = new CheckRetentionPolicies(mVaultsService, clock);
    }

    private Vault createVault(String id) {
        return new Vault() {
            @Override
            public String getID() {
                return id;
            }
        };
    }
    
    @Test
    void testExecuteWithNonEmptyVaultList() {
        checkRetentionPolicies.execute();

        verify(mVaultsService).getVaults();
        verify(mVaultsService, times(3)).checkRetentionPolicy(argVaultId1.capture(), eq(RetentionPoliciesService.RetentionPolicyUpdateReason.TASK_UPDATE));
        verify(mVaultsService, times(3)).getVault(argVaultId2.capture());

        assertThat(argVaultId1.getAllValues()).containsExactly(VAULT_1_ID, VAULT_2_ID, VAULT_3_ID);
        assertThat(argVaultId2.getAllValues()).containsExactly(VAULT_1_ID, VAULT_2_ID, VAULT_3_ID);
        
    }

    @Test
    void testExecuteWithEmptyVaultList() {

        // Configure vaultsService to return an empty list
        lenient().when(mVaultsService.getVaults()).thenReturn(List.of());

        checkRetentionPolicies.execute();

        verify(mVaultsService).getVaults();
        // Verify that no calls to checkRetentionPolicy or getVault are made
        verify(mVaultsService, never()).checkRetentionPolicy(anyString(), eq(RetentionPoliciesService.RetentionPolicyUpdateReason.TASK_UPDATE));
        verify(mVaultsService, never()).getVault(anyString());
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(mVaultsService);
    }
}