package org.datavaultplatform.webapp.model;

import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.datavaultplatform.common.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class VaultReviewModelTest {

    static final LocalDate NOW = LocalDate.now();
    
    @Test
    void testNoArgsConstructor() {
        TestUtils.checkHasNoArgsConstructor(VaultReviewModel.class);
    }

    static Stream<Arguments> expiryDateSource() {
        return Stream.of(Arguments.of((LocalDate) null), Arguments.of(NOW));
    }

    @ParameterizedTest
    @MethodSource("expiryDateSource")
    void testNullVaultReviewWithConstructor(LocalDate nextExpiryDate) {
        var vrm = new VaultReviewModel(null, nextExpiryDate);
        assertThat(vrm.getDepositReviewModels()).isEmpty();

        assertThat(vrm.getNextReviewDate()).isEqualTo(nextExpiryDate);

        assertThat(vrm.getVaultReviewId()).isNull();
        assertThat(vrm.getComment()).isNull();
        assertThat(vrm.getActionedDate()).isNull();
    }
    
    @MethodSource("expiryDateSource")
    @ParameterizedTest
    void testVaultReviewWithConstructor(LocalDate nextExpiryDate) {
        VaultReview vr = new VaultReview();
        vr.setComment("comment");
        vr.setId("vault-review-id");
        LocalDateTime actionedDate = DateTimeUtils.toLocalDateTimeAtMidnight(NOW.plusDays(123));
        vr.setActionedDate(actionedDate);

        var vrm = new VaultReviewModel(vr, nextExpiryDate);

        assertThat(vrm.getDepositReviewModels()).isEmpty();

        assertThat(vrm.getNextReviewDate()).isEqualTo(nextExpiryDate);

        assertThat(vrm.getVaultReviewId()).isEqualTo("vault-review-id");
        assertThat(vrm.getComment()).isEqualTo("comment");
        assertThat(vrm.getActionedDate()).isEqualTo(actionedDate);
    }
    
    @Test
    void testSetDepositReviewModels() {
        
        DepositReviewModel drm1 = new DepositReviewModel();
        DepositReviewModel drm2 = new DepositReviewModel();
        DepositReviewModel drm3 = new DepositReviewModel();
        
        VaultReviewModel vrm = new VaultReviewModel();
        assertThat(vrm.getDepositReviewModels()).isEmpty();

        vrm.setDepositReviewModels(List.of(drm1, drm2, drm3));
        assertThat(vrm.getDepositReviewModels()).isEqualTo(List.of(drm1, drm2, drm3));
        
        vrm.setDepositReviewModels(null);
        assertThat(vrm.getDepositReviewModels()).isEmpty();

        vrm.setDepositReviewModels(List.of(drm1, drm2));
        assertThat(vrm.getDepositReviewModels()).isEqualTo(List.of(drm1, drm2));

        vrm.setDepositReviewModels(List.of());
        assertThat(vrm.getDepositReviewModels()).isEmpty();
    }
}