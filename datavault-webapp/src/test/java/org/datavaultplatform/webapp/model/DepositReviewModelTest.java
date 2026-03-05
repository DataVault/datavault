package org.datavaultplatform.webapp.model;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DepositReviewModelTest {
    
    static final LocalDateTime NOW = LocalDateTime.now();

    DepositReviewModel drm;

    @BeforeEach
    void setup() {
        drm = new DepositReviewModel();
    }

    @Test
    void testNoArgsConstructor() {
        TestUtils.checkHasNoArgsConstructor(DepositReviewModel.class);
    }

    @Nested
    class UpdateFromDepositReviewAndDepositInfoTests {
        
        @Test
        void testNullDepositReview() {
            DepositInfo depositInfo = new DepositInfo();
            var ex = assertThrows(IllegalArgumentException.class, () -> drm.updateFromDepositReviewAndDepositInfo(null, depositInfo));
            assertThat(ex).hasMessage("The depositReview cannot be null");
        }

        @Test
        void testNullDepositInfo(){
            DepositReview dr = new DepositReview();
            var ex = assertThrows(IllegalArgumentException.class, () -> drm.updateFromDepositReviewAndDepositInfo(dr, null));
            assertThat(ex).hasMessage("The depositInfo cannot be null");
        }

        static Stream<Arguments> depositStatusProvider() {
            return Stream.of(
                    Arguments.of(Deposit.Status.COMPLETE, "COMPLETE"),
                    Arguments.of(null, ""));
        }

        @ParameterizedTest
        @MethodSource("depositStatusProvider")
        void testSuccessfulUpdate(Deposit.Status status, String expectedStatus) {
            DepositReview dr = new DepositReview();
            dr.setId("deposit-review-id");
            dr.setDeleteStatus(123);
            dr.setComment("comment");
            
            
            DepositInfo depositInfo = new DepositInfo();
            depositInfo.setID("deposit-id");
            depositInfo.setName("deposit-name");
            depositInfo.setStatus(status);
            depositInfo.setCreationTime(NOW);

            drm.updateFromDepositReviewAndDepositInfo(dr, depositInfo);
            
            assertThat(drm.getDepositReviewId()).isEqualTo("deposit-review-id");
            assertThat(drm.getDeleteStatus()).isEqualTo(123);
            assertThat(drm.getComment()).isEqualTo("comment");

            assertThat(drm.getDepositId()).isEqualTo("deposit-id");
            assertThat(drm.getName()).isEqualTo("deposit-name");
            assertThat(drm.getStatusName()).isEqualTo(expectedStatus);
            assertThat(drm.getCreationTime()).isEqualTo(NOW);
        }
    }

}