package org.datavaultplatform.broker.services;


import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.broker.test.TestClockConfig;
import org.datavaultplatform.common.model.PausedDepositState;
import org.datavaultplatform.common.model.dao.PausedDepositStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
        "broker.email.enabled=true",
        "broker.controllers.enabled=false",
        "broker.rabbit.enabled=false",
        "broker.scheduled.enabled=false"
})
@Import(TestClockConfig.class)
public class PausedDepositStateServiceIT extends BaseReuseDatabaseTest {

    
    @Autowired
    Clock clock;
    
    @Autowired
    PausedDepositStateService service;
    
    @Test
    void testCurrentStateDoesNotExists() {
        PausedDepositState state = service.getCurrentState();
        assertThat(state).isNotNull();
        assertThat(state.isPaused()).isFalse();
        assertThat(state.getCreated()).isEqualTo(LocalDateTime.of(1970,1,1,12,0));
        assertThat(state.getId()).isEqualTo("-1");
    }

    @Test
    void testToggleAutoIdAndTimestamp() {
        PausedDepositState current = service.getCurrentState();
        PausedDepositState toggled = service.toggleState();
        assertThat(toggled.isPaused()).isNotEqualTo(current.isPaused());
        assertThat(toggled.getCreated()).isNotNull();
        assertThat(toggled.getId()).isNotNull();
    }

    
}