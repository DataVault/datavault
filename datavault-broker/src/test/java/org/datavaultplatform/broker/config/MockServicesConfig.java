package org.datavaultplatform.broker.config;


import org.datavaultplatform.broker.services.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

/*
This class creates mock services beans which are placed into the spring context.
 */
@TestConfiguration
public class MockServicesConfig {

  @MockBean
  PausedStateService mPausedStateService;

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
