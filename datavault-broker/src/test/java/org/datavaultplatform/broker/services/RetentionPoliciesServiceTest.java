package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetentionPoliciesServiceTest {

    static final Instant TS_1 = Instant.parse("2005-12-03T12:00:00.00Z");
    static final Instant TS_2 = Instant.parse("2006-12-03T12:00:00.00Z");
    static final Instant TS_NOW_3 = Instant.parse("2007-12-03T12:00:00.00Z");
    static final Instant TS_4 = Instant.parse("2008-12-03T12:00:00.00Z");
    static final Instant TS_5 = Instant.parse("2009-12-03T12:00:00.00Z");
    static final Instant TS_6 = Instant.parse("2010-12-03T12:00:00.00Z");

    static final Date DATE_1 = Date.from(TS_1);
    static final Date DATE_2 = Date.from(TS_2);
    static final Date DATE_NOW_3 = Date.from(TS_NOW_3);
    static final Date DATE_4 = Date.from(TS_4);
    static final Date DATE_5 = Date.from(TS_5);
    static final Date DATE_6 = Date.from(TS_6);

    static final Clock CLOCK = Clock.fixed(TS_NOW_3, ZoneOffset.UTC);

    private RetentionPolicy getRetentionPolicy(int minRetentionPeriodYears, boolean extendUponRetrieval) {
        RetentionPolicy rp = new RetentionPolicy();
        rp.setMinRetentionPeriod(minRetentionPeriodYears);
        rp.setExtendUponRetrieval(extendUponRetrieval);
        return rp;
    }

    @Nested
    class RetentionPolicyExpiryDateTests {

        Vault vault;

        static Stream<Arguments> noGrantEndDateProvider() {
            return Stream.of(
                    Arguments.of(DATE_1, RetentionPolicyStatus.REVIEW),
                    Arguments.of(DATE_NOW_3, RetentionPolicyStatus.OK),
                    Arguments.of(DATE_5, RetentionPolicyStatus.OK));
        }

        static Stream<Arguments> grantEndDateProviderNoRetrieves() {
            return Stream.of(
                    Arguments.of(DATE_1, 0, false, RetentionPolicyStatus.REVIEW, DATE_1),
                    Arguments.of(DATE_2, 0, true, RetentionPolicyStatus.REVIEW, DATE_2),
                    Arguments.of(DATE_2, 1, true, RetentionPolicyStatus.OK, DATE_NOW_3));
        }

        static Stream<Arguments> grantEndDateProviderWithRetrieves() {
            return Stream.of(
                    Arguments.of(DATE_1, 0, false, DATE_2, RetentionPolicyStatus.REVIEW, DATE_1),
                    Arguments.of(DATE_1, 0, true, DATE_2, RetentionPolicyStatus.REVIEW, DATE_1),
                    Arguments.of(DATE_1, 1, false, DATE_2, RetentionPolicyStatus.REVIEW, DATE_2),
                    Arguments.of(DATE_1, 1, true, DATE_2, RetentionPolicyStatus.OK, DATE_NOW_3),
                    Arguments.of(DATE_1, 1, true, DATE_NOW_3, RetentionPolicyStatus.OK, DATE_4),
                    Arguments.of(DATE_2, 1, true, DATE_4, RetentionPolicyStatus.OK, DATE_5),
                    Arguments.of(DATE_2, 1, true, DATE_5, RetentionPolicyStatus.OK, DATE_6));
        }

        void validateRetentionPolicyStatus(int retentionPolicyStatus) {
            assertThat(List.of(RetentionPolicyStatus.OK, RetentionPolicyStatus.REVIEW)).contains(retentionPolicyStatus);
        }

        @BeforeEach
        void setup() {
            vault = Mockito.spy(new Vault());
        }

        @ParameterizedTest
        @MethodSource("noGrantEndDateProvider")
        void testNoGrantEndDate(Date vaultCreationTime, int expectedRetentionPolicyStatus) {

            Assert.notNull(vaultCreationTime, "the vaultCreationTime cannot be null");
            validateRetentionPolicyStatus(expectedRetentionPolicyStatus);

            vault.setRetentionPolicy(null);
            vault.setGrantEndDate(null);
            vault.setCreationTime(DateTimeUtils.toLocalDateTime(vaultCreationTime));

            RetentionPoliciesService.updateRetentionPolicyExpiryDate(vault, CLOCK);
            assertThat(vault.getRetentionPolicyExpiry()).isEqualTo(DateTimeUtils.toLocalDateTime(vaultCreationTime));
            assertThat(vault.getRetentionPolicyStatus()).isEqualTo(expectedRetentionPolicyStatus);
            assertThat(vault.getRetentionPolicyLastChecked()).isEqualTo(DateTimeUtils.toLocalDateTime(DATE_NOW_3));
        }

        private Deposit getDepositAndRetrieveWithTimestamp(Date timestamp) {
            Retrieve retrieve = new Retrieve();
            retrieve.setTimestamp(DateTimeUtils.toLocalDateTime(timestamp));
            List<Retrieve> result = List.of(retrieve);
            Deposit deposit = Mockito.spy(new Deposit());
            lenient().doAnswer(invocation -> result).when(deposit).getRetrieves();
            return deposit;
        }

        public List<Deposit> getDeposit(Date timestamp1) {
            List<Deposit> result = new ArrayList<>();
            result.add(getDepositAndRetrieveWithTimestamp(addDays(timestamp1, -2)));
            result.add(getDepositAndRetrieveWithTimestamp(addDays(timestamp1, -1)));
            result.add(getDepositAndRetrieveWithTimestamp(timestamp1));
            return result;
        }

        @ParameterizedTest
        @MethodSource("grantEndDateProviderNoRetrieves")
        void testHasGrantEndDateNoRetrieves(Date vaultGrantEndDate, int rpMinRetentionPeriod, boolean rpExtendUponRetrieval, int expectedRetentionPolicyStatus, Date expectedRetentionPolicyExpiry) {

            Assert.notNull(vaultGrantEndDate, "the vaultGrantEndDate cannot be null");
            validateRetentionPolicyStatus(expectedRetentionPolicyStatus);

            vault.setRetentionPolicy(getRetentionPolicy(rpMinRetentionPeriod, rpExtendUponRetrieval));
            vault.setCreationTime(null);
            vault.setGrantEndDate(DateTimeUtils.toLocalDate(vaultGrantEndDate));

            lenient().doReturn(List.of()).when(vault).getDeposits();

            RetentionPoliciesService.updateRetentionPolicyExpiryDate(vault, CLOCK);

            assertThat(vault.getRetentionPolicyStatus()).isEqualTo(expectedRetentionPolicyStatus);
            assertThat(vault.getRetentionPolicyLastChecked()).isEqualTo(DateTimeUtils.toLocalDateTime(DATE_NOW_3));
            assertThat(vault.getRetentionPolicyExpiry()).isEqualTo(DateTimeUtils.toLocalDateTime(expectedRetentionPolicyExpiry));
        }

        @ParameterizedTest
        @MethodSource("grantEndDateProviderWithRetrieves")
        void testHasGrantEndDateWithRetries(Date vaultGrantEndDate, int rpMinRetentionPeriod, boolean rpExtendUponRetrieval, Date lastestRetieveDate, int expectedRetentionPolicyStatus, Date expectedRetentionPolicyExpiry) {

            Assert.notNull(vaultGrantEndDate, "the vaultGrantEndDate cannot be null");
            validateRetentionPolicyStatus(expectedRetentionPolicyStatus);

            vault.setRetentionPolicy(getRetentionPolicy(rpMinRetentionPeriod, rpExtendUponRetrieval));
            vault.setCreationTime(null);
            vault.setGrantEndDate(DateTimeUtils.toLocalDate(vaultGrantEndDate));

            lenient().doReturn(getDeposit(lastestRetieveDate)).when(vault).getDeposits();

            RetentionPoliciesService.updateRetentionPolicyExpiryDate(vault, CLOCK);

            assertThat(vault.getRetentionPolicyStatus()).isEqualTo(expectedRetentionPolicyStatus);
            assertThat(vault.getRetentionPolicyLastChecked()).isEqualTo(DateTimeUtils.toLocalDateTime(DATE_NOW_3));
            assertThat(vault.getRetentionPolicyExpiry()).isEqualTo(DateTimeUtils.toLocalDateTime(expectedRetentionPolicyExpiry));
        }
    }
}