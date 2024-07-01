package org.datavaultplatform.worker.controllers;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.RecordingEventSender;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.datavaultplatform.worker.test.TestClockConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
        "worker.security.enabled=true",
        "worker.rabbit.enabled=false",
        "management.endpoints.web.exposure.include=*",
        "management.health.rabbit.enabled=false"})

@AutoConfigureMockMvc
@Import(TestClockConfig.class)
public class TimeControllerTest {

    @Autowired
    MockMvc mvc;

    private final String EXPECTED_TIME_INFO = """
            {"time":"blah"}
            """;
    @ParameterizedTest
    @ValueSource(strings = {"/time","/time/"})
    void testTimeInfo(String path) throws Exception {
        mvc.perform(get(path)).andDo(print()).andExpect(status().isOk());

    }

    @MockBean
    RecordingEventSender eventSender;

}
