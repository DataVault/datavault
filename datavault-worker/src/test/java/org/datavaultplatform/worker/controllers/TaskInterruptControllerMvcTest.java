package org.datavaultplatform.worker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.RecordingEventSender;
import org.datavaultplatform.common.task.TaskInterrupter;
import org.datavaultplatform.common.task.TaskStage;
import org.datavaultplatform.common.task.TaskStageSupport;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.rabbit.RabbitMessageSelectorScheduler;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.datavaultplatform.worker.test.TestClockConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
        "worker.rabbit.enabled=false",
        "management.endpoints.web.exposure.include=*",
        "worker.security.enabled=true",
        "management.endpoints.web.base-path=/actuator",
        "management.endpoints.enabled-by-default=true",
        "management.health.rabbit.enabled=false"
})
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class TaskInterruptControllerMvcTest {

    @Autowired
    TaskInterruptController controller;
    
    @MockBean
    RabbitMessageSelectorScheduler mScheduler;
     
    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mvc;

    @MockBean
    RecordingEventSender eventSender;

    
    @Value("${worker.actuator.username}")
    private String username;

    @Value("${worker.actuator.password}")
    private String password;

    private RequestPostProcessor basicAuth() {
        return user(this.username).password(this.password);
    }
    
    @Test
    @SneakyThrows
    void testDelete() {
        Mockito.doNothing().when(mScheduler).setChecker(null);
        mvc.perform(delete("/task/interrupt").with(basicAuth()))
                .andDo(print())
                .andReturn();

        Mockito.verify(mScheduler).setChecker(null);
        Mockito.verifyNoMoreInteractions(mScheduler);
    }


    @Captor
    ArgumentCaptor<TaskInterrupter.Checker> argChecker;

    @ParameterizedTest
    @MethodSource("validInterruptSource")
    @SneakyThrows
    void testValidInterrupt(TaskInterrupt taskInterrupt) {

        Mockito.doNothing().when(mScheduler).setChecker(argChecker.capture());

        String taskInterruptJson = mapper.writeValueAsString(taskInterrupt);
        
        mvc.perform(
                post("/task/interrupt").with(basicAuth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(taskInterruptJson)
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string(taskInterruptJson))
                .andReturn();
        
        Mockito.verify(mScheduler).setChecker(argChecker.getValue());

        TaskInterrupter.Checker actual = argChecker.getValue();
        TaskStage taskStage = TaskStageSupport.getTaskStage(taskInterrupt.stageName()).get();
        Class<? extends Event> lastStageEvent = TaskInterruptController.getLastEventForTaskStage(taskStage);
        assertThat(actual.label()).isEqualTo(lastStageEvent.getSimpleName());

    }

    
    public static Stream<Arguments> validInterruptSource() {
        return TaskStageSupport.TASK_STAGE_MAP.keySet().stream().map(TaskInterrupt::new).map(Arguments::of);
    }

    @Test
    @SneakyThrows
    void testGet() {
        controller.interruptStageName = "the-current-interrupt-value";
        mvc.perform(get("/task/interrupt").with(basicAuth()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.stageName", Matchers.is("the-current-interrupt-value")))
                .andReturn();
        Mockito.verify(mScheduler, Mockito.never()).setChecker(any());
    }

    @Test
    @SneakyThrows
    void testGetAll() {
        MvcResult result = mvc.perform(get("/task/interrupt/all").with(basicAuth()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(result.getResponse().getContentAsString(), TaskInterruptControllerTest.EXPECTED_ALL_JSON,true);

    }
    
    @Nested
    class SecuredEndpointsTest {

        private void checkUnauthorized(MvcResult mvcResult) {
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @SneakyThrows
        void testGet() {
            MvcResult mvcResult = mvc.perform(get("/task/interrupt"))
                    .andReturn();
            checkUnauthorized(mvcResult);
        }
        @Test
        @SneakyThrows
        void testGetAll() {
            MvcResult mvcResult = mvc.perform(get("/task/interrupt/all"))
                    .andReturn();
            checkUnauthorized(mvcResult);
        }
        @Test
        @SneakyThrows
        void testPost() {
            MvcResult mvcResult = mvc.perform(post("/task/interrupt"))
                    .andReturn();
            checkUnauthorized(mvcResult);
        }
        @Test
        @SneakyThrows
        void testDelete() {
            MvcResult mvcResult = mvc.perform(delete("/task/interrupt"))
                    .andReturn();
            checkUnauthorized(mvcResult);
        }


    }
}