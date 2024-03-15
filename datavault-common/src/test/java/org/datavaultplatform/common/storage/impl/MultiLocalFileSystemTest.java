package org.datavaultplatform.common.storage.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import org.assertj.core.util.Files;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.io.Progress;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.common.storage.Verify.Method.COPY_BACK;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiLocalFileSystemTest {

    final ListAppender<ILoggingEvent> logBackListAppender = new ListAppender<>();

    public static final String NO_SUCH_FILE = "no_such_file.txt";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FILE_3 = "file3.txt";
    private static final String FILE_4 = "file4.txt";
    private MultiLocalFileSystem multi;

    @TempDir
    static File multiBaseDir1;

    @TempDir
    static File multiBaseDir2;

    @BeforeAll
    static void checkTempDirs() {

        assertThat(multiBaseDir1).exists();
        assertThat(multiBaseDir2).exists();

        createTempFile(multiBaseDir1, FILE_1);
        createTempFile(multiBaseDir2, FILE_2);
        createTempFile(multiBaseDir1, FILE_3);
        createTempFile(multiBaseDir2, FILE_4);
    }

    private static File createTempFile(File baseDirectory, String filename){
        File tempFile = new File(baseDirectory, filename);
        try(FileWriter fw = new FileWriter(tempFile)){
            fw.write("This is a temp file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempFile;
    }

    @BeforeEach
    void setup() throws Exception {
        Map<String,String> config = new HashMap<>();
        config.put(PropNames.ROOT_PATH, String.format(" %s , %s ", multiBaseDir1.toString(), multiBaseDir2.toString()));
        multi = new MultiLocalFileSystem("test-fs", config);

        logBackListAppender.start();
        getLoggingInterceptorLogbackLogger().addAppender(logBackListAppender);
    }

    @Nested
    class BasicTests {
        @Test
        void testVerifyMethod() {
            assertThat(multi.getVerifyMethod()).isEqualTo(COPY_BACK);
        }

        @Test
        void testLogger() {
            assertThat(multi.getLogger()).isNotNull();
        }

        @Test
        void testHasDepositIdStorageKey() {
            assertThat(multi.hasDepositIdStorageKey()).isFalse();
        }

        @Test
        void testLocations() {
            assertThat(multi.getLocations()).isEqualTo(Arrays.asList(multiBaseDir1.toString(), multiBaseDir2.toString()));
        }

        @Test
        @SneakyThrows
        void testUsableSpace() {
            long usableSpace1 = multi.getUsableSpace();
            assertThat(usableSpace1).isGreaterThan(0);

            File temp = new File(multiBaseDir1, "thousandByteFile");
            try (FileOutputStream boas = new FileOutputStream(temp)) {
                for (int i = 0; i < 1_000; i++) {
                    boas.write(1);
                }
            }
            long usableSpace2 = multi.getUsableSpace();
            assertThat(usableSpace2).isLessThan(usableSpace1);
        }

        @Test
        void testRetrieveWithoutLocationIsNotSupported() {
            assertThrows(UnsupportedOperationException.class, () -> multi.retrieve(null, null, null));
        }
    }


    @Nested
    class ValidationTests  {

        @Test
        void testRootPathPRequired() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new MultiLocalFileSystem("blah", new HashMap<>()));
            assertThat(ex).hasMessage("The config to the MultiLocalFileSystem has a null 'rootPath' value");
        }

        @Test
        void testConfigMapRequired() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new MultiLocalFileSystem("blah", null));
            assertThat(ex).hasMessage("The device config map cannot be null");
        }

        @Test
        void testNameRequired() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new MultiLocalFileSystem(null, new HashMap<>()));
            assertThat(ex).hasMessage("The device name cannot be null");
        }
    }

    @Nested
    class StoreRetrieveTests {

        Progress progress;

        @BeforeEach
        void setup() {
            progress = new Progress();
        }

        String processActualLogMessage(String actualMessage){
            String replace1 = actualMessage.replaceAll(multi.getLocations().get(0), "LOCATION_1");
            String replace2 = replace1.replaceAll(multi.getLocations().get(1), "LOCATION_2");
            String replace3 = replace2.replaceAll("/private","");
            return replace3;
        }


        void checkLogMessages(List<String> expected) {
            List<String> actual = logBackListAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .map(this::processActualLogMessage)
                    .collect(Collectors.toList());
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void testRetrieveFoundOnLocationOne() throws Exception {
            File possibleLocation1 = new File(multi.getLocations().get(0), FILE_1).getCanonicalFile();
            assertThat(possibleLocation1).exists();

            File possibleLocation2 = new File(multi.getLocations().get(1), FILE_1).getCanonicalFile();
            assertThat(possibleLocation2).doesNotExist();


            File temp = getTempFile();
            assertThat(temp).isEmpty();
            multi.retrieve(FILE_1, temp, progress, multi.getLocations());
            assertThat(temp).isNotEmpty();

            assertThat(progress.getFileCount()).isOne();

            checkLogMessages(Arrays.asList("retrieve[file1.txt] from location(1/2)[LOCATION_1] Retrieved!"));
        }


        @Test
        void testRetrieveFoundOnLocationTwo() throws Exception {

            File possibleLocation1 = new File(multi.getLocations().get(0), FILE_2).getCanonicalFile();
            assertThat(possibleLocation1).doesNotExist();

            File possibleLocation2 = new File(multi.getLocations().get(1), FILE_2).getCanonicalFile();
            assertThat(possibleLocation2).exists();

            File temp = getTempFile();
            assertThat(temp).isEmpty();
            multi.retrieve(FILE_2, temp, progress, multi.getLocations());
            assertThat(temp).isNotEmpty();

            assertThat(progress.getFileCount()).isOne();
            checkLogMessages(Arrays.asList(
                    "retrieve[file2.txt] from location(1/2)[LOCATION_1] problem [The file [LOCATION_1/file2.txt] does not exist]",
                    "retrieve[file2.txt] from location(1/2)[LOCATION_1] trying next location...",
                    "retrieve[file2.txt] from location(2/2)[LOCATION_2] Retrieved!"));
        }

        @Test
        void testRetrieveFoundOnAnyLocationThree() throws Exception{
            File temp = getTempFile();
            assertThat(temp).isEmpty();

            File possibleLocation1 = new File(multi.getLocations().get(0), NO_SUCH_FILE).getCanonicalFile();
            assertThat(possibleLocation1).doesNotExist();

            File possibleLocation2 = new File(multi.getLocations().get(1), NO_SUCH_FILE).getCanonicalFile();
            assertThat(possibleLocation2).doesNotExist();

            Exception ex = assertThrows(Exception.class, ()->
                multi.retrieve(NO_SUCH_FILE, temp, progress, multi.getLocations()));
            assertThat(possibleLocation2).doesNotExist();
            assertThat(ex).hasMessage("The file [" + possibleLocation2 + "] does not exist");
            assertThat(progress.getFileCount()).isZero();

            checkLogMessages(Arrays.asList(
                    "retrieve[no_such_file.txt] from location(1/2)[LOCATION_1] problem [The file [LOCATION_1/no_such_file.txt] does not exist]",
                    "retrieve[no_such_file.txt] from location(1/2)[LOCATION_1] trying next location...",
                    "retrieve[no_such_file.txt] from location(2/2)[LOCATION_2] error using final location"));

        }
    }

    private File getTempFile() {
        return Files.newTemporaryFile();
    }

    private ch.qos.logback.classic.Logger getLoggingInterceptorLogbackLogger() {
        ch.qos.logback.classic.Logger result = (ch.qos.logback.classic.Logger) multi.getLogger();
        assertTrue(result.isDebugEnabled());
        return result;
    }

    @AfterEach
    void tearDown() {
        logBackListAppender.stop();
        getLoggingInterceptorLogbackLogger().detachAppender(logBackListAppender);
    }
}