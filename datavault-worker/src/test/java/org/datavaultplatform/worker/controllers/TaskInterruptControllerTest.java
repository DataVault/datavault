package org.datavaultplatform.worker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.task.TaskInterrupter;
import org.datavaultplatform.common.task.TaskStage;
import org.datavaultplatform.common.task.TaskStageSupport;
import org.datavaultplatform.worker.rabbit.RabbitMessageSelectorScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;


import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskInterruptControllerTest {

    static final String EXPECTED_ALL_JSON = """
            [ {
              "stageName" : "deposit1computesize"
            }, {
              "stageName" : "deposit2transfer"
            }, {
              "stageName" : "deposit3packageencrypt"
            }, {
              "stageName" : "deposit4archive"
            }, {
              "stageName" : "deposit5verify"
            }, {
              "stageName" : "deposit6final"
            }, {
              "stageName" : "retrieve1checkuserstorefreespace"
            }, {
              "stageName" : "retrieve2retrievefromarchiveandrecompose"
            }, {
              "stageName" : "retrieve3uploadedtouserstore"
            }, {
              "stageName" : "retrieve4final"
            } ]
            """;
    ObjectMapper mapper;
    TaskInterruptController controller;
    @Mock
    RabbitMessageSelectorScheduler mScheduler;
    @Captor
    ArgumentCaptor<TaskInterrupter.Checker> argChecker;

    public static Stream<Arguments> validInterruptSource() {
        return TaskStageSupport.TASK_STAGE_MAP.keySet().stream().map(TaskInterrupt::new).map(Arguments::of);
    }

    @BeforeEach
    void setup() {
        controller = new TaskInterruptController(mScheduler);
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    @SneakyThrows
    public void testGetAllInterrupts() {
        List<TaskInterrupt> all = controller.getAllTaskInterrupts();
        String allJson = mapper.writeValueAsString(all);
        JSONAssert.assertEquals(EXPECTED_ALL_JSON, allJson, true);
    }

    @Test
    @SneakyThrows
    public void testDelete() {
        Mockito.doNothing().when(mScheduler).setChecker(null);
        controller.deleteInterrupt();
        Mockito.verify(mScheduler).setChecker(null);
        Mockito.verifyNoMoreInteractions(mScheduler);
    }

    @Test
    void testNullInterrupt() {
        var ex = assertThrows(IllegalArgumentException.class, () -> {
            controller.setInterrupt(null);
        });
        assertThat(ex).hasMessage("TaskInterrupt must not be null");
        Mockito.verifyNoInteractions(mScheduler);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "", "oops"})
    @NullSource
    void testInvalidInterruptStage(String invalidStage) {
        ResponseEntity<?> result = controller.setInterrupt(new TaskInterrupt(invalidStage));
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Mockito.verifyNoInteractions(mScheduler);
        String expected = "ERROR : [Could not resolve taskInterrupt stage [%s]]".formatted(invalidStage);
        assertThat(result.getBody()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("validInterruptSource")
    void testValidInterrupt(TaskInterrupt taskInterrupt) {

        Mockito.doNothing().when(mScheduler).setChecker(argChecker.capture());

        ResponseEntity<?> result = controller.setInterrupt(taskInterrupt);
        
        assertThat(controller.interruptStageName).isEqualTo(taskInterrupt.stageName());

        Mockito.verify(mScheduler).setChecker(argChecker.getValue());

        TaskInterrupter.Checker actual = argChecker.getValue();
        TaskStage taskStage = TaskStageSupport.getTaskStage(taskInterrupt.stageName()).get();
        Class<? extends Event> lastStageEvent = TaskInterruptController.getLastEventForTaskStage(taskStage);
        assertThat(actual.label()).isEqualTo(lastStageEvent.getSimpleName());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isEqualTo(taskInterrupt);
    }

}