package org.datavaultplatform.worker.tasks.deposit;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.task.Context;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DepositSupportTest {

    public static final String USER_ID = "test-user-id";
    public static final String BAG_ID = "teat-bag-id";
    public static final String DEPOSIT_ID = "test-deposit-id";
    public static final String JOB_ID = "test-job-id";
    static final UserEventSender SENDER = Mockito.mock(UserEventSender.class);
    static final Context CONTEXT = Mockito.mock(Context.class);

    static Stream<Arguments> testArgsSource() {
        return Stream.of(
                Arguments.of(null, null, null, null, null, null, null, null, "The userID cannot be blank", false),
                Arguments.of(USER_ID, null, null, null, null, null, null, null, "The jobID cannot be blank", false),
                Arguments.of(USER_ID, JOB_ID, null, null, null, null, null, null, "The depositId cannot be blank", false),
                Arguments.of(USER_ID, JOB_ID, DEPOSIT_ID, null, null, null, null, null, "The userEventSender cannot be null", false),
                Arguments.of(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, null, null, null, null, "The bagID cannot be blank", false),
                Arguments.of(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, null, null, null, "The context cannot be null", false),
                Arguments.of(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, CONTEXT, null, null, "The properties cannot be null", false)
        );
    }

    @ParameterizedTest
    @MethodSource("testArgsSource")
    void testArgs(String userId, String jobId, String depositId, UserEventSender userEventSender, String bagId,
                  Context context, Event lastEvent, Map<String, String> properties,
                  String expectedMessage) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new MyDepositSupport(userId, jobId, depositId, userEventSender, bagId, context, lastEvent, properties));
        assertThat(ex.getMessage()).isEqualTo(expectedMessage);
        verifyNoMoreInteractions(SENDER);
    }

    static class MyDepositSupport extends DepositSupport {
        public MyDepositSupport(String userID, String jobID, String depositId, UserEventSender userEventSender, String bagID, Context context, Event lastEvent, Map<String, String> properties) {
            super(userID, jobID, depositId, userEventSender, bagID, context, lastEvent, properties);
        }
    }
}