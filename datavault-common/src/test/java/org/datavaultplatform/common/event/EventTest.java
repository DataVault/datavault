package org.datavaultplatform.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.datavaultplatform.common.event.client.*;
import org.datavaultplatform.common.event.delete.*;
import org.datavaultplatform.common.event.audit.*;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.common.event.roles.*;
import org.datavaultplatform.common.event.vault.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

    ObjectMapper mapper;

    public static Stream<Arguments> jsonPropertiesTestProvider() {
        List<Class<? extends Event>> all = new ArrayList<>();
        all.addAll(EVENTS_01_AUDIT);
        all.addAll(EVENTS_02_CLIENT);
        all.addAll(EVENTS_03_DELETE);
        all.addAll(EVENTS_04_DEPOSIT);
        all.addAll(EVENTS_05_RETRIEVE);
        all.addAll(EVENTS_O6_ROLES);
        all.addAll(EVENTS_07_VAULT);
        all.addAll(EVENTS_08_GENERAL);
        
        return all.stream().map(Arguments::of);
    }

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
    }
    
    public static final List<Class<? extends Event>> EVENTS_01_AUDIT = List.of(
            AuditComplete.class,
            AuditError.class,
            AuditStart.class,
            ChunkAuditComplete.class,
            ChunkAuditStarted.class
    );

    public static final List<Class<? extends Event>> EVENTS_02_CLIENT = List.of(
            Login.class,
            Logout.class
    );

    public static final List<Class<? extends Event>> EVENTS_03_DELETE = List.of(
            DeleteComplete.class,
            DeleteStart.class
    );


    public static final List<Class<? extends Event>> EVENTS_04_DEPOSIT = List.of(
            ValidationComplete.class,
            ComputedSize.class,
            StartCopyUpload.class,
            UploadComplete.class,
            Complete.class,

            StartTarValidation.class,
            ComputedEncryption.class,
            Start.class,
            CompleteCopyUpload.class,
            TransferComplete.class,

            PackageComplete.class,
            ComputedChunks.class,
            TransferProgress.class,
            ComputedDigest.class,
            CompleteTarValidation.class,

            StartChunkValidation.class,
            CompleteChunkValidation.class
    );

    public static final List<Class<? extends Event>> EVENTS_05_RETRIEVE = List.of(
            ArchiveStoreRetrievedChunk.class,
            RetrieveError.class,
            UploadedToUserStore.class,

            UserStoreSpaceAvailableChecked.class,
            RetrieveStart.class,
            RetrieveComplete.class,

            ArchiveStoreRetrievedAll.class
    );

    public static final List<Class<? extends Event>> EVENTS_O6_ROLES = List.of(
            CreateRoleAssignment.class,
            DeleteRoleAssignment.class,
            OrphanVault.class,

            TransferVaultOwnership.class,
            UpdateRoleAssignment.class
    );

    public static final List<Class<? extends Event>> EVENTS_07_VAULT = List.of(
            Create.class,
            Pending.class,
            Review.class,

            UpdatedDescription.class,
            UpdatedName.class
    );

    public static final List<Class<? extends Event>> EVENTS_08_GENERAL = List.of(
            Error.class,
            Event.class,
            InitStates.class,
            UpdateProgress.class
    );

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ToJsonAndBackTests {

        @Test
        @Order(1)
        void testEvents01Audit() {
            EVENTS_01_AUDIT.forEach(this::checkEvent);
        }

        @Test
        @Order(2)
        void testEvents02Client() {
            EVENTS_02_CLIENT.forEach(this::checkEvent);
        }

        @Test
        @Order(3)
        void testEvents03Delete() {
            EVENTS_03_DELETE.forEach(this::checkEvent);
        }

        @Test
        @Order(4)
        void testEvents04Deposit() {
            EVENTS_04_DEPOSIT.forEach(this::checkEvent);
        }

        @Test
        @Order(5)
        void testEvents05Retrieve() {
            EVENTS_05_RETRIEVE.forEach(this::checkEvent);
        }

        @Test
        @Order(6)
        void testEvents06Roles() {
            EVENTS_O6_ROLES.forEach(this::checkEvent);
        }

        @Test
        @Order(7)
        void testEvents07Vault() {
            EVENTS_07_VAULT.forEach(this::checkEvent);
        }

        @Test
        @Order(8)
        void testEvents08General() {
            EVENTS_08_GENERAL.forEach(this::checkEvent);
        }

        @SneakyThrows
        void checkEvent(Class<? extends Event> eventClass) {
            Event event = eventClass.getConstructor().newInstance();
            String json = mapper.writeValueAsString(event);
            Event fromJson = mapper.readValue(json, Event.class);
            assertThat(fromJson).isInstanceOf(eventClass);
        }
    }
    
    @ParameterizedTest
    @MethodSource("jsonPropertiesTestProvider")
    @SneakyThrows
    void jsonPropertiesTest(Class<? extends Event> eventClass) {
        Event event = eventClass.getConstructor().newInstance();

        event.setArchiveId("test-archive-id");
        event.setAssigneeId("test-assignee-id");
        event.setAuditId("test-audit-id");

        event.setChunkId("test-chunk-id");
        event.setDepositId("test-deposit-id");
        event.setJobId("test-job-id");

        event.setRoleId("test-role-id");
        event.setSchoolId("test-school-id");
        event.setUserId("test-user-id");

        event.setVaultId("test-vault-id");
        event.setNextState(2112);
        event.setPersistent(true);
        
        String json = mapper.writeValueAsString(event);
        System.out.println(json);

        Event event2 = mapper.readValue(json, eventClass);
        assertThat(event2).isInstanceOf(eventClass);

        assertThat(event2.getArchiveId()).isEqualTo(event.getArchiveId());
        assertThat(event2.getAssigneeId()).isEqualTo(event.getAssigneeId());
        assertThat(event2.getAuditId()).isEqualTo(event.getAuditId());

        assertThat(event2.getChunkId()).isEqualTo(event.getChunkId());
        assertThat(event2.getDepositId()).isEqualTo(event.getDepositId());
        assertThat(event2.getJobId()).isEqualTo(event.getJobId());

        assertThat(event2.getRoleId()).isEqualTo(event.getRoleId());
        assertThat(event2.getSchoolId()).isEqualTo(event.getSchoolId());
        assertThat(event2.getUserId()).isEqualTo(event.getUserId());

        assertThat(event2.getVaultId()).isEqualTo(event.getVaultId());
        assertThat(event2.getNextState()).isEqualTo(event.getNextState());
        assertThat(event2.getPersistent()).isEqualTo(event.getPersistent());
    }
    
    @Test
    @SneakyThrows
    void testInitStates(){
        InitStates initStates1 = new InitStates();
        ArrayList<String> states = new ArrayList<>();
        states.addAll(List.of("s1","s2","s3"));
        initStates1.setStates(states);
        
        String json = mapper.writeValueAsString(initStates1);
        Event event = mapper.readValue(json, Event.class);
        assertThat(event instanceof InitStates).isTrue();
        InitStates initStates2 = (InitStates) event;
        assertThat(initStates2.getStates()).isEqualTo(initStates1.getStates());
    }
}
