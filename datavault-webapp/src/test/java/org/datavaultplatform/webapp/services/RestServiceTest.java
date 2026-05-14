package org.datavaultplatform.webapp.services;

import io.micrometer.tracing.Tracer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.dto.PausedDepositStateDTO;
import org.datavaultplatform.common.dto.PausedRetrieveStateDTO;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.util.TraceIdWrapper;
import org.datavaultplatform.common.util.TraceInfo;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.app.services.BaseRestTemplateWithLoggingTest;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = DataVaultWebApp.class)
@Slf4j
@ProfileDatabase
@TestPropertySource(properties = "broker.url=http://www.example.com:1234")
class RestServiceTest extends BaseRestTemplateWithLoggingTest {
    
    @Autowired
    RestService restService;
    
    @BeforeEach
    @SneakyThrows
    void setup() {
        setupInternal("classpath:/stubs/restService/*");
    }

    @Nested
    class PausedDepositStateTests {

        @Test
        @WithMockUser(username = "user1")
        void testTogglePausedState() {
            assertDoesNotThrow(() -> {
                restService.toggleDepositPausedState();
            });
        }

        @Test
        @WithMockUser(username = "user2")
        void testGetCurrentPausedState() {
            PausedDepositStateDTO result = restService.getCurrentDepositPausedState();
            assertThat(result.isPaused()).isTrue();
            assertThat(result.created()).isEqualTo(LocalDateTime.of(2007, 12, 3, 10, 15, 30));
        }

        @Test
        @WithMockUser(username = "user3")
        void testGetPausedStateHistory() {
            List<PausedDepositStateDTO> result = restService.getPausedDepositStateHistory(null);
            assertThat(result.size()).isEqualTo(3);

            PausedDepositStateDTO dto0 = result.get(0);
            PausedDepositStateDTO dto1 = result.get(1);
            PausedDepositStateDTO dto2 = result.get(2);

            assertThat(dto0.isPaused()).isTrue();
            assertThat(dto1.isPaused()).isFalse();
            assertThat(dto2.isPaused()).isTrue();

            assertThat(dto0.created()).isEqualTo(LocalDateTime.of(2012, 12, 12, 12, 12, 12));
            assertThat(dto1.created()).isEqualTo(LocalDateTime.of(2011, 11, 11, 11, 11, 11));
            assertThat(dto2.created()).isEqualTo(LocalDateTime.of(2010, 10, 10, 10, 10, 10));
        }
    }

    @Nested
    class PausedRetrieveStateTests {

        @Test
        @WithMockUser(username = "user1")
        void testTogglePausedState() {
            assertDoesNotThrow(() -> {
                restService.toggleRetrievePausedState();
            });
        }

        @Test
        @WithMockUser(username = "user2")
        void testGetCurrentPausedState() {
            PausedRetrieveStateDTO result = restService.getCurrentRetrievePausedState();
            assertThat(result.isPaused()).isTrue();
            assertThat(result.created()).isEqualTo(LocalDateTime.of(2007, 12, 3, 10, 15, 30));
        }

        @Test
        @WithMockUser(username = "user3")
        void testGetPausedStateHistory() {
            List<PausedRetrieveStateDTO> result = restService.getPausedRetrieveStateHistory(null);
            assertThat(result.size()).isEqualTo(3);

            PausedRetrieveStateDTO dto0 = result.get(0);
            PausedRetrieveStateDTO dto1 = result.get(1);
            PausedRetrieveStateDTO dto2 = result.get(2);

            assertThat(dto0.isPaused()).isTrue();
            assertThat(dto1.isPaused()).isFalse();
            assertThat(dto2.isPaused()).isTrue();

            assertThat(dto0.created()).isEqualTo(LocalDateTime.of(2012, 12, 12, 12, 12, 12));
            assertThat(dto1.created()).isEqualTo(LocalDateTime.of(2011, 11, 11, 11, 11, 11));
            assertThat(dto2.created()).isEqualTo(LocalDateTime.of(2010, 10, 10, 10, 10, 10));
        }
    }
    
    @Nested
    class UpdateReviewDateTests {
        
        @Test
        @WithMockUser(username = "user1")
        void testUpdateReviewDateIsOkay() {
            LocalDate localVaultReviewDate = LocalDate.of(2112, 12, 21);
            VaultInfo vaultInfo = restService.updateReviewDateOfVault("vault-abc",localVaultReviewDate);

            assertThat(vaultInfo.getID()).isEqualTo("vault-abc-id");
            assertThat(vaultInfo.getName()).isEqualTo("test-vault-info");
            assertThat(vaultInfo.getNotes()).isEqualTo("test-vault-info-notes");
            assertThat(vaultInfo.getDescription()).isEqualTo("test-vault-info-description");
            assertThat(vaultInfo.getReviewDate()).isEqualTo(localVaultReviewDate);
        }
    }
    
    @Nested
    class RestartTests {
        
        @Test
        @WithMockUser(username = "user1")
        void testRestartRetrieveWithDepositId() {
            boolean result = restService.restartRetrieve("deposit123","retrieve456");
            assertThat(result).isTrue();
        }

    }

    @SuppressWarnings("GrazieInspectionRunner")
    @Nested
    class TraceIdFromBrokerTests {

        @Autowired
        Tracer tracer;

        @Test
        void testGetTraceFromBrokerWithNoTraceId() {
            TraceInfo result = restService.getTraceFromBroker("user", "password");
            assertThat(result.traceId()).isEqualTo("aaaabbbbccccddddaaaabbbbccccdddd");
        }

        @Test
        void testGetTraceFromBrokerWithTraceId() {

            String traceId = "aaaabbbbccccddddaaaabbbbccccdddd";
            TraceIdWrapper wrapper = new TraceIdWrapper(traceId, tracer);

            wrapper.runWithinWrapper(() -> {
                assertThat(tracer.currentSpan().context().traceId()).isEqualTo(traceId);
                TraceInfo result = restService.getTraceFromBroker("user", "password");
                assertThat(result.traceId()).isEqualTo("abcdef11abcdef22abcdef33abcdef44");
            });
        }
    }
    
    
    @Nested
    class RefreshVaultReviewTests {

        @Test
        void testVaultId123AndDepositReviewsAdded() {
            boolean depositReviewsAdded = restService.refreshUnderwayVaultReview("vault-id-123");
            assertThat(depositReviewsAdded).isTrue();
        }

        @Test
        void testVaultId234AndDepositReviewsNotAdded() {
            boolean depositReviewsAdded = restService.refreshUnderwayVaultReview("vault-id-234");
            assertThat(depositReviewsAdded).isFalse();
        }
    }
}