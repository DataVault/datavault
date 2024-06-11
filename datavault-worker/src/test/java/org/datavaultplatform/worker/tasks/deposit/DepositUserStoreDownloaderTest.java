package org.datavaultplatform.worker.tasks.deposit;

import lombok.SneakyThrows;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.event.deposit.TransferComplete;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.task.Context;
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

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositUserStoreDownloaderTest {

    public static final String USER_ID = "test-user-id";
    public static final String BAG_ID = "teat-bag-id";
    public static final String DEPOSIT_ID = "test-deposit-id";
    public static final String JOB_ID = "test-job-id";
    public static final String DATA = "data";

    private static final String ONE_A = "one a";
    private static final String ONE_B = "one b";
    private static final String TWO_A = "two a";
    private static final String TWO_B = "two b";
    private static final String AA = "aa";
    private static final String BB = "bb";
    private static final String USER_STORE_1 = "userStore1";
    private static final String USER_STORE_MISSING = "userStoreMissing";

    @Captor
    ArgumentCaptor<Event> argEvent;

    List<String> fileStorePaths = new ArrayList<>();

    static final UserEventSender SENDER = Mockito.mock(UserEventSender.class);
    static final Context CONTEXT = Mockito.mock(Context.class);
    static final Map<String, String> PROPS = new HashMap<>();

    final Map<String, UserStore> userStores = new HashMap<>();

    UserStore userStore1;

    Path tempDir;
    Path rootDir;

    final Path faa1a = Paths.get("aa/one/1a.txt");
    final Path faa1b = Paths.get("aa/one/1b.txt");
    final Path faa2a = Paths.get("aa/two/2a.txt");
    final Path faa2b = Paths.get("aa/two/2b.txt");

    final Path fbb1a = Paths.get("bb/one/1a.txt");
    final Path fbb1b = Paths.get("bb/one/1b.txt");
    final Path fbb2a = Paths.get("bb/two/2a.txt");
    final Path fbb2b = Paths.get("bb/two/2b.txt");


    final Path bag1aa1a = Paths.get("1/aa/one/1a.txt");
    final Path bag1aa1b = Paths.get("1/aa/one/1b.txt");
    final Path bag1aa2a = Paths.get("1/aa/two/2a.txt");
    final Path bag1aa2b = Paths.get("1/aa/two/2b.txt");

    final Path bag2bb1a = Paths.get("2/bb/one/1a.txt");
    final Path bag2bb1b = Paths.get("2/bb/one/1b.txt");
    final Path bag2bb2a = Paths.get("2/bb/two/2a.txt");
    final Path bag2bb2b = Paths.get("2/bb/two/2b.txt");
    
    Path uploadPath1;
    Path uploadPath2;
    
    @BeforeEach
    @SneakyThrows
    void setup() {
        tempDir = Files.createTempDirectory("test");
        rootDir = Files.createTempDirectory("root");

        checkPath(rootDir, faa1a, false);
        checkPath(rootDir, faa1b, false);
        checkPath(rootDir, faa2a, false);
        checkPath(rootDir, faa2b, false);

        writeRelativeFile(rootDir, faa1a, ONE_A);
        writeRelativeFile(rootDir, faa1b, ONE_B);
        writeRelativeFile(rootDir, faa2a, TWO_A);
        writeRelativeFile(rootDir, faa2b, TWO_B);

        checkPath(rootDir, fbb1a, false);
        checkPath(rootDir, fbb1b, false);
        checkPath(rootDir, fbb2a, false);
        checkPath(rootDir, fbb2b, false);

        writeRelativeFile(rootDir, fbb1a, ONE_A);
        writeRelativeFile(rootDir, fbb1b, ONE_B);
        writeRelativeFile(rootDir, fbb2a, TWO_A);
        writeRelativeFile(rootDir, fbb2b, TWO_B);

        Map<String, String> userStore1props = new HashMap<>();
        userStore1props.put(PropNames.ROOT_PATH, rootDir.toString());

        userStore1 = new LocalFileSystem(AA, userStore1props) {
            @Override
            public String toString() {
                return "test-local-file-system";
            }
        };

        userStores.put(USER_STORE_1, userStore1);

        lenient().when(CONTEXT.getTempDir()).thenReturn(tempDir);
        lenient().doNothing().when(SENDER).send(argEvent.capture());
        
        uploadPath1 = tempDir.getParent().resolve("uploads").resolve(USER_ID).resolve("upload1");
        Files.createDirectories(uploadPath1);

        uploadPath2 = tempDir.getParent().resolve("uploads").resolve(USER_ID).resolve("upload2");
        Files.createDirectories(uploadPath2);
        
        writeRelativeFile(uploadPath1, Path.of("1a.txt"), ONE_A);
        writeRelativeFile(uploadPath1, Path.of("1b.txt"), ONE_B);

        writeRelativeFile(uploadPath2, Path.of("2a.txt"), TWO_A);
        writeRelativeFile(uploadPath2, Path.of("2b.txt"), TWO_B);
    }


    @Test
    @SneakyThrows
    void testDownloadZeroFileStorePaths() {

        Path bagIdPath = tempDir.resolve(BAG_ID);
        assertThat(Files.exists(bagIdPath)).isFalse();
        Path bagIdDataPath = bagIdPath.resolve(DATA);
        assertThat(Files.exists(bagIdDataPath)).isFalse();

        assertThat(fileStorePaths).isEmpty();

        var downloader = new DepositUserStoreDownloader(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, CONTEXT, null, PROPS,
                fileStorePaths, userStores, null);

        checkTextFiles(bagIdDataPath, 0);

        File downloadResult = downloader.transferFromUserStoreToWorker();

        checkTextFiles(bagIdDataPath, 0);

        checkBagDataDirectoryCreated(downloadResult, bagIdPath, bagIdDataPath);

        checkTransferComplete();
    }


    @Test
    @SneakyThrows
    void testDownloadOneFileStorePaths() {

        checkPath(rootDir, faa1a, true);
        checkPath(rootDir, faa1b, true);
        checkPath(rootDir, faa2a, true);
        checkPath(rootDir, faa2b, true);

        Path bagIdPath = tempDir.resolve(BAG_ID);
        assertThat(Files.exists(bagIdPath)).isFalse();
        Path bagIdDataPath = bagIdPath.resolve(DATA);
        assertThat(Files.exists(bagIdDataPath)).isFalse();

        checkPath(bagIdDataPath, faa1a, false);
        checkPath(bagIdDataPath, faa1b, false);
        checkPath(bagIdDataPath, faa2a, false);
        checkPath(bagIdDataPath, faa2b, false);

        fileStorePaths.add(USER_STORE_1 + "/" + AA);

        var downloader = new DepositUserStoreDownloader(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, CONTEXT, null, PROPS,
                fileStorePaths, userStores, null);

        checkTextFiles(bagIdDataPath, 0);

        File downloadResult = downloader.transferFromUserStoreToWorker();

        checkBagDataDirectoryCreated(downloadResult, bagIdPath, bagIdDataPath);

        checkTransferComplete();

        checkTextFiles(bagIdDataPath, 4);

        checkPath(bagIdDataPath, faa1a, true);
        checkPath(bagIdDataPath, faa1b, true);
        checkPath(bagIdDataPath, faa2a, true);
        checkPath(bagIdDataPath, faa2b, true);

        checkContents(bagIdDataPath, faa1a, ONE_A);
        checkContents(bagIdDataPath, faa1b, ONE_B);
        checkContents(bagIdDataPath, faa2a, TWO_A);
        checkContents(bagIdDataPath, faa2b, TWO_B);
    }

    @Test
    @SneakyThrows
    void testFileStorePathDoesNotExist() {

        checkPath(rootDir, faa1a, true);
        checkPath(rootDir, faa1b, true);
        checkPath(rootDir, faa2a, true);
        checkPath(rootDir, faa2b, true);

        Path bagIdPath = tempDir.resolve(BAG_ID);
        assertThat(Files.exists(bagIdPath)).isFalse();
        Path bagIdDataPath = bagIdPath.resolve(DATA);
        assertThat(Files.exists(bagIdDataPath)).isFalse();

        checkPath(bagIdDataPath, faa1a, false);
        checkPath(bagIdDataPath, faa1b, false);
        checkPath(bagIdDataPath, faa2a, false);
        checkPath(bagIdDataPath, faa2b, false);

        fileStorePaths.add(USER_STORE_1 + "/" + "MISSING");

        var downloader = new DepositUserStoreDownloader(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, CONTEXT, null, PROPS,
                fileStorePaths, userStores, null);

        checkTextFiles(bagIdDataPath, 0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> downloader.transferFromUserStoreToWorker());

        assertThat(ex).hasMessage("java.lang.RuntimeException: StoragePath[MISSING] does not exist on userStore[test-local-file-system]");

        checkErrorEvent("Deposit failed: StoragePath[MISSING] does not exist on userStore[test-local-file-system]");        
    }

    @Test
    @SneakyThrows
    void testDownloadOneFileStorePathsAndUploads() {

        checkPath(rootDir, faa1a, true);
        checkPath(rootDir, faa1b, true);
        checkPath(rootDir, faa2a, true);
        checkPath(rootDir, faa2b, true);

        Path bagIdPath = tempDir.resolve(BAG_ID);
        assertThat(Files.exists(bagIdPath)).isFalse();
        Path bagIdDataPath = bagIdPath.resolve(DATA);
        assertThat(Files.exists(bagIdDataPath)).isFalse();

        checkPath(bagIdDataPath, faa1a, false);
        checkPath(bagIdDataPath, faa1b, false);
        checkPath(bagIdDataPath, faa2a, false);
        checkPath(bagIdDataPath, faa2b, false);

        checkPath(bagIdDataPath, Path.of("uploads/upload1/1a.txt"), false);
        checkPath(bagIdDataPath, Path.of("uploads/upload1/1b.txt"), false);
        checkPath(bagIdDataPath, Path.of("uploads/upload2/2a.txt"), false);
        checkPath(bagIdDataPath, Path.of("uploads/upload2/2b.txt"), false);

        fileStorePaths.add(USER_STORE_1 + "/" + AA);

        var downloader = new DepositUserStoreDownloader(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, CONTEXT, null, PROPS,
                fileStorePaths, userStores, List.of("upload1","upload2","missing"));

        checkTextFiles(bagIdDataPath, 0);

        Path uploadPath1 = tempDir.getParent().resolve("uploads").resolve(USER_ID).resolve("upload1");
        assertThat(uploadPath1).exists();
        assertThat(uploadPath1).isDirectory();
        checkTextFiles(uploadPath1, 2);

        Path uploadPath2 = tempDir.getParent().resolve("uploads").resolve(USER_ID).resolve("upload2");
        assertThat(uploadPath2).exists();
        assertThat(uploadPath2).isDirectory();
        checkTextFiles(uploadPath1, 2);

        File downloadResult = downloader.transferFromUserStoreToWorker();

        checkBagDataDirectoryCreated(downloadResult, bagIdPath, bagIdDataPath);

        checkTransferComplete();

        checkTextFiles(bagIdDataPath, 8);
        checkTextFiles(bagIdDataPath.resolve(AA), 4);
        checkTextFiles(bagIdDataPath.resolve("uploads"), 4);

        // the upload1 and upload2 directories have been moved
        assertThat(uploadPath1).doesNotExist();
        assertThat(uploadPath2).doesNotExist();

        checkPath(bagIdDataPath, faa1a, true);
        checkPath(bagIdDataPath, faa1b, true);
        checkPath(bagIdDataPath, faa2a, true);
        checkPath(bagIdDataPath, faa2b, true);

        checkContents(bagIdDataPath, faa1a, ONE_A);
        checkContents(bagIdDataPath, faa1b, ONE_B);
        checkContents(bagIdDataPath, faa2a, TWO_A);
        checkContents(bagIdDataPath, faa2b, TWO_B);
        
        checkPath(bagIdDataPath, Path.of("uploads/upload1/1a.txt"), true);
        checkPath(bagIdDataPath, Path.of("uploads/upload1/1b.txt"), true);
        checkPath(bagIdDataPath, Path.of("uploads/upload2/2a.txt"), true);
        checkPath(bagIdDataPath, Path.of("uploads/upload2/2b.txt"), true);

        checkContents(bagIdDataPath, Path.of("uploads/upload1/1a.txt"), ONE_A);
        checkContents(bagIdDataPath, Path.of("uploads/upload1/1b.txt"), ONE_B);
        checkContents(bagIdDataPath, Path.of("uploads/upload2/2a.txt"), TWO_A);
        checkContents(bagIdDataPath, Path.of("uploads/upload2/2b.txt"), TWO_B);
    }

    @Test
    @SneakyThrows
    void testDownloadTwofileStorePaths() {

        checkPath(rootDir, faa1a, true);
        checkPath(rootDir, faa1b, true);
        checkPath(rootDir, faa2a, true);
        checkPath(rootDir, faa2b, true);

        checkPath(rootDir, fbb1a, true);
        checkPath(rootDir, fbb1b, true);
        checkPath(rootDir, fbb2a, true);
        checkPath(rootDir, fbb2b, true);

        Path bagIdPath = tempDir.resolve(BAG_ID);
        assertThat(Files.exists(bagIdPath)).isFalse();
        Path bagIdDataPath = bagIdPath.resolve(DATA);
        assertThat(Files.exists(bagIdDataPath)).isFalse();

        checkPath(bagIdDataPath, bag1aa1a, false);
        checkPath(bagIdDataPath, bag1aa1b, false);
        checkPath(bagIdDataPath, bag1aa2a, false);
        checkPath(bagIdDataPath, bag1aa2b, false);

        checkPath(bagIdDataPath, bag2bb1a, false);
        checkPath(bagIdDataPath, bag2bb1b, false);
        checkPath(bagIdDataPath, bag2bb2a, false);
        checkPath(bagIdDataPath, bag2bb2b, false);

        fileStorePaths.add(USER_STORE_1 + "/" + AA);
        fileStorePaths.add(USER_STORE_1 + "/" + BB);

        checkTextFiles(bagIdDataPath, 0);

        var downloader = new DepositUserStoreDownloader(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, CONTEXT, null, PROPS,
                fileStorePaths, userStores, null);

        checkTextFiles(bagIdDataPath, 0);

        File downloadResult = downloader.transferFromUserStoreToWorker();

        checkBagDataDirectoryCreated(downloadResult, bagIdPath, bagIdDataPath);

        checkTransferComplete();

        checkTextFiles(bagIdDataPath, 8);

        checkPath(bagIdDataPath, bag1aa1a, true);
        checkPath(bagIdDataPath, bag1aa1b, true);
        checkPath(bagIdDataPath, bag1aa2a, true);
        checkPath(bagIdDataPath, bag1aa2b, true);

        checkPath(bagIdDataPath, bag2bb1a, true);
        checkPath(bagIdDataPath, bag2bb1b, true);
        checkPath(bagIdDataPath, bag2bb2a, true);
        checkPath(bagIdDataPath, bag2bb2b, true);

        checkContents(bagIdDataPath, bag1aa1a, ONE_A);
        checkContents(bagIdDataPath, bag1aa1b, ONE_B);
        checkContents(bagIdDataPath, bag1aa2a, TWO_A);
        checkContents(bagIdDataPath, bag1aa2b, TWO_B);

        checkContents(bagIdDataPath, bag2bb1a, ONE_A);
        checkContents(bagIdDataPath, bag2bb1b, ONE_B);
        checkContents(bagIdDataPath, bag2bb2a, TWO_A);
        checkContents(bagIdDataPath, bag2bb2b, TWO_B);
    }

    private void checkTextFiles(Path bagIdDataPath, int expectedSize) {
        List<File> files = IteratorUtils.toList(find(bagIdDataPath, "txt"));
        assertThat(files.size()).isEqualTo(expectedSize);
    }

    private void checkTransferComplete() {
        List<Event> nonProgressEvents = argEvent.getAllValues().stream().filter(e -> !(e instanceof UpdateProgress)).toList();
        assertThat(nonProgressEvents).hasSize(1);
        TransferComplete tc = (TransferComplete) nonProgressEvents.get(0);
        assertThat(tc.depositId).isEqualTo(DEPOSIT_ID);
        assertThat(tc.jobId).isEqualTo(JOB_ID);
        assertThat(tc.eventClass).isEqualTo(TransferComplete.class.getName());
        assertThat(tc.message).isEqualTo("File transfer completed");
    }
    
    private void checkErrorEvent(String msg) {
        List<Event> nonProgressEvents = argEvent.getAllValues().stream().filter(e -> !(e instanceof UpdateProgress)).toList();
        assertThat(nonProgressEvents).hasSize(1);
        Error error = (Error) nonProgressEvents.get(0);
        assertThat(error.depositId).isEqualTo(DEPOSIT_ID);
        assertThat(error.jobId).isEqualTo(JOB_ID);
        assertThat(error.eventClass).isEqualTo(Error.class.getName());
        assertThat(error.message).isEqualTo(msg);
    }

    private void checkBagDataDirectoryCreated(File downloadResult, Path bagIdPath, Path bagIdDataPath) {
        assertThat(Files.exists(bagIdPath)).isTrue();
        assertThat(Files.isDirectory(bagIdPath)).isTrue();

        assertThat(Files.exists(bagIdDataPath)).isTrue();
        assertThat(Files.isDirectory(bagIdDataPath)).isTrue();
        assertThat(downloadResult).isEqualTo(bagIdPath.toFile());
    }

    Iterator<File> find(Path startPath, String extension) {
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        return FileUtils.iterateFiles(
                startPath.toFile(),
                WildcardFileFilter.builder().setWildcards("*" + extension).get(),
                TrueFileFilter.INSTANCE);
    }

    @SneakyThrows
    void writeContents(Path path, String contents) {
        Files.createDirectories(path.getParent());
        try (PrintWriter pw = new PrintWriter(path.toFile(), StandardCharsets.UTF_8)) {
            pw.print(contents);
        }
    }

    @SneakyThrows
    String readContents(Path path) {
        return PathUtils.readString(path, StandardCharsets.UTF_8);
    }

    void writeRelativeFile(Path base, Path relative, String contents) {
        Path path = base.resolve(relative);
        writeContents(path, contents);
    }

    String readRelativeFile(Path base, Path relative) {
        Path path = base.resolve(relative);
        return readContents(path);
    }

    void checkPath(Path base, Path relative, boolean exists) {
        Path path = base.resolve(relative);
        assertThat(Files.exists(path)).isEqualTo(exists);
    }

    private void checkContents(Path base, Path relative, String contents) {
        assertThat(readRelativeFile(base, relative)).isEqualTo(contents);
    }

    @ParameterizedTest
    @MethodSource("testArgsSource")
    void testArgs(String userId, String jobId, String depositId, UserEventSender userEventSender, String bagId,
                  Context context, Event lastEvent, Map<String, String> properties, List<String> fileStorePaths, Map<String, UserStore> userStores, List<String> fileUploadPaths,
                  String expectedMessage, boolean messageSent) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new DepositUserStoreDownloader(userId, jobId, depositId, userEventSender, bagId, context, lastEvent, properties, fileStorePaths, userStores, fileUploadPaths));
        assertThat(ex.getMessage()).isEqualTo(expectedMessage);
        if (messageSent) {
            String actualMessage = argEvent.getValue().getMessage();
            assertThat(actualMessage).isEqualTo(expectedMessage);
        }
        verify(SENDER, atLeastOnce()).send(any(Event.class));
        verifyNoMoreInteractions(SENDER);
    }

    static Stream<Arguments> testArgsSource() {
        return Stream.of(
                Arguments.of(null, null, null, null, null, null, null, null, null, null, null, "The userID cannot be blank", false),
                Arguments.of(USER_ID, null, null, null, null, null, null, null, null, null, null, "The jobID cannot be blank", false),
                Arguments.of(USER_ID, JOB_ID, null, null, null, null, null, null, null, null, null, "The depositId cannot be blank", false),

                Arguments.of(USER_ID, JOB_ID, DEPOSIT_ID, null, null, null, null, null, null, null, null, "The userEventSender cannot be null", false),
                Arguments.of(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, null, null, null, null, null, null, null, "The bagID cannot be blank", false),
                Arguments.of(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, null, null, null, null, null, null, "The context cannot be null", false),

                Arguments.of(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, CONTEXT, null, null, null, null, null, "The properties cannot be null", false),
                Arguments.of(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, CONTEXT, null, PROPS, null, null, null, "Deposit failed: null list of fileStorePaths", true),
                Arguments.of(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, CONTEXT, null, PROPS, new ArrayList<>(), null, null, "Deposit failed: null list of userStores", true)
        );
    }
    
    @Test
    @SneakyThrows
    void testAlreadyCopiedAndBagIdDoesExist() {

        ArgumentCaptor<Path> argBagIdDataPath = ArgumentCaptor.forClass(Path.class);
        
        Path bagIdPath = tempDir.resolve(BAG_ID);
        assertThat(Files.exists(bagIdPath)).isFalse();
        Path bagIdDataPath = bagIdPath.resolve(DATA);
        assertThat(Files.exists(bagIdDataPath)).isFalse();

        //we create this directory to force the skip
        Files.createDirectories(bagIdDataPath);

        Event lastEvent = new TransferComplete();
        lastEvent.eventClass = TransferComplete.class.getName();
        
        var downloader = Mockito.spy(new DepositUserStoreDownloader(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, CONTEXT, lastEvent, PROPS,
                fileStorePaths, userStores, null));

        File downloadResult = downloader.transferFromUserStoreToWorker();
        assertThat(downloadResult).isEqualTo(bagIdPath.toFile());
        
        verify(downloader, never()).copySelectedUserDataToBagDataDir(any(Path.class));
    }
    
    @Test
    @SneakyThrows
    void testAlreadyCopiedAndBagIdDoesNotExist() {

        Path bagIdPath = tempDir.resolve(BAG_ID);
        assertThat(Files.exists(bagIdPath)).isFalse();
        Path bagIdDataPath = bagIdPath.resolve(DATA);
        assertThat(Files.exists(bagIdDataPath)).isFalse();

        Event lastEvent = new TransferComplete();
        lastEvent.eventClass = TransferComplete.class.getName();
        
        var downloader = Mockito.spy(new DepositUserStoreDownloader(USER_ID, JOB_ID, DEPOSIT_ID, SENDER, BAG_ID, CONTEXT, lastEvent, PROPS,
                fileStorePaths, userStores, null));
        
        doNothing().when(downloader).copySelectedUserDataToBagDataDir(bagIdDataPath);

        File downloadResult = downloader.transferFromUserStoreToWorker();
        assertThat(downloadResult).isEqualTo(bagIdPath.toFile());
        
        verify(downloader).copySelectedUserDataToBagDataDir(any(Path.class));
    }
}