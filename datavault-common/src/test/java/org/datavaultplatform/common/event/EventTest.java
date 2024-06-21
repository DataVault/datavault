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


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

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
        ObjectMapper mapper;

        @BeforeEach
        void setup() {
            mapper = new ObjectMapper();
        }

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
}
