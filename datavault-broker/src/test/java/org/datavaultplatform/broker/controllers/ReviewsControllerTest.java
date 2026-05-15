package org.datavaultplatform.broker.controllers;

import lombok.SneakyThrows;
import org.datavaultplatform.broker.controllers.admin.AdminReviewsController;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.response.ReviewInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewsControllerTest {

    // getVaultReviews

    ReviewsController controller;

    @Mock
    VaultsService mVaultsService;

    @Mock
    VaultsReviewService mVaultReviewService;

    @Mock
    DepositsReviewService mDepositsReviewService;

    @Mock
    UsersService mUsersService;

    @Mock
    ClientsService mClientsService;
    
    @Mock
    EventService mEventService;
            
    @BeforeEach
    void setup() {
        controller = new ReviewsController(mVaultsService, mVaultReviewService, mDepositsReviewService, mUsersService, mClientsService, mEventService);
    }

        
        User user = new User();

        Vault vault = new Vault(){
            @Override
            public String getID() {
                return "vaultId";
            }
        };
        
        @Test
        @SneakyThrows
        void getVaultReviews() {

            VaultReview vr1 = new VaultReview();
            VaultReview vr2 = new VaultReview();
            ReviewInfo reviewInfo1 = new ReviewInfo();
            ReviewInfo reviewInfo2 = new ReviewInfo();
            when(mUsersService.getUser("userId")).thenReturn(user);
            when(mVaultsService.getUserVault(user, "vaultId")).thenReturn(vault);
            when(mVaultReviewService.findByVaultId("vaultId")).thenReturn(Arrays.asList(vr1, null, vr2));

            ArgumentCaptor<VaultReview> argVaultReview = ArgumentCaptor.forClass(VaultReview.class);
            try( MockedStatic<AdminReviewsController> mockedStatic = Mockito.mockStatic(AdminReviewsController.class)){

                mockedStatic.when(() -> AdminReviewsController.getReviewInfo(vr1)).thenReturn(reviewInfo1);
                mockedStatic.when(() -> AdminReviewsController.getReviewInfo(vr2)).thenReturn(reviewInfo2);

                List<ReviewInfo> result = controller.getVaultReviews("userId", "vaultId");
                assertThat(result).isEqualTo(List.of(reviewInfo1, reviewInfo2));
                
                mockedStatic.verify(() -> AdminReviewsController.getReviewInfo(argVaultReview.capture()), times(2));
                assertThat(argVaultReview.getAllValues().get(0)).isEqualTo(vr1);
                assertThat(argVaultReview.getAllValues().get(1)).isEqualTo(vr2);
            }
            verify(mUsersService).getUser("userId");
            verify(mVaultsService).getUserVault(user, "vaultId");
            verify(mVaultReviewService).findByVaultId("vaultId");

            verifyNoMoreInteractions(mUsersService, mVaultsService, mVaultsService,
                    mClientsService, mEventService, mVaultReviewService);
        }
        
    @Test
    void testGetVaultReview() {
        VaultReview vaultReview = new VaultReview();
        when(mVaultReviewService.getVaultReview("vaultReviewId")).thenReturn(vaultReview);
        VaultReview result = controller.getVaultReview("userId", "vaultReviewId");
        
        assertThat(result).isEqualTo(vaultReview);
        
        verify(mVaultReviewService).getVaultReview("vaultReviewId");

        verifyNoMoreInteractions(mUsersService, mVaultsService, mVaultsService,
                mClientsService, mEventService, mVaultReviewService);
    }

    @Test
    void testGetDepositReviews(){
        DepositReview dr1 = new DepositReview();
        DepositReview dr2 = new DepositReview();
        VaultReview mVaultReview = mock(VaultReview.class);

        when(mVaultReviewService.getVaultReview("vaultReviewId")).thenReturn(mVaultReview);
        when(mVaultReview.getDepositReviews()).thenReturn(List.of(dr1, dr2));

        List<DepositReview> result = controller.getDepositReviews("userId", "vaultReviewId");
        assertThat(result).isEqualTo(List.of(dr1, dr2));

        verify(mVaultReviewService).getVaultReview("vaultReviewId");
        verify(mVaultReview).getDepositReviews();

        verifyNoMoreInteractions(mUsersService, mVaultsService, mVaultsService, mClientsService,
                mEventService, mVaultReviewService, mVaultReview);
    }

    @Test
    void testGetDepositReview(){
    
        DepositReview depositReview = new DepositReview();

        when(mDepositsReviewService.getDepositReview("depositReviewId")).thenReturn(depositReview);
        
        DepositReview result = controller.getDepositReview("userId", "depositReviewId");
        assertThat(result).isEqualTo(depositReview);
        
        verify(mDepositsReviewService).getDepositReview("depositReviewId");

        verifyNoMoreInteractions(mUsersService, mVaultsService, mVaultsService,
                mClientsService, mEventService, mVaultReviewService);
    }

}