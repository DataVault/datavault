package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventTest;
import org.datavaultplatform.common.task.Task;
import org.junit.jupiter.api.*;


import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseTaskJsonTest<T extends Task> {

    public static final String TEST_JOB_ID = "test-job-id";

    public static final Map<String, String> TEST_PROPS = Map.of("key1", "value1", "key2", "value2");

    ObjectMapper mapper;

    public abstract Class<T> getTaskClass();

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
    }
    
    @SneakyThrows
    void checkRetrieveTask(Class<? extends Event> eventClass) {

        T task = getTaskClass().getConstructor().newInstance();
        task.setJobID(TEST_JOB_ID);
        task.setProperties(TEST_PROPS);

        if(eventClass != null) {
            Event lastEvent1 = eventClass.getConstructor().newInstance();
            task.setLastEvent(lastEvent1);
        }

        String taskJson = mapper.writeValueAsString(task);

        checkTaskJsonAs(taskJson, eventClass, Task.class);
        checkTaskJsonAs(taskJson, eventClass, getTaskClass());
    }

    @SneakyThrows
    private void checkTaskJsonAs(String taskJson, Class<? extends Event> eventClass, Class<? extends Task> taskClass) {
        Task task2 = mapper.readValue(taskJson, taskClass);
        Event lastEvent2 = task2.getLastEvent();
        if (eventClass == null) {
            assertThat(lastEvent2).isNull();
        } else {
            assertThat(lastEvent2).isInstanceOf(eventClass);
            assertThat(lastEvent2.getClass()).isEqualTo(eventClass);
            assertThat(task2.getJobID()).isEqualTo(TEST_JOB_ID);
            assertThat(task2.getProperties()).isEqualTo(TEST_PROPS);
        }
    }

    @Test
    @Order(0)
    void testEvents00Null() {
        checkRetrieveTask(null);
    }

    @Test
    @Order(1)
    void testEvents01Audit() {
        EventTest.EVENTS_01_AUDIT.forEach(this::checkRetrieveTask);
    }

    @Test
    @Order(2)
    void testEvents02Client() {
        EventTest.EVENTS_02_CLIENT.forEach(this::checkRetrieveTask);
    }

    @Test
    @Order(3)
    void testEvents03Delete() {
        EventTest.EVENTS_03_DELETE.forEach(this::checkRetrieveTask);
    }

    @Test
    @Order(4)
    void testEvents04Deposit() {
        EventTest.EVENTS_04_DEPOSIT.forEach(this::checkRetrieveTask);
    }

    @Test
    @Order(5)
    void testEvents05Retrieve() {
        EventTest.EVENTS_05_RETRIEVE.forEach(this::checkRetrieveTask);
    }

    @Test
    @Order(6)
    void tesEvents06Roles() {
        EventTest.EVENTS_O6_ROLES.forEach(this::checkRetrieveTask);
    }

    @Test
    @Order(7)
    void testEvents07Vault() {
        EventTest.EVENTS_07_VAULT.forEach(this::checkRetrieveTask);
    }

    @Test
    @Order(8)
    void testEvents08General() {
        EventTest.EVENTS_08_GENERAL.forEach(this::checkRetrieveTask);
    }
}
