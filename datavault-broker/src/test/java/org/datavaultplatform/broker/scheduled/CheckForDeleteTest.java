package org.datavaultplatform.broker.scheduled;

import lombok.SneakyThrows;
import org.datavaultplatform.broker.services.AdminDepositService;
import org.datavaultplatform.broker.services.DepositsReviewService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.datavaultplatform.common.model.DepositReviewDeleteStatus.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CheckForDeleteTest {

    @Mock
    DepositsReviewService mDepositsReviewService;

    @Mock
    AdminDepositService mAdminDepositService;

    @Mock
    VaultsService mVaultsService;

    @Mock
    Vault mVault;
    
    static final Clock CLOCK = Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneId.systemDefault());

    CheckForDelete spyCheckForDelete;


    @BeforeEach
    void setup() {
        spyCheckForDelete = spy(new CheckForDelete(mVaultsService, mDepositsReviewService, mAdminDepositService, CLOCK));
        lenient().when(mVault.getID()).thenReturn("mVault-id");
        lenient().when(mVault.getName()).thenReturn("mVault-name");
    }

    /**
     * A Stream of arguments that do not result in 'spyCheckForDelete.checkVaultForDelete' being called.
     */
    static Stream<Arguments> noVaultsProvider() {
        return Stream.of(
                Arguments.of((List<Vault>) null), 
                Arguments.of(List.of()), 
                Arguments.of(Collections.singletonList(null)));
    }
    
    @Order(1)
    @SneakyThrows
    @ParameterizedTest
    @MethodSource("noVaultsProvider")
    void testExecuteCallsCheckVaultForDeleteWithoutVaults(List<Vault> vaults) {
        
        when(mVaultsService.getVaults()).thenReturn(vaults);

        spyCheckForDelete.execute();
        
        verify(mVaultsService).getVaults();

        verify(spyCheckForDelete, never()).checkVaultForDelete(any(Vault.class), any(LocalDate.class));

        verifyNoMoreInteractions(mVaultsService, mDepositsReviewService, mAdminDepositService);
    }

    @Test
    @Order(2)
    @SneakyThrows
    void testExecuteCallsCheckVaultForDeleteWithMultipleVaults() {

        ArgumentCaptor<Vault> argVault = ArgumentCaptor.forClass(Vault.class);
        
        Vault vault1 = new Vault();
        Vault vault2 = new Vault();

        when(mVaultsService.getVaults()).thenReturn(Arrays.asList(vault1, null, vault2));

        spyCheckForDelete.execute();
        
        verify(spyCheckForDelete,times(2)).checkVaultForDelete(argVault.capture(), eq(LocalDate.now(CLOCK)));

        verify(mVaultsService).getVaults();
        verifyNoMoreInteractions(mVaultsService, mDepositsReviewService, mAdminDepositService);
    }

    /**
     * A set of arguments that do not result in 'spyCheckForDelete.checkActionedDepositReview' being called.
     * @see #testExecuteCallsCheckVaultForDeleteWithSingleVaultButNoReviews
     */
    static Stream<Arguments> vaultReviewsProvider() {
        return Stream.of(
                Arguments.of((List<VaultReview>) null), Arguments.of(List.of()));
    }

    @Order(3)
    @SneakyThrows
    @ParameterizedTest
    @MethodSource("vaultReviewsProvider")
    void testExecuteCallsCheckVaultForDeleteWithSingleVaultButNoReviews(List<VaultReview> vaultReviews) {

        when(mVault.getVaultReviews()).thenReturn(vaultReviews);

        when(mVaultsService.getVaults()).thenReturn(List.of(mVault));

        spyCheckForDelete.execute();

        verify(spyCheckForDelete).checkVaultForDelete(mVault, LocalDate.now(CLOCK));
        
        verify(mVaultsService).getVaults();
        verify(mVault).getVaultReviews();
        
        verify(spyCheckForDelete, never()).checkActionedDepositReview(any(), any(), any(), any());
        
        verifyNoMoreInteractions(mVaultsService, mDepositsReviewService, mAdminDepositService, mVault);
    }

    
    static Stream<Arguments> optionalVaultReviewWithoutNonNullDepositReviewSource() {
        VaultReview actionedDateButNullDepositReviewsList = new VaultReview();
        actionedDateButNullDepositReviewsList.setActionedDate(LocalDateTime.now(CLOCK));
        actionedDateButNullDepositReviewsList.setDepositReviews(null);

        VaultReview actionedDateButEmptyDepositReviews = new VaultReview();
        actionedDateButEmptyDepositReviews.setActionedDate(LocalDateTime.now(CLOCK));
        actionedDateButEmptyDepositReviews.setDepositReviews(List.of());

        VaultReview actionedDateButNullDepositReviews = new VaultReview();
        actionedDateButNullDepositReviews.setActionedDate(LocalDateTime.now(CLOCK));
        actionedDateButNullDepositReviews.setDepositReviews(Arrays.asList(null, null, null));

        VaultReview actionedDateButDepositReviewWithoutActionedDate = new VaultReview();
        actionedDateButDepositReviewWithoutActionedDate.setActionedDate(LocalDateTime.now(CLOCK));
        DepositReview depositReviewWithActionedDate = new DepositReview();
        depositReviewWithActionedDate.setActionedDate(LocalDateTime.now(CLOCK));
        depositReviewWithActionedDate.setDeposit(new Deposit());
        actionedDateButDepositReviewWithoutActionedDate.setDepositReviews(List.of(depositReviewWithActionedDate));

        return Stream.of(
                Arguments.of((VaultReview) null),
                Arguments.of(new VaultReview()),
                Arguments.of(actionedDateButNullDepositReviewsList),
                Arguments.of(actionedDateButEmptyDepositReviews),
                Arguments.of(actionedDateButNullDepositReviews),
                Arguments.of(actionedDateButDepositReviewWithoutActionedDate));
    }

    /**
     * @param mostRecentVaultReview - VaultReview that doesn't contain any non-null DepositReviews
     */
    @Order(4)
    @SneakyThrows
    @ParameterizedTest
    @MethodSource("optionalVaultReviewWithoutNonNullDepositReviewSource")
    void testExecuteCallsCheckVaultForDeleteWithSingleVaultButMostRecentVaultReviewHasNoActionedDateOrNoDepositReviewsWithoutActionedDate(VaultReview mostRecentVaultReview) {

        VaultReview vaultReview = new VaultReview();
        when(mVault.getVaultReviews()).thenReturn(List.of(vaultReview));
        when(mVault.getMostRecentVaultReview()).thenReturn(Optional.ofNullable(mostRecentVaultReview));

        when(mVaultsService.getVaults()).thenReturn(List.of(mVault));

        spyCheckForDelete.execute();

        verify(spyCheckForDelete).checkVaultForDelete(mVault, LocalDate.now(CLOCK));

        verify(mVaultsService).getVaults();
        verify(mVault).getVaultReviews();
        verify(mVault, atLeastOnce()).getID();
        verify(mVault, atLeastOnce()).getName();

        verifyNoMoreInteractions(mVaultsService, mDepositsReviewService, mAdminDepositService, mVault);
    }


    /**
     * a set of arguments that do not result in 'adminDepositService.deleteDeposit' being called.
     * @see #testExecuteWhereOneVaultReviewThatHasDepositReviewWithoutActionDateButDepositReviewCanBeIgnored
     */
    static Stream<Arguments> ignoredDepositReviewSource() {
        DepositReview drNow = new DepositReview();
        drNow.setDeleteStatus(NOW);
        drNow.setDeposit(new Deposit());

        DepositReview drOnReview = new DepositReview();
        drOnReview.setDeleteStatus(ONREVIEW);
        drOnReview.setDeposit(new Deposit());
        
        DepositReview drOnExpiry = new DepositReview();
        drOnExpiry.setDeleteStatus(ONEXPIRY);
        drOnExpiry.setDeposit(new Deposit());
        
        LocalDate today = LocalDate.now(CLOCK);
        LocalDate beforeToday = today.minusDays(1);
        LocalDate afterToday = today.plusDays(1);
        
        return Stream.of(
                Arguments.of(drNow, null, null),
                Arguments.of(drNow, beforeToday, null),
                Arguments.of(drNow, today, null),
                Arguments.of(drNow, afterToday, null),

                Arguments.of(drOnReview, null, null),
                Arguments.of(drOnReview, today, null),
                Arguments.of(drOnReview, afterToday, null),

                Arguments.of(drOnExpiry, null, null),
                Arguments.of(drOnExpiry, null, DateTimeUtils.toLocalDateTimeAtMidnight(afterToday)),
                Arguments.of(drOnExpiry, null, DateTimeUtils.toLocalDateTimeAtMidnight(today))
                );
    }


    @ParameterizedTest
    @MethodSource("ignoredDepositReviewSource")
    @Order(5)
    @SneakyThrows
    void testExecuteWhereOneVaultReviewThatHasDepositReviewWithoutActionDateButDepositReviewCanBeIgnored(DepositReview depositReview, LocalDate oldVaultReviewDate, LocalDateTime retentionPolicyExpiry) {

        VaultReview vaultReview = new VaultReview();
        vaultReview.setOldReviewDate(oldVaultReviewDate);

        VaultReview mostRecentVaultReview = new VaultReview();
        mostRecentVaultReview.setActionedDate(LocalDateTime.now(CLOCK));
        mostRecentVaultReview.setOldReviewDate(oldVaultReviewDate);
        mostRecentVaultReview.setDepositReviews(List.of(depositReview));
        
        when(mVault.getVaultReviews()).thenReturn(List.of(vaultReview));
        when(mVault.getMostRecentVaultReview()).thenReturn(Optional.of(mostRecentVaultReview));
        lenient().when(mVault.getRetentionPolicyExpiry()).thenReturn(retentionPolicyExpiry);

        when(mVaultsService.getVaults()).thenReturn(List.of(mVault));

        spyCheckForDelete.execute();

        verify(spyCheckForDelete).checkVaultForDelete(mVault, LocalDate.now(CLOCK));

        verify(mVaultsService).getVaults();
        verify(mVault).getVaultReviews();
        verify(mVault, atLeastOnce()).getID();
        verify(mVault, atLeastOnce()).getName();
        verify(mVault, atLeast(0)).getRetentionPolicyExpiry(); // might be called

        verify(mAdminDepositService, never()).deleteDeposit(any(), any());
        verify(mDepositsReviewService, never()).updateDepositReview(any());

        verifyNoMoreInteractions(mVaultsService, mDepositsReviewService, mAdminDepositService, mVault);
    }

    /**
     * A set of arguments that result in mAdminDepositService.deleteDeposit being called
     *
     * @see #testExecuteWhereOneVaultReviewThatHasDepositReviewWithoutActionDateAndDepositReviewWillCauseDepositDeletion
     */
    static Stream<Arguments> depositReviewsThatCauseDeletionSource() {
        DepositReview drOnReview = new DepositReview();
        drOnReview.setDeleteStatus(ONREVIEW);
        drOnReview.setDeposit(new Deposit());

        DepositReview drOnExpiry = new DepositReview();
        drOnExpiry.setDeleteStatus(ONEXPIRY);
        drOnExpiry.setDeposit(new Deposit());

        LocalDate today = LocalDate.now(CLOCK);
        LocalDate beforeToday = today.minusDays(1);

        return Stream.of(
                Arguments.of(drOnReview, beforeToday, null),
                Arguments.of(drOnExpiry, null, DateTimeUtils.toLocalDateTimeAtMidnight(beforeToday))
        );
    }

    @ParameterizedTest
    @MethodSource("depositReviewsThatCauseDeletionSource")
    @Order(6)
    @SneakyThrows
    void testExecuteWhereOneVaultReviewThatHasDepositReviewWithoutActionDateAndDepositReviewWillCauseDepositDeletion(DepositReview depositReview, LocalDate oldVaultReviewDate, LocalDateTime retentionPolicyExpiry) {

        VaultReview vaultReview = new VaultReview();
        vaultReview.setOldReviewDate(oldVaultReviewDate);

        VaultReview mostRecentVaultReview = new VaultReview();
        mostRecentVaultReview.setActionedDate(LocalDateTime.now(CLOCK));
        mostRecentVaultReview.setOldReviewDate(oldVaultReviewDate);
        mostRecentVaultReview.setDepositReviews(List.of(depositReview));

        when(mVault.getVaultReviews()).thenReturn(List.of(vaultReview));
        when(mVault.getMostRecentVaultReview()).thenReturn(Optional.of(mostRecentVaultReview));
        lenient().when(mVault.getRetentionPolicyExpiry()).thenReturn(retentionPolicyExpiry);

        when(mVaultsService.getVaults()).thenReturn(List.of(mVault));

        spyCheckForDelete.execute();

        verify(spyCheckForDelete).checkVaultForDelete(mVault, LocalDate.now(CLOCK));

        verify(mVaultsService).getVaults();
        verify(mVault).getVaultReviews();
        verify(mVault, atLeastOnce()).getID();
        verify(mVault, atLeastOnce()).getName();
        int count = retentionPolicyExpiry == null ? 0 : 1;
        verify(mVault, times(count)).getRetentionPolicyExpiry();
        
        verify(mAdminDepositService).deleteDeposit(depositReview.getDeposit(), null);
        verify(mDepositsReviewService).updateDepositReview(depositReview);
        
        verifyNoMoreInteractions(mVaultsService, mDepositsReviewService, mAdminDepositService, mVault);
    }
}