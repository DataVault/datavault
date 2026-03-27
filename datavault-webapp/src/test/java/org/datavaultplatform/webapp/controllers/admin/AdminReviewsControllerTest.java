package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.response.*;
import org.datavaultplatform.webapp.model.DepositReviewModel;
import org.datavaultplatform.webapp.model.VaultReviewModel;
import org.datavaultplatform.webapp.services.RestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.datavaultplatform.webapp.controllers.admin.AdminReviewsController.ACTION_SUBMIT;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReviewsControllerTest {

    AdminReviewsController spyController;

    @Mock
    RestService mRestService;

    Clock clock;

    @BeforeEach
    void setup() {
        clock = Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneId.systemDefault());
        spyController = Mockito.spy(new AdminReviewsController(mRestService, clock));
    }

    @Test
    void getVaultsForReview() {
        LocalDate today = LocalDate.now(clock);

        VaultReviewStatusInfo statusInfo1 = new VaultReviewStatusInfo();
        VaultReviewStatusInfo statusInfo2 = new VaultReviewStatusInfo();
        
        ModelMap modelMap = new ModelMap();
        VaultsData vaultsForReviewData = new VaultsData();
        VaultInfo vaultInfo1 = new VaultInfo();
        vaultInfo1.setID("vaultId1");
        vaultInfo1.setReviewDate(today.plusMonths(3));
        VaultInfo vaultInfo2 = new VaultInfo();
        vaultInfo2.setID("vaultId2");
        vaultInfo2.setReviewDate(today.plusMonths(5));
        vaultsForReviewData.setData(List.of(vaultInfo1, vaultInfo2));
        
        when(mRestService.getVaultsForReview()).thenReturn(vaultsForReviewData);
        
        when(mRestService.getVaultReviewStatusInfo("vaultId1")).thenReturn(statusInfo1);
        when(mRestService.getVaultReviewStatusInfo("vaultId2")).thenReturn(statusInfo2);
        
        String result = spyController.getVaultsForReview(modelMap);
        assertThat(result).isEqualTo("admin/reviews/index");
        verify(mRestService).getVaultsForReview();
        verify(mRestService).getVaultReviewStatusInfo("vaultId1");
        verify(mRestService).getVaultReviewStatusInfo("vaultId2");
        verifyNoMoreInteractions(mRestService);
        assertThat(modelMap.getAttribute("vaults")).isEqualTo(List.of(vaultInfo1, vaultInfo2));
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            errorMsg, false, true,
            null, false, true,
            reviewdate, true, true,
            null, true, true,
            errorMsg, false, false,
            null, false, false,
            reviewdate, true, false,
            null, true, false
            """)
    void testShowReview(String errorMsg, boolean hasDataOwner, boolean hasCurrentReview) {
        ModelMap modelMap = new ModelMap();

        Group vaultGroup = new Group() {
            @Override
            public String getID() {
                return "vaultGroupId";
            }
        };
        RoleModel ndmRole = new RoleModel();
        ndmRole.setName("Nominated Data Manager");

        RoleModel dataOwnerRole = new RoleModel();
        dataOwnerRole.setName("Data Owner");

        RoleAssignment ndmRA1 = new RoleAssignment();
        ndmRA1.setRole(ndmRole);

        RoleAssignment ndmRA2 = new RoleAssignment();
        ndmRA2.setRole(ndmRole);

        RoleAssignment dataOwnerRA = new RoleAssignment();
        dataOwnerRA.setRole(dataOwnerRole);

        VaultInfo vaultInfo = new VaultInfo();
        vaultInfo.setPolicyID("vaultPolicyId");
        vaultInfo.setGroupID("vaultGroupId");
        vaultInfo.setReviewDate(LocalDate.now().plusYears(4));
        when(mRestService.getVault("vaultId")).thenReturn(vaultInfo);

        ReviewInfo currentReviewInfo = new ReviewInfo();
        currentReviewInfo.setDepositReviewIds(List.of("depRevId1", "depRevId2"));
        currentReviewInfo.setDepositIds(List.of("depId1", "depId2"));
        currentReviewInfo.setVaultReviewId("vaultReviewId");

        if (hasCurrentReview) {
            when(mRestService.getCurrentReview("vaultId")).thenReturn(currentReviewInfo);
        } else {
            when(mRestService.getCurrentReview("vaultId")).thenReturn(null);
            when(mRestService.createCurrentReview("vaultId")).thenReturn(currentReviewInfo);
        }
        VaultReview currentVaultReview = new VaultReview();
        when(mRestService.getVaultReview("vaultReviewId")).thenReturn(currentVaultReview);


        List<RoleAssignment> roleAssignments = new ArrayList<>(List.of(ndmRA1, ndmRA2));
        if (hasDataOwner) {
            roleAssignments.add(dataOwnerRA);
        }
        when(mRestService.getRoleAssignmentsForVault("vaultId")).thenReturn(roleAssignments);

        CreateRetentionPolicy exitingRetentionPolicy = new CreateRetentionPolicy();
        exitingRetentionPolicy.setId(123);

        when(mRestService.getRetentionPolicy("vaultPolicyId")).thenReturn(exitingRetentionPolicy);
        when(mRestService.getGroup("vaultGroupId")).thenReturn(vaultGroup);

        when(mRestService.getDeposit(anyString())).thenAnswer(invocation -> {
            String depositId = invocation.getArgument(0);
            DepositInfo result = new DepositInfo();
            result.setID(depositId);
            result.setName(depositId + "-name");
            return result;
        });

        when(mRestService.getDepositReview(anyString())).thenAnswer(invocation -> {
            String depositReviewId = invocation.getArgument(0);
            DepositReview result = new DepositReview();
            result.setId(depositReviewId);
            result.setComment(depositReviewId + "-comment");
            return result;
        });
        String result = spyController.showReview(modelMap, "vaultId", errorMsg);

        assertThat(result).isEqualTo("admin/reviews/create");

        CreateRetentionPolicy retentionPolicy = (CreateRetentionPolicy) modelMap.getAttribute("createRetentionPolicy");
        assertThat(retentionPolicy).isNotEqualTo(exitingRetentionPolicy);

        assertThat(modelMap.getAttribute("vault")).isEqualTo(vaultInfo);

        VaultReviewModel vrm = (VaultReviewModel) modelMap.getAttribute("vaultReviewModel");
        assertThat(vrm).isNotNull();

        List<DepositReviewModel> drms = vrm.getDepositReviewModels();
        assertThat(drms).hasSize(2);

        DepositReviewModel drm1 = drms.get(0);
        assertThat(drm1.getDepositReviewId()).isEqualTo("depRevId1");
        assertThat(drm1.getDepositId()).isEqualTo("depId1");
        assertThat(drm1.getDepositName()).isEqualTo("depId1-name");
        assertThat(drm1.getComment()).isEqualTo("depRevId1-comment");

        DepositReviewModel drm2 = drms.get(1);
        assertThat(drm2.getDepositReviewId()).isEqualTo("depRevId2");
        assertThat(drm2.getDepositId()).isEqualTo("depId2");
        assertThat(drm2.getDepositName()).isEqualTo("depId2-name");
        assertThat(drm2.getComment()).isEqualTo("depRevId2-comment");

        if (hasCurrentReview) {
            verify(mRestService, never()).createCurrentReview(anyString());
        } else {
            verify(mRestService).createCurrentReview(anyString());
        }

        List<RoleAssignment> dataManagers = (List<RoleAssignment>) modelMap.getAttribute("dataManagers");
        assertThat(dataManagers).hasSize(2);
        assertThat(dataManagers.get(0)).isEqualTo(ndmRA1);
        assertThat(dataManagers.get(1)).isEqualTo(ndmRA2);

        RoleAssignment dataOwner = (RoleAssignment) modelMap.getAttribute("dataOwner");
        if (hasDataOwner) {
            assertThat(dataOwner).isEqualTo(dataOwnerRA);
        } else {
            assertThat(dataOwner).isNull();
        }
        String actualError = (String) modelMap.getAttribute("error");
        String expectedError = null;
        if ("reviewdate".equals(errorMsg)) {
            expectedError = "If some deposits are to be retained then a next Review Date must be entered";
        }
        assertThat(actualError).isEqualTo(expectedError);
    }

    @Nested
    class ProcessReviewTests {

        @Test
        void processReview_NullVaultReviewModel() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                RedirectAttributes mRedirectAttrs = mock(RedirectAttributes.class);
                spyController.processReview(null, mRedirectAttrs, "vaultId", "reviewId", AdminReviewsController.ACTION_CANCEL);
            });
            assertThat(ex).hasMessage("VaultReviewModel must not be null");
            verifyNoMoreInteractions(mRestService);
        }

        @Test
        void processReview_ActionCancel() {
            VaultReviewModel vrm = new VaultReviewModel();
            RedirectAttributes mRedirectAttrs = mock(RedirectAttributes.class);
            String result = spyController.processReview(vrm, mRedirectAttrs, "vaultId", "reviewId", AdminReviewsController.ACTION_CANCEL);
            assertThat(result).isEqualTo("redirect:/admin/reviews");
            verifyNoMoreInteractions(mRestService);
        }
        
        @Test
        void processReview_ActionSubmit_InvalidNextReviewDate() {
            VaultReviewModel vrm = new VaultReviewModel();
            RedirectAttributes mRedirectAttrs = mock(RedirectAttributes.class);

            when(spyController.validateNextReviewDate(vrm, mRedirectAttrs)).thenReturn(false);

            String result = spyController.processReview(vrm, mRedirectAttrs, "vaultId", "reviewId", ACTION_SUBMIT);

            assertThat(result).isEqualTo("redirect:/admin/vaults/vaultId/reviews");
            
            verify(spyController).validateNextReviewDate(vrm, mRedirectAttrs);
            verifyNoMoreInteractions(mRestService);
        }

        @Captor
        ArgumentCaptor<DepositReviewModel> argDRM;

        @Test
        void processReview_ActionSubmit_ValidNextReviewDate() {
            VaultReviewModel vrm = new VaultReviewModel();
            DepositReviewModel drm1 = new DepositReviewModel();
            DepositReviewModel drm2 = new DepositReviewModel();
            vrm.setDepositReviewModels(List.of(drm1, drm2));
            RedirectAttributes mRedirectAttrs = mock(RedirectAttributes.class);

            doReturn(true).when(spyController).validateNextReviewDate(vrm, mRedirectAttrs);

            VaultReview originalVaultReview = new VaultReview();
            doNothing().when(spyController).updateVaultReviewAndVault(originalVaultReview, vrm, "vaultId", LocalDateTime.now(clock), ACTION_SUBMIT);
            
            doNothing().when(spyController).processSingleDepositReview(any(DepositReviewModel.class), eq(LocalDateTime.now(clock)), eq(ACTION_SUBMIT));

            when(mRestService.getVaultReview("reviewId")).thenReturn(originalVaultReview);
            String result = spyController.processReview(vrm, mRedirectAttrs, "vaultId", "reviewId", ACTION_SUBMIT);

            assertThat(result).isEqualTo("redirect:/admin/reviews");
            
            verify(spyController).validateNextReviewDate(vrm, mRedirectAttrs);
            verify(spyController).updateVaultReviewAndVault(originalVaultReview, vrm, "vaultId", LocalDateTime.now(clock), ACTION_SUBMIT);
            verify(spyController, times(2)).processSingleDepositReview(argDRM.capture(), eq(LocalDateTime.now(clock)), eq(ACTION_SUBMIT));

            verify(mRestService).getVaultReview("reviewId");
        
            assertThat(argDRM.getAllValues()).containsExactlyInAnyOrder(drm1, drm2);
            
            verifyNoMoreInteractions(mRestService);
        }
        
        @Nested
        class ValidateNextReviewDateTests {
            
            @Test
            void testValidate_NullNextReviewDate() {
                VaultReviewModel vrm = new VaultReviewModel();

                RedirectAttributes mRedirectAttrs = mock(RedirectAttributes.class);
                boolean result = spyController.validateNextReviewDate(vrm, mRedirectAttrs);
                assertThat(result).isTrue();

            }
            
            @Test
            void testValidate_NullDepositReviewModels() {
                VaultReviewModel vrm = new VaultReviewModel();
                vrm.setNextReviewDate(LocalDate.now().plusYears(1));

                RedirectAttributes mRedirectAttrs = mock(RedirectAttributes.class);
                boolean result = spyController.validateNextReviewDate(vrm, mRedirectAttrs);
                assertThat(result).isTrue();
                verify(mRedirectAttrs, never()).addAttribute("error", "reviewdate");
            }
            
            @Test
            void testValidate_NullDepositReviewModelsList() {
                VaultReviewModel vrm = new VaultReviewModel();
                vrm.setNextReviewDate(null);
                vrm.setDepositReviewModels(null);

                RedirectAttributes mRedirectAttrs = mock(RedirectAttributes.class);
                boolean result = spyController.validateNextReviewDate(vrm, mRedirectAttrs);
                assertThat(result).isTrue();
                verify(mRedirectAttrs, never()).addAttribute("error", "reviewdate");
                
            }
            
            @Test
            void testValidate_DepositReviewModelsWithoutRetain() {

                List<DepositReviewModel> models = Stream.of(
                        null,
                        DepositReviewDeleteStatus.NOW,
                        DepositReviewDeleteStatus.ONREVIEW,
                        DepositReviewDeleteStatus.ONEXPIRY).map(deleteStatus -> {
                    DepositReviewModel drm = new DepositReviewModel();
                    if (deleteStatus != null) {
                        drm.setDeleteStatus(deleteStatus);
                        return drm;
                    } else {
                        return null;
                    }
                }).toList();

                VaultReviewModel vrm = new VaultReviewModel();
                vrm.setNextReviewDate(null);
                vrm.setDepositReviewModels(models);

                RedirectAttributes mRedirectAttrs = mock(RedirectAttributes.class);
                boolean result = spyController.validateNextReviewDate(vrm, mRedirectAttrs);
                assertThat(result).isTrue();
                verify(mRedirectAttrs, never()).addAttribute("error", "reviewdate");
            }
            
            @Test
            void testValidate_DepositReviewModelsWithRetain() {

                List<DepositReviewModel> models = IntStream.of(
                        DepositReviewDeleteStatus.NOW,
                        DepositReviewDeleteStatus.ONREVIEW,
                        DepositReviewDeleteStatus.ONEXPIRY,
                        DepositReviewDeleteStatus.RETAIN).mapToObj(deleteStatus -> {
                    DepositReviewModel drm = new DepositReviewModel();
                    drm.setDeleteStatus(deleteStatus);
                    return drm;
                }).toList();

                VaultReviewModel vrm = new VaultReviewModel();
                vrm.setNextReviewDate(null);
                vrm.setDepositReviewModels(models);

                RedirectAttributes mRedirectAttrs = mock(RedirectAttributes.class);
                boolean result = spyController.validateNextReviewDate(vrm, mRedirectAttrs);
                assertThat(result).isFalse();
                verify(mRedirectAttrs).addAttribute("error", "reviewdate");
            }
        }
        
        @Nested
        class UpdateVaultReviewAndVaultTests {
            
            @Test
            void testNullOriginalVaultReviewModel() {
             
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                    spyController.updateVaultReviewAndVault(null, new VaultReviewModel(), "vaultId", LocalDateTime.now(clock), "action");
                });
                assertThat(ex).hasMessage("originalVaultReview cannot be null");
            }
            
            @Test
            void testNullVaultReviewModel() {

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                    spyController.updateVaultReviewAndVault(new VaultReview(), null, "vaultId", LocalDateTime.now(clock), "action");
                });
                assertThat(ex).hasMessage("vaultReviewModel cannot be null");
            }

            @ParameterizedTest()
            @ValueSource(strings = { 
                    AdminReviewsController.ACTION_CANCEL,
                    AdminReviewsController.ACTION_SAVE} )
            void testUpdateVaultReviewAndVault_NonSubmit(String action) {
                VaultReview originalVaultReview = new VaultReview();
                VaultReviewModel vrm = new VaultReviewModel();
                spyController.updateVaultReviewAndVault(originalVaultReview, vrm, "vaultId", LocalDateTime.now(clock), action);
                
                verify(mRestService).editVaultReview(originalVaultReview);
                verifyNoMoreInteractions(mRestService);
            }
            
            @Test
            void testUpdateVaultReviewAndVault_Submit_WithoutNextReviewDate(){
                VaultReview originalVaultReview = new VaultReview();
                
                VaultReviewModel vrm = new VaultReviewModel();
                
                
                VaultInfo vaultInfo = new VaultInfo();
                vaultInfo.setReviewDate(LocalDate.now().plusYears(4));
                
                when(mRestService.getVault("vaultId")).thenReturn(vaultInfo);
                spyController.updateVaultReviewAndVault(originalVaultReview, vrm, "vaultId", LocalDateTime.now(clock), ACTION_SUBMIT);
                
                assertThat(originalVaultReview.getActionedDate()).isEqualTo(LocalDateTime.now(clock));
                assertThat(originalVaultReview.getOldReviewDate()).isEqualTo(LocalDate.now().plusYears(4));
                
                verify(mRestService).getVault("vaultId");
                verify(mRestService).editVaultReview(originalVaultReview);
                verifyNoMoreInteractions(mRestService);
            }
            
            @Test
            void testUpdateVaultReviewAndVault_Submit_WithNextReviewDate(){
                VaultReview originalVaultReview = new VaultReview();

                LocalDate nextReviewDate = LocalDate.now(clock).plusYears(3);
                LocalDate reviewDate = LocalDate.now(clock).plusYears(4);
                
                VaultReviewModel vrm = new VaultReviewModel();
                vrm.setNextReviewDate(nextReviewDate);
                
                VaultInfo vaultInfo = new VaultInfo();
                vaultInfo.setReviewDate(reviewDate);

                when(mRestService.getVault("vaultId")).thenReturn(vaultInfo);
                when(mRestService.editVaultReview(originalVaultReview)).thenReturn(originalVaultReview);
                
                LocalDateTime actionedDateTime = LocalDateTime.now(clock);
                spyController.updateVaultReviewAndVault(originalVaultReview, vrm, "vaultId", actionedDateTime, ACTION_SUBMIT);

                assertThat(originalVaultReview.getActionedDate()).isEqualTo(actionedDateTime);
                assertThat(originalVaultReview.getOldReviewDate()).isEqualTo(reviewDate);

                verify(mRestService).getVault("vaultId");
                verify(mRestService).editVaultReview(originalVaultReview);
                verify(mRestService).updateReviewDateOfVault("vaultId", nextReviewDate);
                verifyNoMoreInteractions(mRestService);
            }
        }
    }
    
    @Nested
    class ProcessSingleReviewTests {

        LocalDateTime timestamp;

        @BeforeEach
        void setup() {
            timestamp = LocalDateTime.now(clock);
        }

        @Test
        void testNullVaultReviewModel() {
            
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                spyController.processSingleDepositReview(null, timestamp, "action" );
            });
            assertThat(ex).hasMessage("The depositReviewModel cannot be null");
        }
        
        @Captor
        ArgumentCaptor<DepositReview> argDepositReview;
        
        @ParameterizedTest
        @CsvSource(nullValues = "null", textBlock = """
               action,  123
               null,    123
               Submit,  123
               """)
        void testNotSubmitActionWithDeleteStatusNowOrRetain(String action, int deleteStatus) {

            DepositReviewModel drm = new DepositReviewModel();
            drm.setDepositReviewId("depositReviewId");
            drm.setComment("comment123");
            drm.setDepositId("depositId");
            drm.setDeleteStatus(deleteStatus);

            DepositReview depositReview = new DepositReview();
            depositReview.setId("depositReviewId");
            
            when(mRestService.getDepositReview(anyString())).thenReturn(depositReview);
            
            spyController.processSingleDepositReview(drm, timestamp, action);
            
            verify(mRestService).getDepositReview("depositReviewId");
            verify(mRestService).editDepositReview(argDepositReview.capture());
            verify(mRestService, never()).deleteDeposit(anyString());
            verifyNoMoreInteractions(mRestService);
            
            DepositReview editedDepositReview = argDepositReview.getValue();
            assertThat(editedDepositReview).isEqualTo(depositReview);
            
            assertThat(editedDepositReview.getDeleteStatus()).isEqualTo(deleteStatus);
            assertThat(editedDepositReview.getComment()).isEqualTo("comment" + deleteStatus);
            assertThat(editedDepositReview.getActionedDate()).isNull();
        }

        @Test
        void testSubmitActionWithDeleteStatusNow() {

            DepositReviewModel drm = new DepositReviewModel();
            drm.setDepositReviewId("depositReviewId");
            drm.setComment("comment");
            drm.setDepositId("depositId");
            drm.setDeleteStatus(DepositReviewDeleteStatus.NOW);

            DepositReview depositReview = new DepositReview();
            depositReview.setId("depositReviewId");

            when(mRestService.getDepositReview(anyString())).thenReturn(depositReview);

            spyController.processSingleDepositReview(drm, timestamp, ACTION_SUBMIT);

            verify(mRestService).getDepositReview("depositReviewId");
            verify(mRestService).editDepositReview(argDepositReview.capture());
            verify(mRestService).deleteDeposit("depositId");
            verifyNoMoreInteractions(mRestService);

            DepositReview editedDepositReview = argDepositReview.getValue();
            assertThat(editedDepositReview).isEqualTo(depositReview);
            

            assertThat(editedDepositReview.getDeleteStatus()).isEqualTo(DepositReviewDeleteStatus.NOW);
            assertThat(editedDepositReview.getComment()).isEqualTo("comment");
            assertThat(editedDepositReview.getActionedDate()).isEqualTo(timestamp);
            
            
        }

        @Test
        void testSubmitActionWithDeleteStatusRetain() {

            DepositReviewModel drm = new DepositReviewModel();
            drm.setDepositReviewId("depositReviewId");
            drm.setComment("comment");
            drm.setDepositId("depositId");
            drm.setDeleteStatus(DepositReviewDeleteStatus.RETAIN);

            DepositReview depositReview = new DepositReview();
            depositReview.setId("depositReviewId");

            when(mRestService.getDepositReview(anyString())).thenReturn(depositReview);

            spyController.processSingleDepositReview(drm, timestamp, ACTION_SUBMIT);

            verify(mRestService).getDepositReview("depositReviewId");
            verify(mRestService).editDepositReview(argDepositReview.capture());
            verify(mRestService, never()).deleteDeposit("depositId");
            verifyNoMoreInteractions(mRestService);

            DepositReview editedDepositReview = argDepositReview.getValue();
            assertThat(editedDepositReview).isEqualTo(depositReview);

            assertThat(editedDepositReview.getDeleteStatus()).isEqualTo(DepositReviewDeleteStatus.RETAIN);
            assertThat(editedDepositReview.getComment()).isEqualTo("comment");
            assertThat(editedDepositReview.getActionedDate()).isEqualTo(timestamp);


        }
    }
}

