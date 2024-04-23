package org.datavaultplatform.common.model.dao;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.model.PausedRetrieveState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

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
class PausedRetrieveStateRepositoryIT extends BaseDatabaseTest {

    @Autowired
    PausedRetrieveStateRepository repo;

    @Autowired
    JdbcTemplate template;
    
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testDbInitialization() {
        assertThat(repo.count()).isEqualTo(4);
        PausedRetrieveState ps = repo.getCurrentState().get();
        assertThat(ps.getId()).isEqualTo("204");
        assertThat(ps.isPaused()).isFalse();
        assertThat(ps.getCreated()).isEqualTo(LocalDateTime.of(2004,7,22,12,12,12, 123456000));
    }

    @Test
    void testRecentEntries() {
        List<PausedRetrieveState> recent0 = repo.getRecentEntries(0);
        assertThat(recent0).hasSize(0);
        List<PausedRetrieveState> recent1 = repo.getRecentEntries(1);
        assertThat(recent1).hasSize(1);
        List<PausedRetrieveState> recent2 = repo.getRecentEntries(2);
        assertThat(recent2).hasSize(2);
        List<PausedRetrieveState> recent3 = repo.getRecentEntries(3);
        assertThat(recent3).hasSize(3);
        List<String> recent4 = repo.getRecentEntries(4).stream().map(PausedRetrieveState::getId).toList();
        assertThat(recent4).isEqualTo(List.of("204","203","202","201"));
        List<PausedRetrieveState> recent5 = repo.getRecentEntries(5);
        assertThat(recent5).hasSize(4);
    }

    @Test
    @Transactional
    void testEmptyDatabase() {
        template.execute("delete from `paused_retrieve_state`");
        Long count = template.queryForObject("select count(*) from `paused_retrieve_state`", (rs, rowNum) -> rs.getLong(1));
        assertThat(count).isZero();
        assertThat(repo.count()).isZero();
        assertThat(repo.getCurrentState()).isEmpty();
    }
}
