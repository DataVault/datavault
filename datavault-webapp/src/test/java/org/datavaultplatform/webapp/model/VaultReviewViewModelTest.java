package org.datavaultplatform.webapp.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VaultReviewViewModelTest {

    static Stream<Arguments> settersAndGettersProviders() {
        DepositReviewViewModel drvm1 = new DepositReviewViewModel();
        DepositReviewViewModel drvm2 = new DepositReviewViewModel();
        return Stream.of(
                Arguments.of(null, List.of()),
                Arguments.of(List.of(drvm1, drvm2), List.of(drvm1, drvm2)));
    }

    @ParameterizedTest
    @MethodSource("settersAndGettersProviders")
    void testSettersAndGetters(List<DepositReviewViewModel> models, List<DepositReviewViewModel> expectedModels) {
        LocalDateTime now = LocalDateTime.now();
        var vrvm = new VaultReviewViewModel();

        vrvm.setActionedDate(now);
        vrvm.setComment("comment");
        vrvm.setCreationTime(now.plusHours(3));

        vrvm.setDepositReviewViewModels(models);

        vrvm.setCurrentVaultReviewDate(now.plusMonths(1).toLocalDate());
        vrvm.setOldReviewDate(now.plusHours(2).toLocalDate());
        vrvm.setVaultReviewId("vault-review-id");

        assertNotNull(vrvm);

        assertThat(vrvm.getActionedDate()).isEqualTo(now);
        assertThat(vrvm.getComment()).isEqualTo("comment");
        assertThat(vrvm.getCreationTime()).isEqualTo(now.plusHours(3));

        assertThat(vrvm.getDepositReviewViewModels()).isEqualTo(expectedModels);

        assertThat(vrvm.getCurrentVaultReviewDate()).isEqualTo(now.plusMonths(1).toLocalDate());
        assertThat(vrvm.getOldReviewDate()).isEqualTo(now.plusHours(2).toLocalDate());
        assertThat(vrvm.getVaultReviewId()).isEqualTo("vault-review-id");
    }
}