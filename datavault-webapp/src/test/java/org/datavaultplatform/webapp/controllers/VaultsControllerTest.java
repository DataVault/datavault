package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.EventInfo;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.util.RoleUtils;
import org.datavaultplatform.webapp.exception.ForbiddenException;
import org.datavaultplatform.webapp.model.DepositReviewModel;
import org.datavaultplatform.webapp.model.VaultReviewHistoryModel;
import org.datavaultplatform.webapp.model.VaultReviewModel;
import org.datavaultplatform.webapp.services.ForceLogoutService;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.services.UserLookupService;
import org.datavaultplatform.webapp.services.ValidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ModelMap;

import java.security.Principal;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultsControllerTest {

    Clock clock = Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneId.systemDefault());

    @Mock
    RestService restService;

    @Mock
    UserLookupService userLookupService;

    @Mock
    ValidateService validateService;

    @Mock
    ForceLogoutService logoutService;
    
    @Mock
    Principal loggedInUser;

    VaultsController controllerSpy;

    LocalDate vaultReviewDate = LocalDate.now(clock);

    
    Vault vault;
    @BeforeEach
    void setup() {
        controllerSpy = Mockito.spy(new VaultsController( restService, userLookupService, logoutService, validateService, "system", "link", "welcome"));
        lenient().when(loggedInUser.getName()).thenReturn("loggedInUserName");
        
        vault = new Vault(){
            @Override
            public String getID(){
                return "vaultId";
            }
        };
    }
    
    
    @Nested
    class GetVaultsTests {
        
        @Test
        void testForbidden() {
            ModelMap modelMap = new ModelMap();

            VaultInfo vaultInfo = new VaultInfo();
            vaultInfo.setID("vaultId");
            vaultInfo.setPolicyID("policyId");
            vaultInfo.setGroupID("groupId");
            vaultInfo.setReviewDate(vaultReviewDate);

            when(restService.getVault("vaultId")).thenReturn(vaultInfo);

            doReturn(false).when(controllerSpy).canAccessVault(vaultInfo, loggedInUser);

            var ex = assertThrows(ForbiddenException.class, () -> {
                controllerSpy.getVaultByVaultId(modelMap, "vaultId", loggedInUser);
            });
            assertThat(ex).hasMessage("Access denied");

            verify(controllerSpy).canAccessVault(vaultInfo, loggedInUser);
        }
        
        /*
        This unit test code highlights that the methods in VaultsController could do with splitting up to make testing easier.
         */
        @Test
        void testNoDataOwnerRoleAssignments() {

            VaultInfo vaultInfo = new VaultInfo();
            vaultInfo.setID("vaultId");
            vaultInfo.setPolicyID("policyId");
            vaultInfo.setGroupID("groupId");
            vaultInfo.setReviewDate(vaultReviewDate);
            
            RoleAssignment raVault1 = new RoleAssignment();
            RoleAssignment raVault2 = new RoleAssignment();
            RoleAssignment raUser1 = new RoleAssignment();
            RoleAssignment raUser2 = new RoleAssignment();
            RoleModel vaultRole1 = new RoleModel();
            RoleModel vaultRole2 = new RoleModel();
            RoleModel validRole1 = new RoleModel();
            RoleModel validRole2 = new RoleModel();
            List<RoleModel> vaultRoles = List.of(vaultRole1, vaultRole2);
            List<RoleAssignment> roleAssignmentsForVault = List.of(raVault1, raVault2);
            List<RoleAssignment> roleAssignmentsForUser = List.of(raUser1, raUser2);
            List<RoleModel> validRoles = List.of(validRole1, validRole2);
            CreateRetentionPolicy createRetentionPolicy = new CreateRetentionPolicy();
            Group group  = new Group();


            DepositInfo defInfo1 = new DepositInfo();
            defInfo1.setID("depositId1");
            defInfo1.setName("depositName1");

            DepositInfo defInfo2 = new DepositInfo();
            defInfo2.setID("depositId2");
            defInfo2.setName("depositName2");
            
            DepositInfo[] depositInfos = {defInfo1, defInfo2};
            
            Retrieve dep1ret1 = new Retrieve();
            Retrieve dep1ret2 = new Retrieve();
            Retrieve dep2ret1 = new Retrieve();
            Retrieve dep2ret2 = new Retrieve();
            
            Retrieve[] deposit1retrieves = {dep1ret1, dep1ret2};
            Retrieve[] deposit2retrieves = {dep2ret1, dep2ret2};

            ReviewInfo reviewInfo1 = new ReviewInfo();
            reviewInfo1.setVaultReviewId("vaultReviewId1");
            reviewInfo1.setDepositIds(      List.of("rev1depId1",   "rev1depId2"));
            reviewInfo1.setDepositReviewIds(List.of("rev1depRevId1","rev1depRevId2"));

            ReviewInfo reviewInfo2 = new ReviewInfo();
            reviewInfo2.setVaultReviewId("vaultReviewId2");
            reviewInfo2.setDepositIds(      List.of("rev2depId1",   "rev2depId2"));
            reviewInfo2.setDepositReviewIds(List.of("rev2depRevId1","rev2depRevId2"));

            ReviewInfo[] reviewInfos = {reviewInfo1, reviewInfo2};

            when(restService.getVault("vaultId")).thenReturn(vaultInfo);
            doReturn(true).when(controllerSpy).canAccessVault(vaultInfo, loggedInUser);


            when(restService.getRoleAssignmentsForVault("vaultId")).thenReturn(roleAssignmentsForVault);
            when(restService.getRoleAssignmentsForUser("loggedInUserName")).thenReturn(roleAssignmentsForUser);

            when(restService.getVaultRoles()).thenReturn(vaultRoles);
            
            when(restService.getRetentionPolicy("policyId")).thenReturn(createRetentionPolicy);
            when(restService.getGroup("groupId")).thenReturn(group);
            
            when(restService.getDepositsListing("vaultId")).thenReturn(depositInfos);
            
            lenient().when(restService.getDepositRetrieves("depositId1")).thenReturn(deposit1retrieves);
            lenient().when(restService.getDepositRetrieves("depositId2")).thenReturn(deposit2retrieves);
            
            when(restService.getDataManagers("vaultId")).thenReturn(new DataManager[0]);
            
            when(restService.getVaultsRoleEvents("vaultId")).thenReturn(new EventInfo[0]);
            
            when(restService.getReviewsListing("vaultId")).thenReturn(reviewInfos);

            when(restService.getVaultReview(anyString())).thenAnswer(invocation -> {
                VaultReview vaultReview = new VaultReview();
                String vaultReviewId = invocation.getArgument(0);
                vaultReview.setId(vaultReviewId);
                vaultReview.setComment("comment4" + vaultReviewId);
                vaultReview.setActionedDate(LocalDateTime.now(clock));
                return vaultReview; 
            });
            when(restService.getDeposit(anyString())).thenAnswer(invocation -> {
                String depositId = invocation.getArgument(0);
                DepositInfo depositInfo = new DepositInfo();
                depositInfo.setID(depositId);
                depositInfo.setName(depositId+"name");
                depositInfo.setCreationTime(LocalDateTime.now(clock));
                return depositInfo;
             });

            AtomicInteger counter= new AtomicInteger(0);
            when(restService.getDepositReview(anyString())).thenAnswer(invocation -> {
                DepositReview dr = new DepositReview();
                String depositReviewId = invocation.getArgument(0);
                dr.setId(depositReviewId);
                dr.setComment(depositReviewId+"-comment");
                dr.setDeleteStatus(counter.incrementAndGet());
                return dr;
            });

            ModelMap modelMap = new ModelMap();
            try(MockedStatic<RoleUtils> mockStatic = Mockito.mockStatic(RoleUtils.class)) {
                mockStatic.when(() -> RoleUtils.isDataOwner(any())).thenReturn(false);
                mockStatic.when(() -> RoleUtils.getAssignableRoles(roleAssignmentsForUser, vaultRoles)).thenReturn(validRoles);

                String result = controllerSpy.getVaultByVaultId(modelMap, "vaultId", loggedInUser);
                assertThat(result).isEqualTo(  "vaults/vault");
                
                mockStatic.verify(() -> RoleUtils.isDataOwner(any()), times(4));
                mockStatic.verify(() -> RoleUtils.getAssignableRoles(roleAssignmentsForUser, vaultRoles));
                
                verify(restService).getVault("vaultId");
                verify(restService).getRoleAssignmentsForVault("vaultId");
                verify(restService).getRoleAssignmentsForUser("loggedInUserName");
                verify(restService).getVaultRoles();
                verify(restService).getRetentionPolicy("policyId");
                verify(restService).getGroup("groupId");
                verify(restService).getDepositsListing("vaultId");
                verify(restService).getDepositRetrieves("depositId1");
                verify(restService).getDepositRetrieves("depositId2");
                verify(restService).getDataManagers("vaultId");
                verify(restService).getVaultsRoleEvents("vaultId");
                
                verify(restService, times(2)).getVaultReview(anyString());

                verify(restService, times(4)).getDeposit(anyString());
                verify(restService, times(4)).getDepositReview(anyString());
                verifyNoMoreInteractions(restService);
            }
            assertThat(modelMap).containsEntry("vault", vaultInfo);
            assertThat(modelMap).containsEntry("roles", validRoles);
            assertThat(modelMap).containsEntry("createRetentionPolicy", createRetentionPolicy);
            assertThat(modelMap).containsEntry("group", group);
            assertThat(modelMap).containsEntry("deposits", depositInfos);
            Map<String, Retrieve[]> retrievals = (Map<String, Retrieve[]>)modelMap.get("retrievals");
            assertThat(retrievals).containsEntry("depositName1", deposit1retrieves);
            assertThat(retrievals).containsEntry("depositName2", deposit2retrieves);
            assertThat(modelMap).containsEntry("dataManagers", List.of());
            assertThat(modelMap).containsEntry("roleEvents", new EventInfo[0]);

            VaultReviewHistoryModel vrhm = (VaultReviewHistoryModel) modelMap.getAttribute("vrhm");
            
            List<VaultReviewModel> vaultReviewModels = vrhm.getVaultReviewModels();
            assertThat(vaultReviewModels).hasSize(2);

            {
                VaultReviewModel vrm1 = vaultReviewModels.get(0);
                assertThat(vrm1.getVaultReviewId()).isEqualTo("vaultReviewId1");
                assertThat(vrm1.getComment()).isEqualTo("comment4vaultReviewId1");
                assertThat(vrm1.getActionedDate()).isEqualTo(LocalDateTime.now(clock));
                assertThat(vrm1.getDepositReviewModels()).hasSize(2);

                DepositReviewModel drm1_1 = vrm1.getDepositReviewModels().get(0);
                assertThat(drm1_1.getDepositId()).isEqualTo("rev1depId1");
                assertThat(drm1_1.getDepositReviewId()).isEqualTo("rev1depRevId1");
                assertThat(drm1_1.getDeleteStatus()).isEqualTo(1);

                DepositReviewModel drm1_2 = vrm1.getDepositReviewModels().get(1);
                assertThat(drm1_2.getDepositId()).isEqualTo("rev1depId2");
                assertThat(drm1_2.getDepositReviewId()).isEqualTo("rev1depRevId2");
                assertThat(drm1_2.getDeleteStatus()).isEqualTo(2);

            }
            {
                VaultReviewModel vrm2 = vaultReviewModels.get(1);
                assertThat(vrm2.getVaultReviewId()).isEqualTo("vaultReviewId2");
                assertThat(vrm2.getComment()).isEqualTo("comment4vaultReviewId2");
                assertThat(vrm2.getActionedDate()).isEqualTo(LocalDateTime.now(clock));
                assertThat(vrm2.getDepositReviewModels()).hasSize(2);

                DepositReviewModel drm2_1 = vrm2.getDepositReviewModels().get(0);
                assertThat(drm2_1.getDepositId()).isEqualTo("rev2depId1");
                assertThat(drm2_1.getDepositReviewId()).isEqualTo("rev2depRevId1");
                assertThat(drm2_1.getDeleteStatus()).isEqualTo(3);

                DepositReviewModel drm2_2 = vrm2.getDepositReviewModels().get(1);
                assertThat(drm2_2.getDepositId()).isEqualTo("rev2depId2");
                assertThat(drm2_2.getDepositReviewId()).isEqualTo("rev2depRevId2");
                assertThat(drm2_2.getDeleteStatus()).isEqualTo(4);
            }
        }
        
    }
}
