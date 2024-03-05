package org.datavaultplatform.worker.tasks;

import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

        @TempDir
        private File payloadDir;

        @Mock
        Device mUserFs;

        @BeforeEach
        void setup() throws Exception {
            assertThat(payloadDir).exists();

            File file1 = new File(payloadDir, "file1.txt");
            File file2 = new File(payloadDir, "file2.txt");
            File file3 = new File(payloadDir, "file3.txt");

            writeToFile(file1, "this is file 1");
            writeToFile(file2, "this is file 2");
            writeToFile(file3, "this is file 3");

        }

        private void writeToFile(File file, String contents) throws Exception {
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(contents);
            }
            assertThat(file).exists();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
        void testSucceedsIfNthAttemptWorks(int attemptThatCopyFilesWorks) throws Exception {
            checkCopyFilesToUserFs(attemptThatCopyFilesWorks);
        }

        @SuppressWarnings("CodeBlock2Expr")
        @Test
        void testFailsAfter10FailedAttempts() {
            assertThatThrownBy(() -> {
                checkCopyFilesToUserFs(11);
            }).isInstanceOf(Exception.class).hasMessage("Failing on [file1.txt] attempt[10]");
        }

        void checkCopyFilesToUserFs(int attemptThatCopyWorksOn) throws Exception {
            Progress progress = new Progress();
            Map<String, String> properties = new HashMap<>();
            properties.put(PropNames.USER_FS_RETRIEVE_ATTEMPTS, "10");
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
