package org.datavaultplatform.broker.services;


import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.broker.test.TestClockConfig;
import org.datavaultplatform.common.model.PausedRetrieveState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

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
public class PausedRetrieveStateServiceIT extends BaseReuseDatabaseTest {

    
    @Autowired
    Clock clock;
    
    @Autowired
    PausedRetrieveStateService service;
    
    @Test
    void testCurrentStateDoesNotExists() {
        PausedRetrieveState state = service.getCurrentState();
        assertThat(state).isNotNull();
        assertThat(state.isPaused()).isFalse();
        assertThat(state.getCreated()).isEqualTo(LocalDateTime.of(1970,1,1,12,0));
        assertThat(state.getId()).isEqualTo("-1");
    }

    @Test
    void testToggleAutoIdAndTimestamp() {
        PausedRetrieveState current = service.getCurrentState();
        PausedRetrieveState toggled = service.toggleState();
        assertThat(toggled.isPaused()).isNotEqualTo(current.isPaused());
        assertThat(toggled.getCreated()).isNotNull();
        assertThat(toggled.getId()).isNotNull();
    }

    
}