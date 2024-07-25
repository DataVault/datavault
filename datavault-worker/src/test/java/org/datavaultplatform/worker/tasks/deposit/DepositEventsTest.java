package org.datavaultplatform.worker.tasks.deposit;

import lombok.SneakyThrows;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.deposit.PackageChunkEncryptComplete;
import org.datavaultplatform.common.event.deposit.TransferComplete;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.stream.*;

class DepositEventsTest {

    @Nested
    class IsLastEventBeforeTests {
        
        final UpdateProgress updateProgress = new UpdateProgress();
        final Error error = new Error();
        
        @ParameterizedTest
        @MethodSource("isBeforeArgs")
        void testIsBefore(Event lastEvent, Class<? extends Event> eventClass) {
            checkIsLastEventBefore(lastEvent, eventClass, true);
        }

        @ParameterizedTest
        @MethodSource("isNotBeforeArgs")
        void testIsNotBefore(Event lastEvent, Class<? extends Event> eventClass) {
            checkIsLastEventBefore(lastEvent, eventClass, false);
        }

        @ParameterizedTest
        @MethodSource("allEventClasses")
        void testNullIsBeforeAll(Class<? extends Event> eventClass) {
            checkIsLastEventBefore(null, eventClass, true);
        }

        @ParameterizedTest
        @MethodSource("allEventClasses")
        void testProgressUpdateIsBeforeAll(Class<? extends Event> eventClass) {
            checkIsLastEventBefore(updateProgress, eventClass, true);
        }
        @ParameterizedTest
        @MethodSource("allEventClasses")
        void testErrorIsBeforeAll(Class<? extends Event> eventClass) {
            checkIsLastEventBefore(error, eventClass, true);
        }
        
        void checkIsLastEventBefore(Event lastEvent, Class<? extends Event> eventClass, boolean expectedIsBefore) {
            assertThat(DepositEvents.INSTANCE.isLastEventBefore(lastEvent, eventClass)).isEqualTo(expectedIsBefore);
        }


        @SneakyThrows
        static Stream<Arguments> isBeforeArgs() {
            Stream.Builder<Arguments> builder = Stream.builder();
            for (int limit = 1; limit < DepositEvents.EVENT_ORDER.size(); limit++) {
                Class<? extends Event> eventClass = getEventClass(limit);
                for (int eventIdx = 0; eventIdx < limit; eventIdx++) {
                    assertThat(eventIdx).isLessThan(limit);
                    Event lastEvent = getEventClass(eventIdx).getDeclaredConstructor().newInstance();
                    builder.add(Arguments.of(lastEvent, eventClass));
                }
                builder.add(Arguments.of(null, eventClass));
            }
            return builder.build();
        }
        @SneakyThrows
        static Stream<Arguments> isNotBeforeArgs() {
            Stream.Builder<Arguments> builder = Stream.builder();
            for (int limit = 0; limit < DepositEvents.EVENT_ORDER.size(); limit++) {
                Class<? extends Event> eventClass = getEventClass(limit);
                for (int eventIdx = limit; eventIdx < DepositEvents.EVENT_ORDER.size(); eventIdx++) {
                    assertThat(eventIdx).isGreaterThanOrEqualTo(limit);
                    Event lastEvent = getEventClass(eventIdx).getDeclaredConstructor().newInstance();
                    builder.add(Arguments.of(lastEvent, eventClass));
                }
            }
            return builder.build();
        }
        static Stream<Arguments> allEventClasses() {
            Stream.Builder<Arguments> builder = Stream.builder();
            for (int i = 0; i < DepositEvents.EVENT_ORDER.size(); i++) {
                builder.add(Arguments.of(getEventClass(i)));
            }
            return builder.build();
        }
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static Class<? extends Event> getEventClass(int idx) {
        return Class.forName(DepositEvents.EVENT_ORDER.get(idx)).asSubclass(Event.class);
    }
    
    @Test
    void testTransferCompleteIsBeforePackageEncryptComplete() {
        boolean hasBeenSeenBefore = !DepositEvents.INSTANCE.isLastEventBefore(new PackageChunkEncryptComplete(), TransferComplete.class);
        assertThat(hasBeenSeenBefore).isTrue();
    }
}