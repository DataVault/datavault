package org.datavaultplatform.worker.tasks.deposit;

import lombok.SneakyThrows;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.tasks.PackageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositVerifierTest {

    @Captor
    ArgumentCaptor<Event> argEvent;

    static final Map<String,String> props = new HashMap<>();

    static final UserEventSender sender = Mockito.mock(UserEventSender.class);
    static final Context context = Mockito.mock(Context.class);

    @BeforeEach
    void setup() {
        doNothing().when(sender).send(argEvent.capture());
    }

    @ParameterizedTest
    @MethodSource("testArgsSource")
    void testArgs(String userId, String jobId, String depositId, UserEventSender userEventSender, String bagId,
                  Context context, Event lastEvent, Map<String, String> properties, Set<ArchiveStoreInfo> archiveStoreInfos,
                  String expectedMessage, boolean messageSent) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new DepositVerifier(userId, jobId, depositId, userEventSender, bagId, context, lastEvent, properties, archiveStoreInfos));
        assertThat(ex.getMessage()).isEqualTo(expectedMessage);
        if(messageSent){
            String actualMessage = argEvent.getValue().getMessage();
            assertThat(actualMessage).isEqualTo(expectedMessage);
            verify(sender).send(argEvent.getValue());
        }
        verifyNoMoreInteractions(sender);
    }

    static Stream<Arguments> testArgsSource() {
        return Stream.of(
                Arguments.of(null,     null,    null,        null,   null,    null,    null,  null,  null,    "The userID cannot be blank", false),
                Arguments.of("userid", null,    null,        null,   null,    null,    null,  null,  null,    "The jobID cannot be blank", false),
                Arguments.of("userid", "jobId", null,        null,   null,    null,    null,  null,  null,    "The depositId cannot be blank", false),
                Arguments.of("userid", "jobId", "depositId", null,   null,    null,    null,  null,  null,    "The userEventSender cannot be null", false),
                Arguments.of("userid", "jobId", "depositId", sender, null,    null,    null,  null,  null,    "The bagID cannot be blank", false),
                Arguments.of("userid", "jobId", "depositId", sender, "bagId", null,    null,  null,  null,    "The context cannot be null", false),
                Arguments.of("userid", "jobId", "depositId", sender, "bagId", context, null,  null,  null,    "The properties cannot be null", false),
                Arguments.of("userid", "jobId", "depositId", sender, "bagId", context, null,  props, null,    "Deposit failed: null list of userStores", true)
        );
    }
    
}