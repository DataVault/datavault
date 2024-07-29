package org.datavaultplatform.worker.tasks.deposit;


import lombok.SneakyThrows;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.event.deposit.ComputedSize;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.task.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositSizeComputerTest {

    @Captor
    ArgumentCaptor<Event> argEvent;

    @Nested
    class ValidationTests {

        static final UserEventSender sender = Mockito.mock(UserEventSender.class);
        static final Context context = Mockito.mock(Context.class);
        static final Map<String, String> props = new HashMap<>();
        static final Map<String, UserStore> uStores = new HashMap<>();

        @BeforeEach
        void setup() {
            doNothing().when(sender).send(argEvent.capture());
        }

        @ParameterizedTest
        @MethodSource("testArgsSource")
        void testArgs(String userId, String jobId, String depositId, UserEventSender userEventSender, String bagId,
                      Context context, Event lastEvent, Map<String, String> properties, Map<String, UserStore> userStores, List<String> filePaths,
                      String expectedMessage, boolean messageSent) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new DepositSizeComputer(userId, jobId, depositId, userEventSender, bagId, context, lastEvent, properties, userStores, filePaths));
            assertThat(ex.getMessage()).isEqualTo(expectedMessage);
            if (messageSent) {
                String actualMessage = argEvent.getValue().getMessage();
                assertThat(actualMessage).isEqualTo(expectedMessage);
                verify(sender).send(argEvent.getValue());
            }
            verifyNoMoreInteractions(sender);
        }

        static Stream<Arguments> testArgsSource() {
            return Stream.of(
                    Arguments.of(null, null, null, null, null, null, null, null, null, null, "The userID cannot be blank", false),
                    Arguments.of("userid", null, null, null, null, null, null, null, null, null, "The jobID cannot be blank", false),
                    Arguments.of("userid", "jobId", null, null, null, null, null, null, null, null, "The depositId cannot be blank", false),
                    Arguments.of("userid", "jobId", "depositId", null, null, null, null, null, null, null, "The userEventSender cannot be null", false),
                    Arguments.of("userid", "jobId", "depositId", sender, null, null, null, null, null, null, "The bagID cannot be blank", false),
                    Arguments.of("userid", "jobId", "depositId", sender, "bagId", null, null, null, null, null, "The context cannot be null", false),
                    Arguments.of("userid", "jobId", "depositId", sender, "bagId", context, null, null, null, null, "The properties cannot be null", false),
                    Arguments.of("userid", "jobId", "depositId", sender, "bagId", context, null, props, null, null, "Deposit failed: null list of userStores", true),
                    Arguments.of("userid", "jobId", "depositId", sender, "bagId", context, null, props, uStores, null, "Deposit failed: null list of fileStorePaths", true)
            );
        }
    }

    @Nested
    class NonValidationTests {

        @Captor
        ArgumentCaptor<Event> argEvent;

        @Mock
        Context mContext;

        @Mock
        UserEventSender mUserEventSender;

        final Map<String, UserStore> userStores = new HashMap<>();
        DepositSizeComputer computer;

        final Map<String, String> properties = new HashMap<>();

        @BeforeEach
        @SneakyThrows
        void setup() {
            UserStore mUserStore1 = Mockito.mock(UserStore.class);
            lenient().when(mUserStore1.getSize("AA")).thenReturn(12L);
            lenient().when(mUserStore1.getSize("AAA")).thenReturn(2100L);

            UserStore mUserStore2 = Mockito.mock(UserStore.class);
            lenient().when(mUserStore2.getSize("BB")).thenReturn(34_0000L);
            lenient().when(mUserStore2.getSize("BBB")).thenReturn(1200_0000L);

            UserStore mUserStore3 = Mockito.mock(UserStore.class);
            lenient().when(mUserStore2.getSize("CC")).thenReturn(123L);
            lenient().when(mUserStore2.getSize("CCC")).thenReturn(456L);

            doNothing().when(mUserEventSender).send(argEvent.capture());

            userStores.put("A", mUserStore1);
            userStores.put("B", mUserStore2);


        }

        @ParameterizedTest
        @MethodSource("computeSizeSource")
        void testComputeSize(List<String> fileStorePaths, long expectedSize) {
            computer = new DepositSizeComputer("userId", "jobId", "depositId", mUserEventSender, "bagId", mContext, null, properties, userStores, fileStorePaths);
            assertThat(computer.calculateTotalDepositSize()).isEqualTo(expectedSize);
            Event actualEvent = argEvent.getValue();
            assertThat(actualEvent).isInstanceOf(ComputedSize.class);
            ComputedSize cs = (ComputedSize) actualEvent;
            assertThat(cs.getBytes()).isEqualTo(expectedSize);
            assertThat(cs.getDepositId()).isEqualTo("depositId");
            assertThat(cs.getJobId()).isEqualTo("jobId");
            verify(mUserEventSender).send(actualEvent);
            verifyNoMoreInteractions(mUserEventSender);
        }

        @Test
        void testMissingUserStore() {
            List<String> fileStorePaths = List.of("D/DD");
            computer = new DepositSizeComputer("userId", "jobId", "depositId", mUserEventSender, "bagId", mContext, null, properties, userStores, fileStorePaths);

            Exception ex = assertThrows(Exception.class, () -> computer.calculateTotalDepositSize());

            Event actualEvent = argEvent.getValue();
            assertThat(actualEvent).isInstanceOf(Error.class);
            Error err = (Error) actualEvent;
            assertThat(err.getMessage()).isEqualTo("Deposit failed: could not access user filesystem");
            assertThat(err.getDepositId()).isEqualTo("depositId");
            assertThat(err.getJobId()).isEqualTo("jobId");
            verify(mUserEventSender).send(actualEvent);
            verifyNoMoreInteractions(mUserEventSender);

            RuntimeException rte = (RuntimeException) ex;
            assertThat(rte).hasMessage("java.lang.IllegalStateException: Could not get user store for D");
        }

        static Stream<Arguments> computeSizeSource() {
            return Stream.of(
                    Arguments.of(List.of("A/AA", "A/AAA", "B/BB", "B/BBB"), 1234_2112L),
                    Arguments.of(List.of("B/BB", "A/AAA", "A/AA", "B/BBB"), 1234_2112L),
                    Arguments.of(List.of("B/BB", "B/BBB", "A/AA", "A/AAA"), 1234_2112L),
                    Arguments.of(Arrays.asList("B/BBB", "B/BB", null, "A/AAA", "A/AA"), 1234_2112L),
                    Arguments.of(Collections.emptyList(), 0L)
            );
        }
    }
}