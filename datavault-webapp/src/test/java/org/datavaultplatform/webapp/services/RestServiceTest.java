package org.datavaultplatform.webapp.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.dto.PausedStateDTO;
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
    class PausedStateTests {

        @Test
        @WithMockUser(username = "user1")
        void testTogglePausedState() {
            restService.togglePausedState();
        }

        @Test
        @WithMockUser(username = "user2")
        void testGetCurrentPausedState() {
            PausedStateDTO result = restService.getCurrentPausedState();
            assertThat(result.isPaused()).isTrue();
            assertThat(result.created()).isEqualTo(LocalDateTime.of(2007, 12, 3, 10, 15, 30));
        }

        @Test
        @WithMockUser(username = "user3")
        void testGetPausedStateHistory() {
            List<PausedStateDTO> result = restService.getPausedStateHistory(null);
            assertThat(result.size()).isEqualTo(3);

            PausedStateDTO dto0 = result.get(0);
            PausedStateDTO dto1 = result.get(1);
            PausedStateDTO dto2 = result.get(2);

            assertThat(dto0.isPaused()).isTrue();
            assertThat(dto1.isPaused()).isFalse();
            assertThat(dto2.isPaused()).isTrue();

            assertThat(dto0.created()).isEqualTo(LocalDateTime.of(2012, 12, 12, 12, 12, 12));
            assertThat(dto1.created()).isEqualTo(LocalDateTime.of(2011, 11, 11, 11, 11, 11));
            assertThat(dto2.created()).isEqualTo(LocalDateTime.of(2010, 10, 10, 10, 10, 10));
        }
    }
}