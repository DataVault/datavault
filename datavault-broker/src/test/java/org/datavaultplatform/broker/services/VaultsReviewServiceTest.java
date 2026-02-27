package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.VaultReviewDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultsReviewServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2007-12-03T12:00:00.00Z"), ZoneOffset.UTC);

    VaultsReviewService vaultsReviewService;

    @Mock
    VaultReviewDAO mVaultReviewDAO;

    @Mock
    DepositsReviewService mDepositReviewService;

    @BeforeEach
    void setup() {
        vaultsReviewService = new VaultsReviewService(mVaultReviewDAO, mDepositReviewService, CLOCK);
    }

    @Test
    void testSearch() {
        String query = "query";
        List<VaultReview> reviews = List.of(new VaultReview());
        when(mVaultReviewDAO.search(anyString())).thenReturn(reviews);
        List<VaultReview> vaultReviews = vaultsReviewService.search(query);

        assertThat(vaultReviews).isEqualTo(reviews);
        verify(mVaultReviewDAO).search(query);
        verifyNoMoreInteractions(mVaultReviewDAO, mDepositReviewService);

    }

    @ParameterizedTest
    @ValueSource(strings = "vaultId")
    @NullSource
    void testFindByVaultId(String vaultId) {
        vaultsReviewService.findByVaultId(vaultId);
        verify(mVaultReviewDAO).findByVaultId(vaultId);
        verifyNoMoreInteractions(mVaultReviewDAO, mDepositReviewService);
    }

    @Test
    void testUpdateVaultReview() {
        VaultReview vaultReview = new VaultReview();
        doAnswer(invocation -> invocation.getArgument(0)).when(mVaultReviewDAO).update(any(VaultReview.class));
        vaultsReviewService.updateVaultReview(vaultReview);

        verify(mVaultReviewDAO).update(vaultReview);
        verifyNoMoreInteractions(mVaultReviewDAO, mDepositReviewService);
    }

    @Nested
    class DueForReviewEmailTests {

        @ParameterizedTest(name = "Review due in {0}mo, last actioned {1}mo ago (actioned: {2}) -> expected: {3}")
        @CsvSource(textBlock = """
                0,  true,   -9, true
                -1, true,   0,  false
                1,  true,   -9, true
                12, true,   -9, false
                """)
        void testDueForReviewEmail(int reviewOffsetMonths, boolean isActioned, int actionedOffsetMonths, boolean expected) {
            LocalDateTime now = LocalDateTime.now(CLOCK);

            Vault vault = new Vault();
            vault.setReviewDate(now.plusMonths(reviewOffsetMonths).toLocalDate());

            VaultReview review = new VaultReview();
            vault.setVaultReviews(List.of(review));
            if (isActioned) {
                review.setActionedDate(now.plusMonths(actionedOffsetMonths));
            }

            boolean dueForReviewEmail = vaultsReviewService.dueForReviewEmail(vault);
            assertThat(dueForReviewEmail).isEqualTo(expected);
        }

        @Test
        void testNullVault() {
            boolean dueForReview = vaultsReviewService.dueForReviewEmail(null);
            assertThat(dueForReview).isFalse();
        }

        @Test
        void testVaultHasNoReviewDate() {
            Vault vault = new Vault();
            boolean dueForReview = vaultsReviewService.dueForReviewEmail(vault);
            assertThat(dueForReview).isFalse();
        }

        @Test
        void testTodayBeforeStartOfReviewWindow() {
            Vault vault = new Vault();
            // this makes the today before the start of the 'review window' which is (review date -6 months)
            vault.setReviewDate(LocalDate.now(CLOCK).plusMonths(7));
            boolean dueForReview = vaultsReviewService.dueForReviewEmail(vault);
            assertThat(dueForReview).isFalse();
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
                true, true, false
                true, false, false
                false, false, false
                false, true, true 
                """)
            // note: we only expectReview if no current review exists and reviewHasNotHappenedAfterReviewWindowStart is true
        void testTodayNotBeforeStartOfReviewWindow(boolean currentReviewExists, boolean reviewHasNotHappenedAfterReviewWindowStart, boolean reviewExpected) {


            LocalDate reviewDate = LocalDate.now(CLOCK).plusMonths(3);
            LocalDate reviewWindowStartDate = reviewDate.minusMonths(6);

            List<VaultReview> vaultReviews = new ArrayList<>();
            VaultReview completedReview = new VaultReview();
            completedReview.setCreationTime(LocalDateTime.now(CLOCK).minusDays(3));
            vaultReviews.add(completedReview);
            if (reviewHasNotHappenedAfterReviewWindowStart) {
                completedReview.setActionedDate(reviewWindowStartDate.minusDays(1).atStartOfDay());
            } else {
                completedReview.setActionedDate(reviewWindowStartDate.plusDays(1).atStartOfDay());
            }

            if (currentReviewExists) {
                VaultReview currentReview = new VaultReview();
                currentReview.setActionedDate(null);
                currentReview.setCreationTime(LocalDateTime.now(CLOCK).minusDays(1));
                vaultReviews.add(currentReview);
            }
            Vault vault = new Vault();
            vault.setVaultReviews(vaultReviews);

            // this makes the today after the start of the 'review window' which is (review date -6 months)
            vault.setReviewDate(reviewDate);

            boolean dueForReview = vaultsReviewService.dueForReviewEmail(vault);
            assertThat(dueForReview).isEqualTo(reviewExpected);

        }
    }

    @Nested
    class DueForReviewTests {

        @ParameterizedTest(name = "{index} ==> ReviewOffset: {0}mo, ActionedOffset: {1}mo, Expected: {2}")
        @CsvSource(textBlock = """
                0,  true, -9, true
                -1, true, 0,  false
                1,  true, -9, true
                12, true, -9, false
                12, false, 0, false
                0,  false, 0, true
                1,  false, 0, true
                6,  false, 0, true
                7,  false, 0, false
                """)
        void testIsVaultForReview(int reviewOffsetMonths, boolean actioned, int actionedOffsetMonths, boolean expected) {
            LocalDateTime today = LocalDateTime.now(CLOCK);

            LocalDate reviewLocalDate = today.plusMonths(reviewOffsetMonths).toLocalDate();

            VaultReview vaultReview = new VaultReview();
            if (actioned) {
                vaultReview.setActionedDate(today.plusMonths(actionedOffsetMonths));
            }

            Vault vault = new Vault();
            vault.setReviewDate(reviewLocalDate);
            vault.setVaultReviews(List.of(vaultReview));

            boolean vaultForReview = vaultsReviewService.isVaultForReview(vault);

            assertThat(vaultForReview).isEqualTo(expected);
        }
        
        /** here **/

        @Test
        void testNullVault() {
            boolean vaultForReview = vaultsReviewService.isVaultForReview(null);
            assertThat(vaultForReview).isFalse();
        }

        @Test
        void testVaultHasNoReviewDate() {
            Vault vault = new Vault();
            boolean vaultForReview = vaultsReviewService.isVaultForReview(vault);
            assertThat(vaultForReview).isFalse();
        }

        @Test
        void testTodayBeforeStartOfReviewWindow() {
            Vault vault = new Vault();
            // this makes the today before the start of the 'review window' which is (review date -6 months)
            vault.setReviewDate(LocalDate.now(CLOCK).plusMonths(7));
            boolean vaultForReview = vaultsReviewService.isVaultForReview(vault);
            assertThat(vaultForReview).isFalse();
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
                false, false
                true, true 
                """)
            // note: we only expectReview if reviewHasNotHappenedAfterReviewWindowStart is true
        void testTodayNotBeforeStartOfReviewWindow(boolean reviewHasNotHappenedAfterReviewWindowStart, boolean reviewExpected) {


            LocalDate reviewDate = LocalDate.now(CLOCK).plusMonths(3);
            LocalDate reviewWindowStartDate = reviewDate.minusMonths(6);

            List<VaultReview> vaultReviews = new ArrayList<>();
            VaultReview completedReview = new VaultReview();
            completedReview.setCreationTime(LocalDateTime.now(CLOCK).minusDays(3));
            vaultReviews.add(completedReview);
            if (reviewHasNotHappenedAfterReviewWindowStart) {
                completedReview.setActionedDate(reviewWindowStartDate.minusDays(1).atStartOfDay());
            } else {
                completedReview.setActionedDate(reviewWindowStartDate.plusDays(1).atStartOfDay());
            }

            Vault vault = new Vault();
            vault.setVaultReviews(vaultReviews);

            // this makes the today after the start of the 'review window' which is (review date -6 months)
            vault.setReviewDate(reviewDate);

            boolean vaultForReview = vaultsReviewService.isVaultForReview(vault);
            assertThat(vaultForReview).isEqualTo(reviewExpected);

        }
    }

    @Nested
    class CreateVaultReviewTests {

        @Captor
        ArgumentCaptor<VaultReview> argVaultReview1;

        @Captor
        ArgumentCaptor<Vault> argValue2;

        @Captor
        ArgumentCaptor<VaultReview> argVaultReview2;

        @Test
        void testCreateVaultReviewArgs() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                vaultsReviewService.createVaultReview(null);
            });
            assertThat(ex).hasMessage("The vault cannot be null");
        }

        @Test
        void testCreateVaultReview() {
            Vault vault = new Vault();

            VaultReview vaultReview = vaultsReviewService.createVaultReview(vault);
            assertThat(vaultReview).isNotNull();
            assertThat(vaultReview.getCreationTime()).isEqualTo(LocalDateTime.now(CLOCK));
            assertThat(vaultReview.getVault()).isEqualTo(vault);

            verify(mVaultReviewDAO).save(argVaultReview1.capture());
            verify(mDepositReviewService).addDepositReviews(argValue2.capture(), argVaultReview2.capture());

            verifyNoMoreInteractions(mVaultReviewDAO, mDepositReviewService);

            assertThat(argVaultReview1.getValue()).isEqualTo(vaultReview);
            assertThat(argValue2.getValue()).isEqualTo(vault);
            assertThat(argVaultReview2.getValue()).isEqualTo(vaultReview);
        }
    }

    @Nested
    class ConstructorTests {

        @Test
        void testNullClock() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new VaultsReviewService(mVaultReviewDAO, mDepositReviewService, null);
            });
            assertThat(ex).hasMessage("clock cannot be null");
        }

        @Test
        void testNullDepositReviewService() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new VaultsReviewService(mVaultReviewDAO, null, CLOCK);
            });
            assertThat(ex).hasMessage("depositsReviewService cannot be null");
        }

        @Test
        void testNullVaultReviewDAO() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new VaultsReviewService(null, mDepositReviewService, CLOCK);
            });
            assertThat(ex).hasMessage("vaultReviewDAO cannot be null");
        }
    }

    @Nested
    class GetVaultReviewTests {

        @Test
        void testFound() {
            VaultReview vaultReview = new VaultReview();
            when(mVaultReviewDAO.findById("ID")).thenReturn(Optional.of(vaultReview));

            VaultReview result = vaultsReviewService.getVaultReview("ID");
            assertThat(result).isEqualTo(vaultReview);

            verify(mVaultReviewDAO).findById("ID");
            verifyNoMoreInteractions(mVaultReviewDAO, mDepositReviewService);
        }

        @Test
        void testNotFound() {
            when(mVaultReviewDAO.findById("ID")).thenReturn(Optional.empty());

            VaultReview result = vaultsReviewService.getVaultReview("ID");
            assertThat(result).isNull();

            verify(mVaultReviewDAO).findById("ID");
            verifyNoMoreInteractions(mVaultReviewDAO, mDepositReviewService);
        }
    }

    @Nested
    class GetVaultsForReviewTests {

        @Test
        void shouldFilterVaultsCorrectly() {
            // Spy allows us to mock internal method calls
            VaultsReviewService spy = Mockito.spy(new VaultsReviewService(mVaultReviewDAO, mDepositReviewService, CLOCK));
            Vault vault1 = new Vault(); // Should be included
            Vault vault2 = new Vault(); // Should be filtered out
            Vault vault3 = new Vault(); // Should be included

            List<Vault> inputList = Arrays.asList(vault1, vault2, vault3, null);

            // Stub the internal method call for specific objects
            doReturn(true).when(spy).isVaultForReview(vault1);
            doReturn(false).when(spy).isVaultForReview(vault2);
            doReturn(true).when(spy).isVaultForReview(vault3);

            List<Vault> result = spy.getVaultsForReview(inputList);

            assertThat(result).hasSize(2)
                    .contains(vault1)
                    .doesNotContain(vault2)
                    .contains(vault3);

            verify(spy, times(3)).isVaultForReview(any(Vault.class));
        }

        @Test
        void shouldReturnEmptyListWhenInputIsNull() {
            List<Vault> result = vaultsReviewService.getVaultsForReview(null);
            assertThat(result).isEmpty();
        }
    }

}
