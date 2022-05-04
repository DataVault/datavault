package org.datavaultplatform.broker.config;


import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.broker.services.ArchivesService;
import org.datavaultplatform.broker.services.AuditsService;
import org.datavaultplatform.broker.services.BillingService;
import org.datavaultplatform.broker.services.ClientsService;
import org.datavaultplatform.broker.services.DataCreatorsService;
import org.datavaultplatform.broker.services.DataManagersService;
import org.datavaultplatform.broker.services.DepositsReviewService;
import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.EmailService;
import org.datavaultplatform.broker.services.EventService;
import org.datavaultplatform.broker.services.ExternalMetadataService;
import org.datavaultplatform.broker.services.FilesService;
import org.datavaultplatform.broker.services.FileStoreService;
import org.datavaultplatform.broker.services.GroupsService;
import org.datavaultplatform.broker.services.JobsService;
import org.datavaultplatform.broker.services.MetadataService;
import org.datavaultplatform.broker.services.PendingDataCreatorsService;
import org.datavaultplatform.broker.services.PendingVaultsService;
import org.datavaultplatform.broker.services.RetentionPoliciesService;
import org.datavaultplatform.broker.services.RetrievesService;
import org.datavaultplatform.broker.services.RolesAndPermissionsService;
import org.datavaultplatform.broker.services.UserKeyPairService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.broker.services.VaultsReviewService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.services.LDAPService;

import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.boot.test.mock.mockito.MockBean;

/*
This class creates mock services beans which are placed into the spring context.
 */
@TestConfiguration
public class MockServicesConfig {

  @MockBean
  AdminService mAdminService;

  @MockBean
  ArchivesService mArchivesService;

  @MockBean
  ArchiveStoreService mArchiveStoreService;

  @MockBean
  AuditsService mAuditsService;

  @MockBean
  BillingService mBillingService;

  @MockBean
  ClientsService mClientsService;

  @MockBean
  DataCreatorsService mDataCreatorService;

  @MockBean
  DataManagersService mDataManagerService;

  @MockBean
  DepositsReviewService mDepositsReviewService;

  @MockBean
  DepositsService mDepositsService;

  @MockBean
  EmailService mEmailService;

  @MockBean
  EventService mEventService;

  @MockBean
  ExternalMetadataService mExternalMetadataService;

  @MockBean
  FilesService mFilesService;

  @MockBean
  FileStoreService mFileStoreService;

  @MockBean
  GroupsService mGroupsService;

  @MockBean
  JobsService mJobService;

  @MockBean
  MetadataService metadataService;

  @MockBean
  PendingDataCreatorsService mPendingDataCreatorsService;

  @MockBean
  PendingVaultsService mPendingVaultsService;

  @MockBean
  RetentionPoliciesService mRetentionPoliciesService;

  @MockBean
  LDAPService mLdapService;

  @MockBean
  RetrievesService mRetrievesService;

  @MockBean
  RolesAndPermissionsService mRolesAndPermissionService;

  @MockBean
  UserKeyPairService mUserKeyPairService;

  @MockBean
  UsersService mUsersService;

  @MockBean
  VaultsReviewService mVaultsReviewService;

  @MockBean
  VaultsService mVaultsService;

}
