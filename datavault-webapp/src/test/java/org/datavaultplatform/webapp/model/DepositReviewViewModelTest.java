package org.datavaultplatform.webapp.model;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.DepositReviewDeleteStatus;
import org.datavaultplatform.common.response.DepositInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DepositReviewViewModelTest {

    static Stream<Arguments> depositStatusProvider() {
        return Stream.of(
                Arguments.of(DepositReviewDeleteStatus.NOW, Deposit.Status.COMPLETE),
                Arguments.of(DepositReviewDeleteStatus.RETAIN, Deposit.Status.DELETED),
                Arguments.of(DepositReviewDeleteStatus.ONREVIEW, Deposit.Status.IN_PROGRESS),
                Arguments.of(DepositReviewDeleteStatus.ONEXPIRY, Deposit.Status.DELETE_IN_PROGRESS),
                Arguments.of(DepositReviewDeleteStatus.NOW, Deposit.Status.FAILED),
                Arguments.of(DepositReviewDeleteStatus.RETAIN, Deposit.Status.DELETE_FAILED),
                Arguments.of(DepositReviewDeleteStatus.ONREVIEW, Deposit.Status.NOT_STARTED),
                Arguments.of(DepositReviewDeleteStatus.ONEXPIRY, null)
        );
    }

    @ParameterizedTest
    @MethodSource("depositStatusProvider")
    void testConstructor(int deleteStatus, Deposit.Status depositStatus) {
        LocalDateTime now = LocalDateTime.now();
        DepositReview depositReview = new DepositReview();
        depositReview.setId("deposit-review-id");
        depositReview.setComment("comment");
        depositReview.setDeleteStatus(123);
        depositReview.setActionedDate(now);
        depositReview.setDeleteStatus(deleteStatus);
        depositReview.setDeposit(null);
        depositReview.setCreationTime(now.plusHours(1));

        DepositInfo depositInfo = new DepositInfo();
        depositInfo.setCreationTime(now.plusHours(2));
        depositInfo.setName("info-name");
        depositInfo.setID("deposit-id");
        depositInfo.setStatus(depositStatus);

        var result = new DepositReviewViewModel(depositReview, depositInfo);

        assertThat(result.getDepositId()).isEqualTo("deposit-id");
        assertThat(result.getDepositReviewId()).isEqualTo("deposit-review-id");
        assertThat(result.getComment()).isEqualTo("comment");
        assertThat(result.getDeleteStatus()).isEqualTo(deleteStatus);
        assertThat(result.getActionedDate()).isEqualTo(now);
        assertThat(result.getDepositCreationTime()).isEqualTo(now.plusHours(2));
        assertThat(result.getDepositName()).isEqualTo("info-name");
        String depositStatusStr = depositStatus == null ? "" : depositStatus.toString();
        assertThat(result.getDepositStatusName()).isEqualTo(depositStatusStr);

    }

    @Nested
    class ConstructorArgsTests {
        @Test
        void testNullDepositReview() {
            DepositInfo depositInfo = new DepositInfo();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new DepositReviewViewModel(null, depositInfo);
            });
            assertThat(ex).hasMessage("The depositReview cannot be null");
        }

        @Test
        void testNullDepositInfo() {
            DepositReview depositReview = new DepositReview();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new DepositReviewViewModel(depositReview, null);
            });
            assertThat(ex).hasMessage("The depositInfo cannot be null");
        }

        @Test
        void testNullDepositReviewId() {
            DepositReview depositReview = new DepositReview();
            DepositInfo depositInfo = new DepositInfo();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new DepositReviewViewModel(depositReview, depositInfo);
            });
            assertThat(ex).hasMessage("The depositReview Id cannot be null");
        }
    }
    
    @Test
    void testSortListOfDepositReviewViewModelsByDepositCreationTime() {
        
        List<DepositReviewViewModel> items = new ArrayList<>();

        DepositReview depositReview1 = new DepositReview();
        depositReview1.setId("deposit-review-id-1");
        DepositInfo info1 = new DepositInfo();
        info1.setCreationTime(LocalDateTime.now());
        
        DepositReview depositReview2 = new DepositReview();
        depositReview2.setId("deposit-review-id-2");
        DepositInfo info2 = new DepositInfo();
        info2.setCreationTime(LocalDateTime.now().minusDays(7));

        var drvm1 = new DepositReviewViewModel(depositReview1, info1);
        var drvm2 = new DepositReviewViewModel(depositReview2, info2);
        items.add(drvm2);
        items.add(null);
        items.add(drvm1);
        
        assertThat(items).hasSize(3);

        items.sort(DepositReviewViewModel.BY_DEPOSIT_CREATION_TIME);
        assertThat(items).hasSize(3);
        
        assertThat(items.get(0)).isEqualTo(null);
        assertThat(items.get(1)).isEqualTo(drvm2);
        assertThat(items.get(2)).isEqualTo(drvm1);

    }
}