package org.datavaultplatform.webapp.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.dto.PausedDepositStateDTO;
import org.datavaultplatform.common.dto.PausedRetrieveStateDTO;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.app.services.BaseRestTemplateWithLoggingTest;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
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
            restService.toggleDepositPausedState();
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
            restService.toggleRetrievePausedState();
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
            Date vaultReviewDate = convertToDateViaInstant(localVaultReviewDate);
            VaultInfo vaultInfo = restService.updateVaultReviewDate("vault-abc",vaultReviewDate);

            assertThat(vaultInfo.getID()).isEqualTo("vault-abc-id");
            assertThat(vaultInfo.getName()).isEqualTo("test-vault-info");
            assertThat(vaultInfo.getNotes()).isEqualTo("test-vault-info-notes");
            assertThat(vaultInfo.getDescription()).isEqualTo("test-vault-info-description");
            assertThat(convertToLocalDateViaInstant(vaultInfo.getReviewDate())).isEqualTo(localVaultReviewDate);
        }
    }

    public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
    }
    public Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneOffset.UTC)
                .toInstant());
    }
    
}