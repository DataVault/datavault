package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.dao.AuditChunkStatusDAO;
import org.datavaultplatform.common.model.dao.DepositChunkDAO;
import org.datavaultplatform.common.model.dao.DepositDAO;
import org.datavaultplatform.common.model.dao.EventDAO;
import org.datavaultplatform.common.util.StoredChunks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
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
        void testGetChunksStored() {
            
            StoredChunks result = new StoredChunks();

            when(mEventDAO.findDepositChunksStored("depositId")).thenReturn(result);

            assertThat(depositsService.getChunksStored("depositId")).isEqualTo(result);

            verify(mEventDAO).findDepositChunksStored("depositId");

            verifyNoMoreInteractions(mEventDAO);
        }
    }
    
    @Captor
    ArgumentCaptor<String> argDepositId;

    @Captor
    ArgumentCaptor<String> argJobClass;
    
    @Test
    void testGetLastEventForDepositFound() {
        Event mEvent = Mockito.mock(Event.class);
        when(mEventDAO.findLatestEventByDepositIdAndJobTaskClass(argDepositId.capture(), argJobClass.capture())).thenReturn(Optional.of(mEvent));
        
        Event lastEvent = depositsService.getLastNotFailedDepositEvent("depositId123");
        assertThat(lastEvent).isEqualTo(mEvent);
        String actualDepositId = argDepositId.getValue();
        String actualJobClass = argJobClass.getValue();
        assertThat(actualDepositId).isEqualTo("depositId123");
        assertThat(actualJobClass).isEqualTo(Job.TASK_CLASS_DEPOSIT);
        verify(mEventDAO).findLatestEventByDepositIdAndJobTaskClass(actualDepositId, actualJobClass);
        verifyNoMoreInteractions(mEventDAO);
    }
    @Test
    void testGetLastEventForDepositNotFound() {
        when(mEventDAO.findLatestEventByDepositIdAndJobTaskClass(argDepositId.capture(), argJobClass.capture())).thenReturn(Optional.empty());

        Event lastEvent = depositsService.getLastNotFailedDepositEvent("depositId123");
        assertThat(lastEvent).isNull();
        String actualDepositId = argDepositId.getValue();
        String actualJobClass = argJobClass.getValue();
        assertThat(actualDepositId).isEqualTo("depositId123");
        assertThat(actualJobClass).isEqualTo(Job.TASK_CLASS_DEPOSIT);
        verify(mEventDAO).findLatestEventByDepositIdAndJobTaskClass(actualDepositId, actualJobClass);
        verifyNoMoreInteractions(mEventDAO);
    }
    @Test
    void testGetLastEventForRetrieveFound() {
        Event mEvent = Mockito.mock(Event.class);
        when(mEventDAO.findLatestEventByDepositIdAndJobTaskClass(argDepositId.capture(), argJobClass.capture())).thenReturn(Optional.of(mEvent));

        Event lastEvent = depositsService.getLastNotFailedRetrieveEvent("depositId123");
        assertThat(lastEvent).isEqualTo(mEvent);
        String actualDepositId = argDepositId.getValue();
        String actualJobClass = argJobClass.getValue();
        assertThat(actualDepositId).isEqualTo("depositId123");
        assertThat(actualJobClass).isEqualTo(Job.TASK_CLASS_RETRIEVE);
        verify(mEventDAO).findLatestEventByDepositIdAndJobTaskClass(actualDepositId, actualJobClass);
        verifyNoMoreInteractions(mEventDAO);
    }
    @Test
    void testGetLastEventForRetrieveNotFound() {
        when(mEventDAO.findLatestEventByDepositIdAndJobTaskClass(argDepositId.capture(), argJobClass.capture())).thenReturn(Optional.empty());

        Event lastEvent = depositsService.getLastNotFailedRetrieveEvent("depositId123");
        assertThat(lastEvent).isNull();
        String actualDepositId = argDepositId.getValue();
        String actualJobClass = argJobClass.getValue();
        assertThat(actualDepositId).isEqualTo("depositId123");
        assertThat(actualJobClass).isEqualTo(Job.TASK_CLASS_RETRIEVE);
        verify(mEventDAO).findLatestEventByDepositIdAndJobTaskClass(actualDepositId, actualJobClass);
        verifyNoMoreInteractions(mEventDAO);
    }

}