package org.datavaultplatform.worker.tasks;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.Getter;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.datavaultplatform.common.task.TaskConfig;
import org.datavaultplatform.common.task.TaskConfigTL;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.ProcessHelper;
import org.datavaultplatform.common.util.ProcessInfo;
import org.datavaultplatform.common.util.TestUtils;
import org.datavaultplatform.worker.cleanup.ProcessHelperWithProcessInfoIT;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This COMPLEX integration test creates a multi-level tree of TaskExectors that eventually spawn os processes to run TSM scripts.
 * This simulates the tree of TaskExecutors that is created when we Deposit a multi-chunk archive to multiple archives that have multiple locations (like TivoliStorageManager).
 * Some of these tests get one of the 'TSM simulating shell scripts' to throw an error and check that everything else running in parallel stops as expected.
 * The reason why we need this test is that we have multiple layers of TaskExecutors running parallel tasks.
 * As mentioned above, with Deposit there are 3 layers of TaskExecutor used in storing chunk files for Deposit Worker Task
 * 1. per chunk: see org.datavaultplatform.worker.tasks.deposit.DepositArchiveStoresUploader#uploadToStorage
 * 2. per archiveStore: see org.datavaultplatform.worker.operations.ChunkUploadTracker#call
 * 3. per location: see org.datavaultplatform.common.storage.impl.TivoliStorageManager#store
 * This test does not run on Windows because the way windows process shutdown works is different from Mac/Linux.
 * <p>
 * Most of the complexity in this test class is dynamically creating a tree of TaskExecutors where we can control if/when an error occurs.
 * the TaskExecutors having their internal ExecutorService in a non-terminated state.
 * Some features of TaskExecutors: 1) they are not spring beans 2) they don't share the same "java.lang.Thread" so sharing information between them is not straightforward.
 * Each worker thread within the tree of TaskExecutors has access to the current TaskConfig via TaskConfigTL. Note that TaskExecutor#wrap copies the TaskConfigTL into each child task thread. 
 * We are relying on the fact that there is only one 'tree' of TaskExecutors active in a Worker Task(Deposit/Delete/Retrieve) at a time.
 */
@DisabledOnOs(OS.WINDOWS)
class ComplexWorkerTaskShutdownOnErrorIT {

    public static final Logger LOG = LoggerFactory.getLogger(ComplexWorkerTaskShutdownOnErrorIT.class);
    
    private static final LevelInfo LEVEL_1 = new LevelInfo("level1", 1, 2, 10);
    private static final LevelInfo LEVEL_2 = new LevelInfo("level2", 2, 2, 10);
    private static final LevelInfo LEVEL_3 = new LevelInfo("level3", 3, 2, 10);

    // 10 x 10 X 10 - means there are 1000 Leaf-Actions running at the same time.

    private static final List<LevelInfo> LEVELS_ONE_ONLY = List.of(LEVEL_1);
    private static final List<LevelInfo> LEVELS_ONE_AND_TWO = List.of(LEVEL_1, LEVEL_2);
    private static final List<LevelInfo> LEVELS_ONE_TWO_AND_THREE = List.of(LEVEL_1, LEVEL_2, LEVEL_3);

    /**
     * Appends an element to a list
     *
     * @param list    a list
     * @param element an element to add to the list
     * @return a combined list of list parameter and element parameter
     */
    public static <T> List<T> append(List<T> list, T element) {
        return Stream.concat(list.stream(), Stream.of(element)).toList();
    }

    private static <T> Callable<T> getCallableWithLevel(Callable<T> callable) {
        int currentLevel = TaskExecutorLevel.getLevel();
        return () -> {
            TaskExecutorLevel.initialize(currentLevel + 1);
            try {
                return callable.call();
            } finally {
                //as this thread might be reused - we reset the level
                TaskExecutorLevel.reset();
            }
        };
    }

    @BeforeAll
    static void checkThread() {
        String threadName = Thread.currentThread().getName();
        if (threadName.contains("parallel")) {
            throw new IllegalStateException("This test should only be run with other junit5 tests sequentially - not parallel!");
        }
    }

    @BeforeEach
    void setup() {
        TaskConfigTL.get().setExecutorProperShutdownEnabled(true);
    }

    @AfterEach
    void tearDown() {
        TaskConfigTL.reset();
    }

    private TaskExecutor<TreeNode> getTaskExecutor(LevelInfo info) {
        return new TaskExecutor<>(info.threads, info.desc);
    }

    public TreeNode execute(List<LevelInfo> levels, LeafAction leafAction) throws Exception {
        return execute(new ExecuteContext(ErrorControl.NO_ERRORS, leafAction), levels);
    }

    public TreeNode execute(List<LevelInfo> levels) throws Exception {
        return execute(levels, LeafAction.DEFAULT_ACTION);
    }

    public TreeNode execute(List<LevelInfo> levels, ErrorControl errorControl) throws Exception {
        return execute(new ExecuteContext(errorControl, LeafAction.DEFAULT_ACTION), levels);
    }

    public TreeNode execute(ExecuteContext context, List<LevelInfo> levels) throws Exception {
        return execute(context, List.of(), levels);
    }

    public TreeNode.Branch execute(ExecuteContext context, List<Integer> coords, List<LevelInfo> levels) throws Exception {
        if (levels == null || levels.isEmpty()) {
            return null;
        }
        LevelInfo thisLevel = levels.get(0);
        List<LevelInfo> childLevels = levels.subList(1, levels.size());
        return execute(context, coords, thisLevel, childLevels);
    }

    public TreeNode.Branch execute(ExecuteContext context, List<Integer> coords, LevelInfo thisLevel, List<LevelInfo> childLevels) throws Exception {

        TaskExecutor<TreeNode> executor = getTaskExecutor(thisLevel);
        List<TreeNode> results = new CopyOnWriteArrayList<>();
        for (int i = 0; i < thisLevel.tasks; i++) {
            int taskNum = i + 1;
            List<Integer> newCoords = append(coords, taskNum);

            Callable<TreeNode> callable = getCallableWithLevel(() -> {
                assertThat(thisLevel.level).isEqualTo(TaskExecutorLevel.getLevel());
                if (childLevels.isEmpty()) {
                    String label = "Leaf[%d/%d]@%s".formatted(taskNum, thisLevel.tasks, TaskExecutorLevel.getLevelDescription());
                    context.perform(label, newCoords); //this can throw Exceptions based on the errorControl within 'context'
                    return new TreeNode.Leaf(label, newCoords);
                } else {
                    return execute(context, append(coords, taskNum), childLevels);
                }
            });
            executor.add(callable);
        }
        executor.execute(results::add);
        return new TreeNode.Branch(TaskExecutorLevel.getLevelDescription(), coords, results);
    }

    void checkLeafNodes(LevelInfo leafLevel, List<TreeNode> nodes) {
        assertThat(nodes).hasSize(leafLevel.tasks);
        for (int i = 0; i < leafLevel.tasks; i++) {
            int taskNum = i + 1;
            String expectedLabel = "Leaf[%d/%d]@Level[%d]".formatted(taskNum, leafLevel.tasks, leafLevel.level);
            assertThat(nodes.stream().anyMatch(it -> it.label().startsWith(expectedLabel))).isTrue();
        }
    }

    public void runWithShutdownConfigured(ThrowingRunnable runnable) throws Exception {
        runWithShutdownConfigured(true, runnable);
    }

    public void runWithShutdownConfigured(boolean shutdownProperly, ThrowingRunnable runnable ) throws Exception {
        boolean properShutdownEnabled = TaskConfig.DEFAULT_EXECUTOR_PROPER_SHUTDOWN_ENABLED;
        try {
            if (!shutdownProperly) {
                properShutdownEnabled = TaskConfigTL.get().isExecutorProperShutdownEnabled();
                TaskConfigTL.get().setExecutorProperShutdownEnabled(false);
            }
            runnable.run();
        } finally {
            TaskConfigTL.get().setExecutorProperShutdownEnabled(properShutdownEnabled);
        }
    }

    enum OsScriptType {
        EXIT_SUCCESS(1000, 100, 0, false),
        EXIT_ERROR(1000, 100, 123, false),
        LONG_RUNNING_STOPS_ON_SIGTERM(1000, 20_000, 999, false),
        LONG_RUNNING_DOES_NOT_STOP_ON_SIGTERM(1000, 20_000, 999, true);

        private final long maxProcessDurationMs;
        private final long scriptDelayMs;
        private final int scriptExitCode;
        private final boolean scriptIgnoreSigTerm;

        OsScriptType(long maxProcessDurationMs, long scriptDelayMs, int scriptExitCode, boolean scriptIgnoreSigTerm) {
            this.maxProcessDurationMs = maxProcessDurationMs;
            this.scriptDelayMs = scriptDelayMs;
            this.scriptExitCode = scriptExitCode;
            this.scriptIgnoreSigTerm = scriptIgnoreSigTerm;
        }

        public String getScriptDelayMsStr() {
            return String.valueOf(this.scriptDelayMs);
        }

        public String getMaxProcessDurationMsStr() {
            return String.valueOf(this.maxProcessDurationMs);
        }

        public String getScriptExitCodeStr() {
            return String.valueOf(this.scriptExitCode);
        }

        public String getScriptIgnoreSigTermStr() {
            return this.scriptIgnoreSigTerm ? "yes" : "no";
        }
    }

    public sealed interface TreeNode {

        String label();

        int level();

        record Leaf(String label, int level, List<Integer> coords) implements TreeNode {
            public Leaf(String label, List<Integer> coords) {
                this(label, coords.size(), coords);
            }
        }

        record Branch(String label, int level, List<Integer> coords, List<TreeNode> children) implements TreeNode {
            public Branch(String label, List<Integer> coords, List<TreeNode> children) {
                this(label, coords.size(), coords, children);
            }
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    interface LeafAction {
        LeafAction DEFAULT_ACTION = (ErrorControl errorControl, String leafLabel, List<Integer> coords) -> {
            if (errorControl.willError(coords)) {
                throw new SimulatedTaskException(errorControl, "oops@%s".formatted(errorControl.targetCoords));
            }
        };

        void perform(ErrorControl errorControl, String leafLabel, List<Integer> coords) throws Exception;
    }

    static class OperatingSystemScriptLeafAction implements LeafAction {

        public static final String SCRIPT_PATH = ProcessHelperWithProcessInfoIT.SCRIPT_PATH;
        private final OsScriptType osScriptErrorType;

        OperatingSystemScriptLeafAction(OsScriptType osScriptErrorType) {
            this.osScriptErrorType = osScriptErrorType;
        }

        OperatingSystemScriptLeafAction() {
            this(null);
        }

        private List<String> getBaseCommands(String label, OsScriptType osScriptErrorType, boolean willError) {
            String arg1Label = label;
            String arg2DelayMs;
            String arg3ExitCodeStr;
            String arg4IgnoreSigTerm;
            if (willError) {
                arg2DelayMs = osScriptErrorType.getScriptDelayMsStr();
                arg3ExitCodeStr = osScriptErrorType.getScriptExitCodeStr();
                arg4IgnoreSigTerm = osScriptErrorType.getScriptIgnoreSigTermStr();
            } else {
                arg2DelayMs = OsScriptType.EXIT_SUCCESS.getScriptDelayMsStr();
                arg3ExitCodeStr = OsScriptType.EXIT_SUCCESS.getScriptExitCodeStr();
                arg4IgnoreSigTerm = OsScriptType.EXIT_SUCCESS.getScriptExitCodeStr();
            }
            List<String> baseCommands = List.of(SCRIPT_PATH, arg1Label, arg2DelayMs, arg3ExitCodeStr, arg4IgnoreSigTerm);
            return baseCommands;
        }
        
        private String[] getCommands(String label, OsScriptType osScriptErrorType, boolean willError) {
            final List<String> baseCommands = getBaseCommands(label, osScriptErrorType, willError);
            final List<String> result;
            if (osScriptErrorType == OsScriptType.LONG_RUNNING_DOES_NOT_STOP_ON_SIGTERM && willError) {
                // this is a bit of a workaround because we cannot setup 'script/stdbuf' line buffering prefix to ignore SIGTERM
                // so don't add script/stdbuf line buffering prefix when we want to ignore sigterm
                result = baseCommands;
            } else {
                result = TivoliStorageManager.addLineBufferingPrefix(baseCommands);
                LOG.warn("XXX modified commands {}", result);
            }
            return result.toArray(String[]::new);
        }

        @Override
        public void perform(ErrorControl errorControl, String leafLabel, List<Integer> coords) throws Exception {
            boolean willError = errorControl.willError(coords);
            long maxProcessMs = willError ? osScriptErrorType.maxProcessDurationMs : OsScriptType.EXIT_SUCCESS.maxProcessDurationMs;
            String[] commands = getCommands(leafLabel, osScriptErrorType, willError);
            try {
                
                ProcessInfo info = new ProcessHelper(leafLabel, Duration.ofMillis(maxProcessMs), commands).execute();
                if (info.wasFailure()) {
                    throw new SimulatedTaskException(errorControl, "oops[exitcode=%s]@%s]".formatted(errorControl.targetCoords, info.exitValue()));
                }
            } catch (TimeoutException te) {
                throw new SimulatedTaskException(errorControl, "oops[timeout]@%s".formatted(errorControl.targetCoords), te);
            }
        }
    }

    static class SimulatedTaskException extends Exception {

        @Getter
        private final ErrorControl errorControl;

        public SimulatedTaskException(ErrorControl errorControl, String message, Exception ex) {
            super(message, ex);
            this.errorControl = errorControl;
        }

        public SimulatedTaskException(ErrorControl errorControl, String message) {
            super(message);
            this.errorControl = errorControl;
        }
    }

    /**
     * We make sure each Callable runs with access to TaskExecutorLevel with the correct 'level'
     */
    static class TaskExecutorLevel {
        private static final ThreadLocal<Integer> INSTANCE = ThreadLocal.withInitial(() -> 0);

        public static int getLevel() {
            return INSTANCE.get();
        }

        public static void reset() {
            INSTANCE.remove();
        }

        public static void initialize(int value) {
            INSTANCE.set(value);
        }

        public static String getLevelDescription() {
            return "Level[%d]".formatted(getLevel());
        }
    }

    record ErrorControl(List<Integer> targetCoords, boolean errorRequested) {

        public static final ErrorControl NO_ERRORS = new ErrorControl();

        public ErrorControl {
            Assert.notNull(targetCoords, "targetCoords must not be null");
            Assert.noNullElements(targetCoords, "targetCoords cannot contain null elements");
        }

        public ErrorControl(boolean willError, List<LevelInfo> levels) {
            this(willError ? transform(levels) : List.of(), willError);
        }

        public ErrorControl() {
            this(false, List.of());
        }

        public static int getRandom(int min, int max) {
            if (min > max) {
                throw new IllegalArgumentException("max must be greater than or equal to min");
            }
            return ThreadLocalRandom.current().nextInt(min, max + 1);
        }

        private static List<Integer> transform(List<LevelInfo> levelInfos) {
            return levelInfos.stream()
                    .map(LevelInfo::tasks)
                    .map(taskNum -> getRandom(1, taskNum))
                    .toList();
        }

        public boolean willError(List<Integer> actualCoords) {
            return this.errorRequested && this.targetCoords.equals(actualCoords);
        }
    }

    record ExecuteContext(ErrorControl errorControl, LeafAction leafAction) {

        public void perform(String label, List<Integer> coords) throws Exception {
            leafAction.perform(errorControl, label, coords);
        }
    }

    record LevelInfo(String desc, int level, int threads, int tasks) {
    }

    @Nested
    class FoundationalSuccessTests {

        @Test
        void testLevel1Only() throws Exception {
            runWithShutdownConfigured(() -> {
                TreeNode result = execute(LEVELS_ONE_ONLY);
                System.out.println(result);
                assertThat(result.level()).isZero();

                TreeNode.Branch branch = assertInstanceOf(TreeNode.Branch.class, result);

                checkLeafNodes(LEVEL_1, branch.children());
            });
        }


        @Test
        void testLevels1and2() throws Exception {
            runWithShutdownConfigured(() -> {
                TreeNode result = execute(LEVELS_ONE_AND_TWO);
                System.out.println(result);
                assertThat(result.level()).isZero();

                TreeNode.Branch topLevel = assertInstanceOf(TreeNode.Branch.class, result);

                assertThat(topLevel.children()).hasSize(LEVEL_1.tasks);
                for (int i = 0; i < LEVEL_1.tasks; i++) {
                    TreeNode.Branch taskNumBranch = (TreeNode.Branch) topLevel.children.get(i);
                    checkLeafNodes(LEVEL_2, taskNumBranch.children());
                }
            });
        }

        @Test
        void testLevels1and2and3() throws Exception {
            runWithShutdownConfigured(() -> {
                TreeNode result = execute(LEVELS_ONE_TWO_AND_THREE);
                System.out.println(result);
                assertThat(result.level()).isZero();

                TreeNode.Branch topLevel = assertInstanceOf(TreeNode.Branch.class, result);

                assertThat(topLevel.children()).hasSize(LEVEL_1.tasks);
                for (int i = 0; i < LEVEL_1.tasks; i++) {
                    TreeNode.Branch taskNumBranch1 = (TreeNode.Branch) topLevel.children.get(i);
                    for (TreeNode taskNumBranch1child : taskNumBranch1.children) {
                        TreeNode.Branch level2 = (TreeNode.Branch) taskNumBranch1child;
                        checkLeafNodes(LEVEL_3, level2.children);
                    }
                }
            });
        }
    }

    @Nested
    class FoundationalErrorTests {

        void checkErrorForTestLevels(ErrorControl errorControl, List<LevelInfo> testLevels) {
            var simulatedTaskException = assertThrows(SimulatedTaskException.class, () -> {
                runWithShutdownConfigured(() -> {
                    execute(testLevels, errorControl);
                });
            });
            assertThat(simulatedTaskException.getErrorControl()).isEqualTo(errorControl);
        }

        @Nested
        class NonRandomErrors {

            static final ErrorControl ERROR_AT_FINAL_TASK = new ErrorControl(List.of(LEVEL_1.tasks, LEVEL_2.tasks, LEVEL_3.tasks), true);

            static final ErrorControl ERROR_AT_FIRST_TASK = new ErrorControl(List.of(1,1,1), true);

            List<String> failAtFirstTask(boolean properShutdown) throws Exception {
                return failAtSpecificTask(properShutdown, ERROR_AT_FIRST_TASK);
            }
            List<String> failAtFinalTask(boolean properShutdown) throws Exception {
                return failAtSpecificTask(properShutdown, ERROR_AT_FINAL_TASK);
            }
            List<String> failAtSpecificTask(boolean properShutdown, ErrorControl errorControl) throws Exception {
                List<ILoggingEvent> loggingEvents = TestUtils.captureLogging(TaskExecutor.class, () -> {
                    var simulatedTaskException = assertThrows(SimulatedTaskException.class, () -> {
                        runWithShutdownConfigured(properShutdown, () -> {
                            execute(LEVELS_ONE_TWO_AND_THREE, errorControl);
                        });
                    });
                    assertThat(simulatedTaskException.getErrorControl()).isEqualTo(errorControl);
                });
                Map<Boolean, List<String>> terminatedMessages = loggingEvents.stream()
                        .map(ILoggingEvent::getFormattedMessage)
                        .filter(it -> it.contains("Terminated?"))
                        .collect(Collectors.partitioningBy(s -> s.contains("Terminated?[true]")));
                List<String> notTerminated = terminatedMessages.get(false);
                return notTerminated;
            }

            @Test
            void testErrorAtFirstTaskWithoutProperShutdown() throws Exception {
                List<String> notTerminated = failAtFirstTask(false);
                assertThat(notTerminated).isNotEmpty();
                assertThat(notTerminated.stream().allMatch(it -> it.contains("Terminated?[false]"))).isTrue();
            }

            @Test
            void testErrorAtFinalTaskWithProperShutdown() throws Exception {
                List<String> notTerminated = failAtFinalTask(true);
                assertThat(notTerminated).isEmpty();
            }
        }

        @Nested
        @EnabledOnOs(OS.MAC)
        class RandomErrors {

            void checkRandomErrorForTestLevels(List<LevelInfo> testLevels) {
                ErrorControl errorControl = new ErrorControl(true, testLevels);
                checkErrorForTestLevels(errorControl, testLevels);
            }


            @Test
            void testRandomLevel1Only() {
                checkRandomErrorForTestLevels(LEVELS_ONE_ONLY);
            }


            @Test
            void testRandomLevels1and2() {
                checkRandomErrorForTestLevels(LEVELS_ONE_AND_TWO);
            }

            @Test
            void testRandomLevels1and2and3() {
                checkRandomErrorForTestLevels(LEVELS_ONE_TWO_AND_THREE);
            }
        }
    }

    @Nested
    class OperatingSystemScriptSuccessTests {

        final LeafAction osScriptAction = new OperatingSystemScriptLeafAction();

        @Test
        void testLevel1Only() throws Exception {
            runWithShutdownConfigured(() -> {
                TreeNode result = execute(LEVELS_ONE_ONLY, osScriptAction);
                System.out.println(result);
                assertThat(result.level()).isZero();

                TreeNode.Branch branch = assertInstanceOf(TreeNode.Branch.class, result);

                checkLeafNodes(LEVEL_1, branch.children());
            });
        }

        @Test
        void testLevels1and2() throws Exception {
            runWithShutdownConfigured(() -> {
                TreeNode result = execute(LEVELS_ONE_AND_TWO, osScriptAction);
                System.out.println(result);
                assertThat(result.level()).isZero();

                TreeNode.Branch topLevel = assertInstanceOf(TreeNode.Branch.class, result);

                assertThat(topLevel.children()).hasSize(LEVEL_1.tasks);
                for (int i = 0; i < LEVEL_1.tasks; i++) {
                    TreeNode.Branch taskNumBranch = (TreeNode.Branch) topLevel.children.get(i);
                    checkLeafNodes(LEVEL_2, taskNumBranch.children());
                }
            });
        }

        @Test
        void testLevels1and2and3() throws Exception {
            runWithShutdownConfigured(() -> {
                TreeNode result = execute(LEVELS_ONE_TWO_AND_THREE, osScriptAction);
                System.out.println(result);
                assertThat(result.level()).isZero();

                TreeNode.Branch topLevel = assertInstanceOf(TreeNode.Branch.class, result);

                assertThat(topLevel.children()).hasSize(LEVEL_1.tasks);
                for (int i = 0; i < LEVEL_1.tasks; i++) {
                    TreeNode.Branch taskNumBranch1 = (TreeNode.Branch) topLevel.children.get(i);
                    for (TreeNode taskNumBranch1child : taskNumBranch1.children) {
                        TreeNode.Branch level2 = (TreeNode.Branch) taskNumBranch1child;
                        checkLeafNodes(LEVEL_3, level2.children);
                    }
                }
            });
        }
    }

    @Nested
    class OperatingSystemErrorTests {

        static final OperatingSystemScriptLeafAction OS_SCRIPT_ACTION_EXIT_VIA_SIGTERM = new OperatingSystemScriptLeafAction(OsScriptType.LONG_RUNNING_STOPS_ON_SIGTERM);
        static final OperatingSystemScriptLeafAction OS_SCRIPT_ACTION_EXIT_WITH_ERROR_CODE = new OperatingSystemScriptLeafAction(OsScriptType.EXIT_ERROR);
        static final OperatingSystemScriptLeafAction OS_SCRIPT_ACTION_EXIT_REQUIRES_SIGKILL = new OperatingSystemScriptLeafAction(OsScriptType.LONG_RUNNING_DOES_NOT_STOP_ON_SIGTERM);

        static final List<LevelInfo> TEST_LEVELS = List.of(
                new LevelInfo("level1", 1, 2, 5),
                new LevelInfo("level2", 2, 2, 5),
                new LevelInfo("level3", 3, 2, 10)); //250

        ErrorControl getErrorControlForFinalTask(List<LevelInfo> levels) {
            return new ErrorControl(levels.stream().map(LevelInfo::tasks).toList(), true);
        }

        ErrorControl getErrorControlForFirstTask(List<LevelInfo> levels) {
            return new ErrorControl(levels.stream().map(it -> 1).toList(), true);
        }

        List<String> failAtFinalTask(boolean properShutdown, OperatingSystemScriptLeafAction osLeafAction, List<LevelInfo> testLevels) throws Exception {
            ErrorControl errorControl = getErrorControlForFinalTask(testLevels);
            return failAtSpecificTask(errorControl, properShutdown, osLeafAction, testLevels);
        }

        List<String> failAtFirstTask(boolean properShutdown, OperatingSystemScriptLeafAction osLeafAction, List<LevelInfo> testLevels) throws Exception {
            ErrorControl errorControl = getErrorControlForFirstTask(testLevels);
            return failAtSpecificTask(errorControl, properShutdown, osLeafAction, testLevels);
        }

        List<String> failAtSpecificTask(ErrorControl errorControl, boolean properShutdown, OperatingSystemScriptLeafAction osLeafAction, List<LevelInfo> testLevels) throws Exception {
            List<ILoggingEvent> loggingEvents = TestUtils.captureLogging(TaskExecutor.class, () -> {
                var simulatedTaskException = assertThrows(SimulatedTaskException.class, () -> {
                    runWithShutdownConfigured(properShutdown, () -> {
                        execute(new ExecuteContext(errorControl, osLeafAction), testLevels);
                    });
                });
                assertThat(simulatedTaskException.getErrorControl()).isEqualTo(errorControl);
                if (osLeafAction.osScriptErrorType == OsScriptType.LONG_RUNNING_DOES_NOT_STOP_ON_SIGTERM || osLeafAction.osScriptErrorType == OsScriptType.LONG_RUNNING_STOPS_ON_SIGTERM) {
                    assertThat(simulatedTaskException).hasCauseInstanceOf(TimeoutException.class);
                    TimeoutException ex = (TimeoutException) simulatedTaskException.getCause();
                    if (OsScriptType.LONG_RUNNING_STOPS_ON_SIGTERM == osLeafAction.osScriptErrorType) {
                        assertThat(ex.getMessage()).contains("forcedToShutdown[false]");
                    } else {
                        assertThat(ex.getMessage()).contains("forcedToShutdown[true]");
                    }
                }
            });
            Map<Boolean, List<String>> terminatedMessages = loggingEvents.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .filter(it -> it.contains("Terminated?"))
                    .collect(Collectors.partitioningBy(s -> s.contains("Terminated?[true]")));
            List<String> notTerminated = terminatedMessages.get(false);
            return notTerminated;
        }

        void checkErrorAtFirstTaskWithoutProperShutdown(OperatingSystemScriptLeafAction osLeafAction, List<LevelInfo> testLevels) throws Exception {
            List<String> notTerminated = failAtFirstTask(false, osLeafAction, testLevels);
            assertThat(notTerminated).isNotEmpty();
            assertThat(notTerminated.stream().allMatch(it -> it.contains("Terminated?[false]"))).isTrue();
        }

        void checkErrorAtFinalTaskWithProperShutdown(OperatingSystemScriptLeafAction osLeafAction, List<LevelInfo> testLevels) throws Exception {
            List<String> notTerminated = failAtFinalTask(true, osLeafAction, testLevels);
            assertThat(notTerminated).isEmpty();
        }

        @Nested
        class NonRandomErrors {
            
            @Nested
            class ExitWithSigTerm {

                @Test
                void testErrorAtFinalTaskWithProperShutdownWhenStopsOnSigTerm() throws Exception {
                    checkErrorAtFinalTaskWithProperShutdown(OS_SCRIPT_ACTION_EXIT_VIA_SIGTERM, TEST_LEVELS);
                }

                @Test
                void testErrorAtFirstTaskWithoutProperShutdownWhenStopsOnSigTerm() throws Exception {
                    checkErrorAtFirstTaskWithoutProperShutdown(OS_SCRIPT_ACTION_EXIT_VIA_SIGTERM, TEST_LEVELS);
                }
            }

            @Nested
            class ExitWithSigKill {

                @BeforeEach
                void setup() {
                    TaskConfigTL.get().setProcessSigTermTimeoutDuration(Duration.ofSeconds(2));
                }

                @Test
                void testErrorAtFinalTaskWithProperShutdownWhenStopsOnSigKill() throws Exception {
                    checkErrorAtFinalTaskWithProperShutdown(OS_SCRIPT_ACTION_EXIT_REQUIRES_SIGKILL, TEST_LEVELS);
                }

                @Test
                void testErrorAtFirstTaskWithoutProperShutdownWhenStopsOnSigKill() throws Exception {
                    checkErrorAtFirstTaskWithoutProperShutdown(OS_SCRIPT_ACTION_EXIT_REQUIRES_SIGKILL, TEST_LEVELS);
                }
            }

            @Nested
            class ExitWithExitCode {

                @Test
                void testErrorAtFinalTaskWithProperShutdownWhenStoppedViaExitCode() throws Exception {
                    checkErrorAtFinalTaskWithProperShutdown(OS_SCRIPT_ACTION_EXIT_WITH_ERROR_CODE, TEST_LEVELS);
                }

                @Test
                void testErrorAtFirstTaskWithoutProperShutdownWhenStoppedViaExitCode() throws Exception {
                    checkErrorAtFirstTaskWithoutProperShutdown(OS_SCRIPT_ACTION_EXIT_WITH_ERROR_CODE, TEST_LEVELS);
                }
            }
        }

        @Nested
        @EnabledOnOs(OS.MAC)
        class RandomErrors {

            @Test
            void testFailAtRandomTaskWithExitCodeWithProperShutdown() throws  Exception {
                ErrorControl random = new ErrorControl(true, TEST_LEVELS);
                List<String> result = failAtSpecificTask(random, true, OS_SCRIPT_ACTION_EXIT_WITH_ERROR_CODE, TEST_LEVELS);
                assertThat(result).isEmpty();
            }

            @Test
            void testFailAtRandomTaskViaSigTermWithProperShutdown() throws  Exception {
                ErrorControl random = new ErrorControl(true, TEST_LEVELS);
                List<String> result = failAtSpecificTask(random, true, OS_SCRIPT_ACTION_EXIT_VIA_SIGTERM, TEST_LEVELS);
                assertThat(result).isEmpty();
            }

            @Test
            void testFailAtRandomTaskViaSigKillWithProperShutdown() throws  Exception {
                TaskConfigTL.get().setProcessSigTermTimeoutDuration(Duration.ofSeconds(2));
                ErrorControl random = new ErrorControl(true, TEST_LEVELS);
                List<String> result = failAtSpecificTask(random, true, OS_SCRIPT_ACTION_EXIT_REQUIRES_SIGKILL, TEST_LEVELS);
                assertThat(result).isEmpty();
            }
        }
    }

}
