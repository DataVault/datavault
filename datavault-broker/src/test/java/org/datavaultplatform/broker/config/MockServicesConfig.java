package org.datavaultplatform.broker.config;

import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.broker.services.AuditsService;
import org.datavaultplatform.broker.services.ClientsService;
import org.datavaultplatform.broker.services.DepositsReviewService;
import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.EmailService;
import org.datavaultplatform.broker.services.JobsService;
import org.datavaultplatform.broker.services.RolesAndPermissionsService;
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
  ArchiveStoreService mArchiveStoreService;

  @MockBean
  DepositsService mDepositsService;

  @MockBean
  LDAPService mLdapService;

  @MockBean
  EmailService mEmailService;

  @MockBean
  JobsService mJobService;

  @MockBean
  Sender sender;

  @MockBean
  VaultsService mVaultsService;

  @MockBean
  AuditsService mAuditsService;

  @MockBean
  VaultsReviewService mVaultsReviewService;

  @MockBean
  RolesAndPermissionsService mRolesAndPermissionService;

  @MockBean
  DepositsReviewService mDepositsReviewService;

  @MockBean
  UsersService mUsersService;

  @MockBean
  ClientsService mClientsService;

  @MockBean
  AdminService mAdminService;
}
