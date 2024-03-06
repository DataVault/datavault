package org.datavaultplatform.worker.tasks;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.worker.retry.TwoSpeedRetry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RetrieveTest {

    @Test
    @SneakyThrows
    void testThrowCheckSumError() {
        File mFile = mock(File.class);
        when(mFile.getCanonicalPath()).thenReturn("/tmp/some/file.1.tar");
        Exception ex = assertThrows(Exception.class, () -> Retrieve.throwChecksumError("<actualCS>", "<expectedCS>",
                mFile, "test"));
        assertEquals("Checksum failed:test:(actual)<actualCS> != (expected)<expectedCS>:/tmp/some/file.1.tar", ex.getMessage());
    }

    @Nested
    class UserFsRetrieveTests {

        private static final int MAX_ATTEMPTS = 4;

        @TempDir
        private File payloadDir;


        ch.qos.logback.classic.Logger getRetryLogger() {
            @SuppressWarnings("UnnecessaryLocalVariable")
            ch.qos.logback.classic.Logger result = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TwoSpeedRetry.class);
            return result;
        }

        final ListAppender<ILoggingEvent> logBackListAppender = new ListAppender<>();

        @Mock
        Device mUserFs;

        @BeforeEach
        void setup() throws Exception {

            logBackListAppender.start();
            getRetryLogger().addAppender(logBackListAppender);


            assertThat(payloadDir).exists();

            File file1 = new File(payloadDir, "file1.txt");
            File file2 = new File(payloadDir, "file2.txt");
            File file3 = new File(payloadDir, "file3.txt");

            writeToFile(file1, "this is file 1");
            writeToFile(file2, "this is file 2");
            writeToFile(file3, "this is file 3");

        }

        @AfterEach
        void tearDown() {
            logBackListAppender.stop();
            getRetryLogger().detachAppender(logBackListAppender);
        }

        private void writeToFile(File file, String contents) throws Exception {
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(contents);
            }
            assertThat(file).exists();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4})
        void testSucceedsIfNthAttemptWorks(int attemptThatCopyFilesWorks) throws Exception {
            checkCopyFilesToUserFs(attemptThatCopyFilesWorks);

            if (attemptThatCopyFilesWorks == MAX_ATTEMPTS) {
                List<String> logMessages = this.logBackListAppender.list.stream()
                        .map(ILoggingEvent::getFormattedMessage)
                        .collect(Collectors.toList());

                assertThat(logMessages).isEqualTo(Arrays.asList(
                        "Task[toUserFs - file1.txt] Initial attempt",
                        "Task[toUserFs - file1.txt] Backoff! attempts so far [1], retry in [010ms]",
                        "Task[toUserFs - file1.txt] Sleeping for [010ms]",
                        // CHANGE TO 2nd Backoff Delay of 20ms
                        "Task[toUserFs - file1.txt] Backoff! attempts so far [2], retry in [020ms]",
                        "Task[toUserFs - file1.txt] Sleeping for [020ms]",
                        "Task[toUserFs - file1.txt] Backoff! attempts so far [3], retry in [020ms]",
                        "Task[toUserFs - file1.txt] Sleeping for [020ms]",
                        "Task[toUserFs - file1.txt] Succeeded after [4] attempts",

                        "Task[toUserFs - file2.txt] Initial attempt",
                        "Task[toUserFs - file2.txt] Backoff! attempts so far [1], retry in [010ms]",
                        "Task[toUserFs - file2.txt] Sleeping for [010ms]",
                        // CHANGE TO 2nd Backoff Delay of 20ms
                        "Task[toUserFs - file2.txt] Backoff! attempts so far [2], retry in [020ms]",
                        "Task[toUserFs - file2.txt] Sleeping for [020ms]",
                        "Task[toUserFs - file2.txt] Backoff! attempts so far [3], retry in [020ms]",
                        "Task[toUserFs - file2.txt] Sleeping for [020ms]",

                        "Task[toUserFs - file2.txt] Succeeded after [4] attempts",
                        "Task[toUserFs - file3.txt] Initial attempt",
                        "Task[toUserFs - file3.txt] Backoff! attempts so far [1], retry in [010ms]",
                        "Task[toUserFs - file3.txt] Sleeping for [010ms]",
                        // CHANGE TO 2nd Backoff Delay of 20ms
                        "Task[toUserFs - file3.txt] Backoff! attempts so far [2], retry in [020ms]",
                        "Task[toUserFs - file3.txt] Sleeping for [020ms]",
                        "Task[toUserFs - file3.txt] Backoff! attempts so far [3], retry in [020ms]",
                        "Task[toUserFs - file3.txt] Sleeping for [020ms]",
                        "Task[toUserFs - file3.txt] Succeeded after [4] attempts"
                ));
            }
        }

        @SuppressWarnings("CodeBlock2Expr")
        @Test
        void testFailsAfter4FailedAttempts() {
            assertThatThrownBy(() -> {
                checkCopyFilesToUserFs(5);
            }).isInstanceOf(Exception.class).hasMessage("Failing on [file1.txt] attempt[4]");
        }

        void checkCopyFilesToUserFs(int attemptThatCopyWorksOn) throws Exception {
            Progress progress = new Progress();
            Map<String, String> properties = new HashMap<>();
            properties.put(PropNames.USER_FS_RETRIEVE_MAX_ATTEMPTS, String.valueOf(MAX_ATTEMPTS));
            properties.put(PropNames.USER_FS_RETRIEVE_DELAY_MS_1, "10");
            properties.put(PropNames.USER_FS_RETRIEVE_DELAY_MS_2, "20");
            Retrieve ret = new Retrieve();
            ret.setupUserFsTwoSpeedRetry(properties);

            Map<File, Integer> attemptCountsPerFile = new HashMap<>();
            doAnswer(invocation -> {
                String path = invocation.getArgument(0, String.class);
                File argFile = invocation.getArgument(1, File.class);
                Progress argProgress = invocation.getArgument(2, Progress.class);
                assertThat(argProgress).isSameAs(progress);

                attemptCountsPerFile.merge(argFile, 1, Integer::sum);

                int attempts = attemptCountsPerFile.get(argFile);

                if (attempts == attemptThatCopyWorksOn) {
                    argProgress.incFileCount(1);
                    return path;
                }
                Path relativePath = payloadDir.toPath().relativize(argFile.toPath());
                throw new Exception(String.format("Failing on [%s] attempt[%d]", relativePath, attempts));

            }).when(mUserFs).store(any(), any(), any());

            ret.copyFilesToUserFs(progress, payloadDir, mUserFs, 123_3456L);
        }
    }
}
