package org.datavaultplatform.broker.queue;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.TestUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.counting;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@ExtendWith(MockitoExtension.class)
class TaskTimerSupportTest {

    record ResetPair(TaskTimerSupport.TaskState before, TaskTimerSupport.TaskState after) {
        ResetPair {
            Assert.isTrue(before != null, "before cannot be null");
            Assert.isTrue(after != null, "after cannot be null");
        }
    }

    static final long SHORT_DELAY_MS = 100;
    static final long TIMEOUT_MS = 300;
    static final Duration SHORT_DELAY = Duration.ofMillis(SHORT_DELAY_MS);
    static final Clock CLOCK = Clock.systemDefaultZone();
    static final Consumer<String> DEFAULT_TIMEOUT_ACTION = id -> log.info("TIMED OUT [{}]", id);
    TaskTimerSupport timerSupport;
    List<String> timersStarted;
    List<ResetPair> timersReset;
    List<String> timersEnded;
    List<String> timersTimedOut;

    @Mock
    Consumer<String> mockAction;

    @BeforeEach
    void setup() {
        timerSupport = getTimerSupport(true);
    }

    private TaskTimerSupport getTimerSupport(boolean enabled) {
        timersReset = new CopyOnWriteArrayList<>();
        timersStarted = new CopyOnWriteArrayList<>();
        timersEnded = new CopyOnWriteArrayList<>();
        timersTimedOut = new CopyOnWriteArrayList<>();
        return new TaskTimerSupport(CLOCK, enabled, TIMEOUT_MS, 5) {

            @Override
            protected void listenTimerStart(TaskData td) {
                log.info("test listener start [{}]", td);
                timersStarted.add(td.getInfo().id());
            }

            @Override
            protected void listenTimerReset(TaskInfo coreData, TaskState before, TaskState after) {
                log.info("test listener reset [{}]", coreData.id());
                timersReset.add(new ResetPair(before, after));
            }

            @Override
            protected void listenTimerEnd(TaskData td) {
                log.info("test listener end [{}]", td);
                timersEnded.add(td.getInfo().id());
            }

            @Override
            protected void listenTimeOut(String label, String id) {
                log.info("test listener timeout label[{}]id[{}]", label, id);
                timersTimedOut.add(id);
            }
        };
    }

    @Order(10)
    @ParameterizedTest
    @ValueSource(strings = {"123", "1234"})
    void testRemoveUnknown(String id) {
        timerSupport.removeTimer(id);
        assertThat(timersStarted).hasSize(0);
        assertThat(timersEnded).hasSize(0);
        assertThat(timersStarted).hasSize(0);
    }

    @Order(20)
    @Test
    void testTimeout() {
        doNothing().when(mockAction).accept(any(String.class));
        timerSupport.startTimer("one", "test", mockAction);
        TestUtils.waitUntil(Duration.ofSeconds(1), timerSupport::isEmpty);
        assertThat(timersStarted).isEqualTo(List.of("one"));
        assertThat(timersEnded).isEmpty();
        assertThat(timersTimedOut).isEqualTo(List.of("one"));
        verify(mockAction).accept(eq("one"));

    }

    @Order(21)
    @Test
    void testTimeoutDisabled() throws InterruptedException {
        lenient().doNothing().when(mockAction).accept(any(String.class));

        var timerSupport = getTimerSupport(false);
        assertThat(timerSupport.isEnabled()).isFalse();

        timerSupport.startTimer("one", "test", mockAction);

        TimeUnit.MILLISECONDS.sleep(TIMEOUT_MS * 2);
        timerSupport.resetTimer("one");

        TimeUnit.MILLISECONDS.sleep(TIMEOUT_MS * 2);
        timerSupport.resetTimer("one");

        TimeUnit.MILLISECONDS.sleep(TIMEOUT_MS * 2);
        timerSupport.removeTimer("one");
        
        assertThat(timersEnded).isEqualTo(List.of("one"));
        assertThat(timersTimedOut).isEmpty();

        //reset twice
        assertThat(timersReset.stream().map(pair -> pair.before.info().id()).toList()).isEqualTo(List.of("one", "one"));

        verify(mockAction, never()).accept(any(String.class));
        
        assertThat(this.timerSupport.isEmpty()).isTrue();
    }

    @Order(22)
    @Test
    void testTimeoutThrowsRuntimeException() {
        timerSupport.startTimer("rte", "test", id -> {
            throw new RuntimeException("oops");
        });
        TestUtils.waitUntil(Duration.ofSeconds(10), timerSupport::isEmpty);

        assertThat(timersStarted).isEqualTo(List.of("rte"));
        assertThat(timersEnded).isEmpty();
        assertThat(timersTimedOut).isEqualTo(List.of("rte"));
    }

    @Order(30)
    @Test
    void testEndedBeforeTimeout() throws InterruptedException {

        AtomicBoolean timedOut = new AtomicBoolean(false);
        timerSupport.startTimer("two", "test", id -> timedOut.set(true));
        TimeUnit.MILLISECONDS.sleep(SHORT_DELAY.toMillis());
        timerSupport.removeTimer("two");

        assertThat(timersStarted).isEqualTo(List.of("two"));
        assertThat(timersEnded).isEqualTo(List.of("two"));
        assertThat(timedOut.get()).isFalse();
    }

    @Order(70)
    @Test
    void testReset2TimesThenComplete() throws Exception {

        AtomicBoolean timedOut = new AtomicBoolean(false);
        timerSupport.startTimer("one", "test", id -> timedOut.set(true));

        TimeUnit.MILLISECONDS.sleep(SHORT_DELAY.toMillis());
        timerSupport.resetTimer("one");

        TimeUnit.MILLISECONDS.sleep(SHORT_DELAY.toMillis());
        timerSupport.resetTimer("one");

        TimeUnit.MILLISECONDS.sleep(SHORT_DELAY.toMillis());
        timerSupport.removeTimer("one");

        assertThat(timersStarted).isEqualTo(List.of("one"));
        assertThat(timersReset.stream().map(pair -> pair.before.info().id()).toList()).isEqualTo(List.of("one", "one"));
        assertThat(timersEnded).isEqualTo(List.of("one"));

        assertThat(timedOut).isFalse();
        assertThat(timersTimedOut).isEmpty();
    }

    private void checkResets() {
        for (ResetPair pair : timersReset) {
            TaskTimerSupport.TaskState before = pair.before;
            TaskTimerSupport.TaskState after = pair.after;

            assertThat(before.info()).isEqualTo(after.info());

            assertThat(before.cancelTimeout()).isNotNull();
            assertThat(after.cancelTimeout()).isNotNull();

            assertThat(before.created()).isBefore(after.created());
            assertThat(after.resetCount()).isEqualTo(after.resetCount() + 1);

            if (before.resetCount() == 0) {
                assertThat(before.created()).isNull();
            } else {
                Duration durationBefore = Duration.between(before.info().created(), before.created());
                checkDuration(durationBefore);
            }
            Duration durationAfter = Duration.between(before.created(), after.created());
            checkDuration(durationAfter);
        }
    }

    private void checkDuration(Duration duration) {
        assertThat(duration).isBetween(Duration.ofMillis(SHORT_DELAY_MS), Duration.ofMillis(3 * TIMEOUT_MS));
    }

    @Order(80)
    @Test
    void testReset2TimesThenTimeout() throws Exception {

        AtomicBoolean timedOut = new AtomicBoolean(false);
        timerSupport.startTimer("one", "test", id -> timedOut.set(true));

        TimeUnit.MILLISECONDS.sleep(SHORT_DELAY.toMillis());
        timerSupport.resetTimer("one");

        TimeUnit.MILLISECONDS.sleep(SHORT_DELAY.toMillis());
        timerSupport.resetTimer("one");

        TestUtils.waitUntil(Duration.ofSeconds(10), timedOut::get);

        assertThat(timersStarted).isEqualTo(List.of("one"));
        assertThat(timersReset.stream().map(pair -> pair.before.info().id()).toList()).isEqualTo(List.of("one", "one"));
        assertThat(timersEnded).isEmpty();

        assertThat(timedOut).isTrue();
        assertThat(timersTimedOut).isEqualTo(List.of("one"));
    }

    @AfterEach
    void tearDown() {
        timerSupport.close();
        assertThat(timerSupport.isEmpty()).isTrue();
        //checkResets();
    }


    @SuppressWarnings("CodeBlock2Expr")
    @Order(40)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Nested
    class ManyTimers {
        static final int LIMIT = 500;

        private List<String> getIds() {
            List<String> ids = IntStream.rangeClosed(1, LIMIT).mapToObj("id-%04d"::formatted).toList();
            Set<String> check = new HashSet<>(ids);
            assertThat(ids).hasSize(LIMIT);
            assertThat(check).hasSize(LIMIT);
            return ids;
        }

        @Order(50)
        @Test
        void testManyTimersNoResetEndedBeforeTimeout() {

            List<String> ids = getIds();
            ids.forEach(id -> timerSupport.startTimer(id, "test", DEFAULT_TIMEOUT_ACTION));
            ids.forEach(id -> timerSupport.removeTimer(id));

            assertThat(timersStarted.size()).isEqualTo(LIMIT);
            assertThat(timersEnded.size()).isEqualTo(LIMIT);
            ids.forEach(id -> assertThat(timersStarted.contains(id)).isTrue());
            ids.forEach(id -> assertThat(timersEnded.contains(id)).isTrue());
        }

        @Order(60)
        @Test
        void testManyTimersNoResetTimedOut() {
            List<String> ids = getIds();

            AtomicInteger timedOut = new AtomicInteger(0);
            Consumer<String> timeoutAction = id -> {
                timedOut.incrementAndGet();
            };
            ids.forEach(id -> timerSupport.startTimer(id, "test", DEFAULT_TIMEOUT_ACTION));
            assertThat(timersStarted.size()).isEqualTo(LIMIT);

            TestUtils.waitUntil(Duration.ofSeconds(15), timerSupport::isEmpty);

            assertThat(timersTimedOut.size()).isEqualTo(LIMIT);
            ids.forEach(id -> assertThat(timersStarted.contains(id)).isTrue());
            ids.forEach(id -> assertThat(timersTimedOut.contains(id)).isTrue());
        }

        @Order(70)
        @Test
        void testManyTimersResetEndedBeforeFinalTimeout() throws Exception {
            List<String> ids = getIds();

            ids.forEach(id -> timerSupport.startTimer(id, "test", DEFAULT_TIMEOUT_ACTION));

            int resets = 50;

            TimeUnit.MILLISECONDS.sleep(SHORT_DELAY.toMillis());
            
            for (int i = 0; i < resets; i++) {
                assertThat(timerSupport.size()).isEqualTo(LIMIT);
                ids.forEach(id -> timerSupport.resetTimer(id));
                assertThat(timerSupport.size()).isEqualTo(LIMIT);
                TimeUnit.MILLISECONDS.sleep(SHORT_DELAY.toMillis());
            }

            ids.forEach(id -> timerSupport.removeTimer(id));
            assertThat(timerSupport.size()).isEqualTo(0);

            assertThat(timersStarted.size()).isEqualTo(LIMIT);

            Map<String, Long> resetCounts = timersReset.stream().collect(Collectors.groupingBy(pair -> pair.before.info().id(), counting()));
            ids.forEach(id -> assertThat(resetCounts.get(id)).isEqualTo(resets));

            assertThat(timersReset.size()).isEqualTo(resets * LIMIT);

            assertThat(timersEnded.size()).isEqualTo(LIMIT);
            ids.forEach(id -> assertThat(timersStarted.contains(id)).isTrue());
            ids.forEach(id -> assertThat(timersEnded.contains(id)).isTrue());
        }

        @Order(80)
        @Test
        void testManyTimersResetButEventualTimeout() throws Exception {
            List<String> ids = getIds();
            AtomicInteger counter = new AtomicInteger(0);
            assertThat(ids.size()).isEqualTo(LIMIT);

            ids.forEach(id -> timerSupport.startTimer(id, "test", $id -> counter.incrementAndGet()));
            TimeUnit.MILLISECONDS.sleep(SHORT_DELAY.toMillis());

            int resets = 50;

            assertThat(timersStarted.size()).isEqualTo(LIMIT);

            for (int i = 0; i < resets; i++) {
                ids.forEach(id -> timerSupport.resetTimer(id));
                TimeUnit.MILLISECONDS.sleep(SHORT_DELAY.toMillis());
            }

            TestUtils.waitUntil(Duration.ofSeconds(20), () -> counter.get() == LIMIT);
            
            TestUtils.waitUntil(Duration.ofSeconds(1), timerSupport::isEmpty);

            assertThat(timersStarted).hasSize(LIMIT);

            Map<String, Long> resetCounts = timersReset.stream().collect(Collectors.groupingBy(pair -> pair.before.info().id(), counting()));
            ids.forEach(id -> assertThat(resetCounts.get(id)).isEqualTo(resets));

            assertThat(timersReset.size()).isEqualTo(resets * LIMIT);

            assertThat(timersEnded).isEmpty();
            ids.forEach(id -> assertThat(timersStarted.contains(id)).isTrue());
        }
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Nested
    class ValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        @NullSource
        void testInitWithBlankId(String blank) {
            RuntimeException rte = assertThrows(RuntimeException.class, () -> {
                timerSupport.startTimer(blank, "test", DEFAULT_TIMEOUT_ACTION);
            });
            assertThat(rte).hasMessage("The id cannot be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        @NullSource
        void testRemoveWithBlankId(String blank) {
            RuntimeException rte = assertThrows(RuntimeException.class, () -> {
                timerSupport.removeTimer(blank);
            });
            assertThat(rte).hasMessage("The id cannot be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        @NullSource
        void testResetWithBlankId(String blank) {
            RuntimeException rte = assertThrows(RuntimeException.class, () -> {
                timerSupport.resetTimer(blank);
            });
            assertThat(rte).hasMessage("The id cannot be blank");
        }

        @Test
        void testInvalidClock() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new TaskTimerSupport(null, true, TIMEOUT_MS, 5);
            });
            assertThat(ex).hasMessage("The clock cannot be null");

        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 1, 99})
        void testInvalidTimeout(int timeoutMs) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new TaskTimerSupport(Clock.systemDefaultZone(), true, timeoutMs, 123);
            });
            assertThat(ex).hasMessage("The timeout cannot be less than 100ms");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0})
        void testInvalidNumberOfThreads(int numberOfThreads) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new TaskTimerSupport(Clock.systemDefaultZone(), true, TIMEOUT_MS, numberOfThreads);
            });
            assertThat(ex).hasMessage("The numberOfThreads must be greater than 0");
        }
    }
}