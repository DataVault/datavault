package org.datavaultplatform.broker.controllers;

import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.response.VaultInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class VaultsControllerTest {

    public static final String USER_ID = "user-id";
    public static final String VAULT_ID = "vault-id";
    @Mock
    EmailService mEmailService;
    @Mock
    VaultsService mVaultsService;
    @Mock
    PendingVaultsService mPendingVaultsService;
    @Mock
    PendingDataCreatorsService mPendingDataCreatorsService;
    @Mock
    DepositsService mDepositsService;
    @Mock
    ExternalMetadataService mExternalMetadataService;
    @Mock
    RetentionPoliciesService mRetentionPoliciesService;
    @Mock
    GroupsService mGroupsService;
    @Mock
    UsersService mUsersService;
    @Mock
    EventService mEventService;
    @Mock
    ClientsService mClientsService;
    @Mock
    DataManagersService mDataManagersService;
    @Mock
    RolesAndPermissionsService PermissionsService;
    
    String activeDir;
    String archiveDir;
    String homePage;
    String helpPage;

    VaultsController controller;
    
    @Mock
    User mUser;
    
    @Mock
    Vault mVault;

    @Mock
    VaultInfo mVaultInfo;
    
    @Captor
    ArgumentCaptor<Date> argReviewDate;
    
    @BeforeEach
    void setup(){
        controller = new VaultsController(
                mEmailService,
                mVaultsService,
                mPendingVaultsService,
                mPendingDataCreatorsService,
                mDepositsService,
                mExternalMetadataService,
                mRetentionPoliciesService,
                mGroupsService,
                mUsersService,
                mEventService,
                mClientsService,
                mDataManagersService,
                PermissionsService,
                activeDir,
                archiveDir,
                homePage,
                helpPage);
    }
    
    @Test
    void testUpdateReviewDate() throws Exception {
        String testReviewDateString = "2112-12-21";
        Mockito.when(mUsersService.getUser(USER_ID)).thenReturn(mUser);
        Mockito.when(mVaultsService.getUserVault(mUser, VAULT_ID)).thenReturn(mVault);
        
        Mockito.doNothing().when(mVault).setReviewDate(argReviewDate.capture());
        Mockito.when(mVault.convertToResponse()).thenReturn(mVaultInfo);
        
        VaultInfo result = controller.updateVaultReviewDate(USER_ID, VAULT_ID, testReviewDateString);
        assertThat(result).isSameAs(mVaultInfo);
        
        LocalDate localVaultReviewDate = LocalDate.ofInstant(argReviewDate.getValue().toInstant(), ZoneOffset.UTC);
        assertThat(localVaultReviewDate).hasYear(2112);
        assertThat(localVaultReviewDate).hasMonth(Month.DECEMBER);
        assertThat(localVaultReviewDate).hasDayOfMonth(21);
        
        Mockito.verify(mUsersService).getUser(USER_ID);
        Mockito.verify(mVaultsService).getUserVault(mUser, VAULT_ID);
        Mockito.verify(mVault).setReviewDate(argReviewDate.getValue());
        Mockito.verify(mVaultsService).updateVault(mVault);
        Mockito.verify(mVault).convertToResponse();
        
        Mockito.verifyNoMoreInteractions(mUser, mVault, mUsersService, mVaultsService, mVaultInfo);
        
    }
}