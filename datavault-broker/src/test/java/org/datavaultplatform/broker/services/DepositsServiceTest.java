package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.model.dao.AuditChunkStatusDAO;
import org.datavaultplatform.common.model.dao.DepositChunkDAO;
import org.datavaultplatform.common.model.dao.DepositDAO;
import org.datavaultplatform.common.model.dao.EventDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositsServiceTest {

    @Mock
    DepositDAO mDepositDAO;
    @Mock
    EventDAO mEventDAO;
    @Mock
    DepositChunkDAO mDepositChunkDAO;
    @Mock
    AuditChunkStatusDAO mAuditChunkStatusDAO;

    int auditPeriodMinutes;
    int auditPeriodHours;
    int auditPeriodDays;
    int auditPeriodMonths;
    int auditPeriodYears;
    int auditMaxChunksPerDeposits;
    int auditMaxTotalChunks;

    DepositsService depositsService;

    @BeforeEach
    void setup() {
        depositsService = new DepositsService(
                mDepositDAO,
                mDepositChunkDAO,
                mAuditChunkStatusDAO,
                mEventDAO,
                auditPeriodMinutes,
                auditPeriodHours,
                auditPeriodDays,
                auditPeriodMonths,
                auditPeriodYears,
                auditMaxChunksPerDeposits,
                auditMaxTotalChunks
        );
    }
    
    @Nested
    class ChunksStoredTests {
        
        @Test
        void testNull() {
            assertThat(depositsService.getChunksStored(null)).isEmpty();
            verifyNoMoreInteractions(mEventDAO);
        }

        @Test
        void testNonNull() {
            
            Complete complete = new Complete("depositId","jobId", new HashMap<>(), 1234L);
            Date completeDate = complete.getTimestamp();
            
            List<Integer> result = new ArrayList<>();

            when(mEventDAO.findDepositChunksStored("depositId")).thenReturn(result);

            assertThat(depositsService.getChunksStored("depositId")).isEqualTo(result);

            verify(mEventDAO).findDepositChunksStored("depositId");

            verifyNoMoreInteractions(mEventDAO);
        }
    }
}