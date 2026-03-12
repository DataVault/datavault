package org.datavaultplatform.common.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vault is primarily a data class, but it now has some methods that are more complex that getters/setters
 */
class VaultTest {

    private static final LocalDateTime NOW = LocalDateTime.now();
    
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetMostRecentVaultReviewTests {

        static Stream<Arguments> noVaultReviewProvider() {
            return Stream.of(
                    Arguments.of((List<VaultReview>) null),
                    Arguments.of(List.of()),
                    Arguments.of(Collections.singletonList((VaultReview) null)));
        }

        @Order(1)
        @ParameterizedTest
        @MethodSource("noVaultReviewProvider")
        void testNoVaultReviews(List<VaultReview> vaultReviewList){
            Vault vault = new Vault();
            vault.setVaultReviews(vaultReviewList);
            assertThat(vault.getMostRecentVaultReview()).isNotPresent();
        }

        @Test
        @Order(2)
        void testVaultReviewWithNullCreationTimes() {
            VaultReview vaultReview1 = new VaultReview();
            vaultReview1.setId("123");

            VaultReview vaultReview2 = new VaultReview();
            vaultReview2.setId("222");
            vaultReview2.setCreationTime(NOW);
            Vault vault = new Vault();
            vault.setVaultReviews(List.of(vaultReview1, vaultReview2));

            assertThat(vault.getMostRecentVaultReview())
                    .isPresent()
                    .contains(vaultReview2);
        }

        @Test
        @Order(3)
        void testMostRecentVaultReviewHasMaxCreationTime() {
            VaultReview vaultReview1 = new VaultReview();
            vaultReview1.setId("123");
            vaultReview1.setCreationTime(NOW.minusNanos(2));

            VaultReview vaultReview2 = new VaultReview();
            vaultReview2.setId("222");
            vaultReview2.setCreationTime(NOW);

            VaultReview vaultReview3 = new VaultReview();
            vaultReview3.setId("33");
            vaultReview3.setCreationTime(NOW.minusNanos(1));
            
            Vault vault = new Vault();
            vault.setVaultReviews(Arrays.asList(vaultReview1, null, vaultReview2, null, vaultReview3));
            assertThat(vault.getMostRecentVaultReview())
                    .isPresent()
                    .contains(vaultReview2);
        }

        /**
         * If there are 2 VaultReviews with exactly the same creationTime,
         * the first will be used and there will not be an error.
         */
        @Test
        @Order(4)
        void testMostRecentVaultReviewHasFirstMaxCreationTime() {
            VaultReview vaultReview1 = new VaultReview();
            vaultReview1.setId("123");
            vaultReview1.setCreationTime(NOW.minusNanos(2));

            VaultReview vaultReview2 = new VaultReview();
            vaultReview2.setId("222");
            vaultReview2.setCreationTime(NOW);

            VaultReview vaultReview3 = new VaultReview();
            vaultReview3.setId("333");
            vaultReview3.setCreationTime(NOW.minusNanos(1));

            VaultReview vaultReview4 = new VaultReview();
            vaultReview4.setId("444");
            vaultReview4.setCreationTime(NOW);
            
            assertThat(vaultReview2.getCreationTime()).isEqualTo(vaultReview4.getCreationTime());

            Vault vault = new Vault();
            vault.setVaultReviews(Arrays.asList(vaultReview1, null, vaultReview2, null, vaultReview3, vaultReview4));
            assertThat(vault.getMostRecentVaultReview())
                    .isPresent()
                    .contains(vaultReview2);
        }
    }
    
    
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FindLatestVaultReviewIfStillUnderwayTests {

        Vault vault = new Vault();

        @Test
        void testNoVaultReviews() {
            assertThat(vault.findLatestVaultReviewIfStillUnderway()).isNotPresent();
        }

        @Test
        void testLatestVaultReviewIsNotUnderway() {
            VaultReview vaultReview = new VaultReview();
            vaultReview.setCreationTime(NOW.minusDays(2));
            vaultReview.setActionedDate(NOW.minusDays(1));
            
            vault.setVaultReviews(List.of(vaultReview));
            assertThat(vault.findLatestVaultReviewIfStillUnderway()).isNotPresent();
        }

        @Test
        void testLatestVaultReviewIsUnderway() {
            VaultReview vaultReview = new VaultReview();
            vaultReview.setCreationTime(NOW.minusDays(2));

            vault.setVaultReviews(List.of(vaultReview));
            assertThat(vault.findLatestVaultReviewIfStillUnderway()).isPresent().contains(vaultReview);
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class IsVaultReviewUnderwayTests {

        Vault vault = new Vault();

        @Test
        void testNoVaultReviews() {
            assertThat(vault.isVaultReviewUnderway()).isFalse();
        }

        @Test
        void testLatestVaultReviewIsNotUnderway() {
            VaultReview vaultReview = new VaultReview();
            vaultReview.setCreationTime(NOW.minusDays(2));
            vaultReview.setActionedDate(NOW.minusDays(1));

            vault.setVaultReviews(List.of(vaultReview));
            assertThat(vault.isVaultReviewUnderway()).isFalse();
        }

        @Test
        void testLatestVaultReviewIsUnderway() {
            VaultReview vaultReview = new VaultReview();
            vaultReview.setCreationTime(NOW.minusDays(2));

            vault.setVaultReviews(List.of(vaultReview));
            assertThat(vault.isVaultReviewUnderway()).isTrue();
        }
    }

}