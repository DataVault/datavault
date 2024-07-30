package org.datavaultplatform.worker.tasks.deposit;

import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.event.deposit.ComputedChunks;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.event.deposit.PackageComplete;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.worker.operations.FileSplitter;
import org.datavaultplatform.worker.operations.Tar;
import org.datavaultplatform.worker.tasks.ChecksumHelper;
import org.datavaultplatform.worker.tasks.EncryptionChunkHelper;
import org.datavaultplatform.worker.tasks.PackageHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositPackagerTest {

    public static final String TEST_USER_ID = "test-user-id";
    public static final String TEST_JOB_ID = "test-job-id";
    public static final String TEST_DEPOSIT_ID = "test-deposit-id";
    public static final String TEST_BAG_ID = "test-bag-id";
    public static final String TEST_DEPOSIT_METADATA = "test-deposit-metadata";
    public static final String TEST_VAULT_METADATA = "test-vault-metadata";
    public static final String TEST_EXTERNAL_METADATA = "test-external-metadata";

    DepositPackager depositPackager;
    
    @Mock
    UserEventSender mUserEventSender;

    @Mock
    Context mContext;

    @Mock
    Event mLastEvent;

    @Captor
    ArgumentCaptor<File> argBagDir1;

    @Captor
    ArgumentCaptor<File> argBagDir2;

    @Captor
    ArgumentCaptor<String> argDepositMetadata;

    @Captor
    ArgumentCaptor<String> argVaultMetadata;

    @Captor
    ArgumentCaptor<String> argFileTypeMetadata;

    @Captor
    ArgumentCaptor<String> argExternalMetadata;

    @Captor
    ArgumentCaptor<File> argTarFile1;
    @Captor
    ArgumentCaptor<File> argTarFile2;
    @Captor
    ArgumentCaptor<File> argTarFile3;

    @Captor
    ArgumentCaptor<Integer> argNumThreads;

    @Captor
    ArgumentCaptor<Long> argBytesPerChunk;

    @Captor
    ArgumentCaptor<String> argLabel;

    @Mock
    File mChunkFile1;
    
    @Mock
    File mChunkFile2;
    
    @Mock
    TaskExecutor<ChecksumHelper> mTaskExecutor1;
    @Mock
    TaskExecutor<EncryptionChunkHelper> mTaskExecutor2;

    List<Event> eventsSent;
    
    Map<String, String> properties;
    
    Path contextTempDir;
    
    File expectedTarFile;
    
    long expectedTarFileSize;

    File testBagDir;
    
    @BeforeEach
    @SneakyThrows
    void setup() {
        contextTempDir = Files.createTempDirectory("test");
        Files.createDirectories(contextTempDir);
        
        lenient().doAnswer(invocation -> {
            Event event = invocation.getArgument(0, Event.class);
            eventsSent.add(event);
            return null;
        }).when(mUserEventSender).send(any(Event.class));
        eventsSent = new ArrayList<>();
        properties = new HashMap<>();

        properties.put(PropNames.DEPOSIT_METADATA, TEST_DEPOSIT_METADATA);
        properties.put(PropNames.VAULT_METADATA, TEST_VAULT_METADATA);
        properties.put(PropNames.EXTERNAL_METADATA, TEST_EXTERNAL_METADATA);

        depositPackager = spy(new DepositPackager(TEST_USER_ID, TEST_JOB_ID, TEST_DEPOSIT_ID,
                mUserEventSender, TEST_BAG_ID, mContext, mLastEvent, properties));
        lenient().doNothing().when(depositPackager).createBag(argBagDir1.capture());
        lenient().doNothing().when(depositPackager).addMetadata(
                argBagDir2.capture(),
                argDepositMetadata.capture(),
                argVaultMetadata.capture(),
                argFileTypeMetadata.capture(),
                argExternalMetadata.capture());

        lenient().when(depositPackager.getTaskExecutor1(argNumThreads.capture(), argLabel.capture())).thenReturn(mTaskExecutor1);
        lenient().when(depositPackager.getTaskExecutor2(argNumThreads.capture(), argLabel.capture())).thenReturn(mTaskExecutor2);
        when(mContext.getTempDir()).thenReturn(contextTempDir);

        expectedTarFile = contextTempDir.resolve("test-bag-id.tar").toFile();
        
        try(FileWriter fw = new FileWriter(expectedTarFile)) {
            fw.write("testing-testing-123");
        }
        expectedTarFileSize = expectedTarFile.length();
        
        lenient().when(mContext.getChunkingByteSize()).thenReturn(2112L);
        lenient().when(mContext.getNoChunkThreads()).thenReturn(12);

        testBagDir = Files.createTempDirectory("test").toFile();
        
        lenient().when(mChunkFile1.length()).thenReturn(100_000L);
        lenient().when(mChunkFile2.length()).thenReturn(200_000L);
        lenient().when(mChunkFile1.getAbsolutePath()).thenReturn("/tmp/chunkFile1");
        lenient().when(mChunkFile2.getAbsolutePath()).thenReturn("/tmp/chunkFile2");
    }


    @Test
    @SneakyThrows
    void testEncryptedAndChunks() {
        ChecksumHelper checksumHelper1 = new ChecksumHelper(1, "CHUNK-HASH-1", mChunkFile1);
        ChecksumHelper checksumHelper2 = new ChecksumHelper(2, "CHUNK-HASH-2", mChunkFile2);
        
        EncryptionChunkHelper encChecksumHelper1 = new EncryptionChunkHelper("one".getBytes(StandardCharsets.UTF_8), "ENC-CHUNK-HASH-1", 1);
        EncryptionChunkHelper encChecksumHelper2 = new EncryptionChunkHelper("two".getBytes(StandardCharsets.UTF_8), "ENC-CHUNK-HASH-2", 2);

        try (MockedStatic<Tar> mockedTar = mockStatic(Tar.class);
             MockedStatic<Verify> mockedVerify = mockStatic(Verify.class);
             MockedStatic<FileSplitter> mockedFileSplitter = mockStatic(FileSplitter.class)) {

            mockedTar.when(() -> {
                Tar.createTar(eq(testBagDir), argTarFile1.capture());
            }).thenAnswer(invocation -> null);

            mockedVerify.when(() -> {
                Verify.getDigest(argTarFile2.capture());
            }).thenReturn("TEST-DIGEST");

            mockedFileSplitter.when(() -> {
                FileSplitter.splitFile(argTarFile3.capture(), argBytesPerChunk.capture());
            }).thenReturn(new File[]{mChunkFile1, mChunkFile2});

            when(mContext.getEncryptionMode()).thenReturn(Context.AESMode.GCM);
            when(mContext.isChunkingEnabled()).thenReturn(true);
            when(mContext.isEncryptionEnabled()).thenReturn(true);


            // simulate what happens in the 1st executor
            doAnswer(invocation -> {
                Consumer<ChecksumHelper> checksumHelperConsumer = invocation.getArgument(0, Consumer.class);
                checksumHelperConsumer.accept(checksumHelper1);
                checksumHelperConsumer.accept(checksumHelper2);
                return null;
            }).when(mTaskExecutor1).execute(any(Consumer.class));

            // simulate what happens in the 2nd executor
            doAnswer(invocation -> {
                Consumer<EncryptionChunkHelper> encChecksumHelperConsumer = invocation.getArgument(0, Consumer.class);
                encChecksumHelperConsumer.accept(encChecksumHelper1);
                encChecksumHelperConsumer.accept(encChecksumHelper2);
                return null;
            }).when(mTaskExecutor2).execute(any(Consumer.class));

            PackageHelper result = this.depositPackager.packageStep(testBagDir);
            var chunkHelpersMap = result.getChunkHelpers();
            assertThat(chunkHelpersMap.size()).isEqualTo(2);
            var helper1 = chunkHelpersMap.get(1);
            assertThat(helper1.isEncrypted()).isTrue();
            assertThat(helper1.getChunkNumber()).isEqualTo(1);
            assertThat(helper1.getChunkFile()).isEqualTo(mChunkFile1);
            assertThat(helper1.getChunkHash()).isEqualTo("CHUNK-HASH-1");
            assertThat(helper1.getChunkEncHash()).isEqualTo("ENC-CHUNK-HASH-1");
            assertThat(helper1.getChunkIV()).isEqualTo("one".getBytes(StandardCharsets.UTF_8));


            var helper2 = chunkHelpersMap.get(2);
            assertThat(helper2.isEncrypted()).isTrue();
            assertThat(helper2.getChunkNumber()).isEqualTo(2);
            assertThat(helper2.getChunkFile()).isEqualTo(mChunkFile2);
            assertThat(helper2.getChunkHash()).isEqualTo("CHUNK-HASH-2");
            assertThat(helper2.getChunkEncHash()).isEqualTo("ENC-CHUNK-HASH-2");
            assertThat(helper2.getChunkIV()).isEqualTo("two".getBytes(StandardCharsets.UTF_8));

            assertThat(result.isEncrypted()).isTrue();
            assertThat(result.isChunked()).isTrue();
            assertThat(result.getTarFile()).isEqualTo(expectedTarFile);
            assertThat(result.getArchiveSize()).isEqualTo(expectedTarFileSize);
            assertThat(result.getTarHash()).isEqualTo("TEST-DIGEST");

            // we did not encrypt the whole tar file - we encrypted the chunks
            assertThat(result.getIv()).isNull();
            assertThat(result.getEncTarHash()).isNull();

            assertThat(eventsSent.size()).isEqualTo(4);
            PackageComplete event1 = (PackageComplete) eventsSent.get(0);
            assertThat(event1.getDepositId()).isEqualTo(TEST_DEPOSIT_ID);
            assertThat(event1.getJobId()).isEqualTo(TEST_JOB_ID);

            ComputedDigest event2 = (ComputedDigest) eventsSent.get(1);
            assertThat(event2.getDepositId()).isEqualTo(TEST_DEPOSIT_ID);
            assertThat(event2.getJobId()).isEqualTo(TEST_JOB_ID);
            assertThat(event2.getDigest()).isEqualTo("TEST-DIGEST");
            assertThat(event2.getDigestAlgorithm()).isEqualTo("SHA-1");

            ComputedEncryption event3 = (ComputedEncryption) eventsSent.get(2);
            assertThat(event3.getDepositId()).isEqualTo(TEST_DEPOSIT_ID);
            assertThat(event3.getJobId()).isEqualTo(TEST_JOB_ID);

            assertThat(event3.getChunkIVs()).isEqualTo(result.getChunksIVs());
            assertThat(event3.getEncChunkDigests()).isEqualTo(result.getEncChunkHashes());
            assertThat(event3.getChunksDigest()).isEqualTo(result.getChunkHashes());
            
            Mockito.verify(mUserEventSender, times(4)).send(any(Event.class));
            assertThat(argLabel.getAllValues()).isEqualTo(List.of("Chunking failed.", "Chunk encryption failed."));

            verify(mTaskExecutor1, times(2)).add(any(Callable.class));
            verify(mTaskExecutor2, times(2)).add(any(Callable.class));
            verify(mTaskExecutor1, times(1)).execute(any(Consumer.class));
            verify(mTaskExecutor2, times(1)).execute(any(Consumer.class));
            assertThat(argBytesPerChunk.getAllValues()).isEqualTo(List.of(2112L));

            assertThat(argNumThreads.getAllValues().size()).isEqualTo(2);
            int actualThreads1 = argNumThreads.getAllValues().get(0);
            int actualThreads2 = argNumThreads.getAllValues().get(1);
            assertThat(actualThreads1).isEqualTo(12);
            assertThat(actualThreads2).isEqualTo(12);

            assertThat(argLabel.getAllValues().size()).isEqualTo(2);
            String actualLabel1 = argLabel.getAllValues().get(0);
            assertThat(actualLabel1).isEqualTo("Chunking failed.");

            String actualLabel2 = argLabel.getAllValues().get(1);
            assertThat(actualLabel2).isEqualTo("Chunk encryption failed.");

            verify(depositPackager).getTaskExecutor1(actualThreads1, actualLabel1);
            verify(depositPackager).getTaskExecutor2(actualThreads2, actualLabel2);
            
            File actualTarFile1 = argTarFile1.getValue();
            assertThat(actualTarFile1).isEqualTo(expectedTarFile);

            File actualTarFile2 = argTarFile2.getValue();
            assertThat(actualTarFile2).isEqualTo(expectedTarFile);

            File actualTarFile3 = argTarFile2.getValue();
            assertThat(actualTarFile3).isEqualTo(expectedTarFile);

            System.out.println("FIN.");
        }
    }

    @Test
    @SneakyThrows
    void testEncryptedAndNoChunks() {
        try (MockedStatic<Tar> mockedTar = mockStatic(Tar.class);
             MockedStatic<Verify> mockedVerify = mockStatic(Verify.class);
             MockedStatic<Encryption> mockedEncryption = mockStatic(Encryption.class)) {

            mockedTar.when(() -> {
                Tar.createTar(eq(testBagDir), argTarFile1.capture());
            }).thenAnswer(invocation -> null);

            mockedVerify.when(() -> {
                Verify.getDigest(argTarFile2.capture());
            }).thenReturn("TEST-DIGEST", "ENC-TEST-DIGEST");

            mockedEncryption.when(() -> {
                Encryption.encryptFile(mContext, expectedTarFile);
            }).thenReturn("WHOLE-TAR-IV".getBytes(StandardCharsets.UTF_8));

            when(mContext.getEncryptionMode()).thenReturn(Context.AESMode.GCM);
            when(mContext.isChunkingEnabled()).thenReturn(false);
            when(mContext.isEncryptionEnabled()).thenReturn(true);

            PackageHelper result = this.depositPackager.packageStep(testBagDir);

            mockedEncryption.verify(() -> {
                Encryption.encryptFile(mContext, expectedTarFile);
            }, times(1));

            var chunkHelpersMap = result.getChunkHelpers();
            assertThat(chunkHelpersMap.size()).isEqualTo(0);

            assertThat(result.isEncrypted()).isTrue();
            assertThat(result.isChunked()).isFalse();
            assertThat(result.getTarFile()).isEqualTo(expectedTarFile);
            assertThat(result.getArchiveSize()).isEqualTo(expectedTarFileSize);
            assertThat(result.getTarHash()).isEqualTo("TEST-DIGEST");

            // we did not encrypt the whole tar file - we encrypted the chunks
            assertThat(result.getIv()).isEqualTo("WHOLE-TAR-IV".getBytes(StandardCharsets.UTF_8));
            assertThat(result.getEncTarHash()).isEqualTo("ENC-TEST-DIGEST");

            assertThat(eventsSent.size()).isEqualTo(4);
            PackageComplete event1 = (PackageComplete) eventsSent.get(0);
            assertThat(event1.getDepositId()).isEqualTo(TEST_DEPOSIT_ID);
            assertThat(event1.getJobId()).isEqualTo(TEST_JOB_ID);

            ComputedDigest event2 = (ComputedDigest) eventsSent.get(1);
            assertThat(event2.getDepositId()).isEqualTo(TEST_DEPOSIT_ID);
            assertThat(event2.getJobId()).isEqualTo(TEST_JOB_ID);
            assertThat(event2.getDigest()).isEqualTo("TEST-DIGEST");
            assertThat(event2.getDigestAlgorithm()).isEqualTo("SHA-1");

            ComputedEncryption event3 = (ComputedEncryption) eventsSent.get(2);
            assertThat(event3.getDepositId()).isEqualTo(TEST_DEPOSIT_ID);
            assertThat(event3.getJobId()).isEqualTo(TEST_JOB_ID);

            assertThat(event3.getChunkIVs()).isNull();
            assertThat(event3.getEncChunkDigests()).isNull();
            assertThat(event3.getChunksDigest()).isNull();

            assertThat(result.getChunksIVs().isEmpty()).isTrue();
            assertThat(result.getChunkHashes().isEmpty()).isTrue();
            assertThat(result.getEncChunkHashes().isEmpty()).isTrue();

            Mockito.verify(mUserEventSender, times(4)).send(any(Event.class));
            assertThat(argLabel.getAllValues()).isEqualTo(Collections.emptyList());
            verify(mTaskExecutor1, times(0)).add(any(Callable.class));
            verify(mTaskExecutor2, times(0)).add(any(Callable.class));
            verify(mTaskExecutor1, times(0)).execute(any(Consumer.class));
            verify(mTaskExecutor2, times(0)).execute(any(Consumer.class));
            assertThat(argBytesPerChunk.getAllValues()).isEqualTo(Collections.emptyList());

            verify(depositPackager, never()).getTaskExecutor1(any(Integer.class), any(String.class));
            verify(depositPackager, never()).getTaskExecutor2(any(Integer.class), any(String.class));

            File actualTarFile1 = argTarFile1.getValue();
            assertThat(actualTarFile1).isEqualTo(expectedTarFile);

            File actualTarFile2 = argTarFile2.getValue();
            assertThat(actualTarFile2).isEqualTo(expectedTarFile);

            System.out.println("FIN.");
        }
    }

    @Test
    @SneakyThrows
    void testNotEncryptedAndChunks() {
        ChecksumHelper checksumHelper1 = new ChecksumHelper(1, "CHUNK-HASH-1", mChunkFile1);
        ChecksumHelper checksumHelper2 = new ChecksumHelper(2, "CHUNK-HASH-2", mChunkFile2);

        try (MockedStatic<Tar> mockedTar = mockStatic(Tar.class);
             MockedStatic<Verify> mockedVerify = mockStatic(Verify.class);
             MockedStatic<FileSplitter> mockedFileSplitter = mockStatic(FileSplitter.class)) {

            mockedTar.when(() -> {
                Tar.createTar(eq(testBagDir), argTarFile1.capture());
            }).thenAnswer(invocation -> null);

            mockedVerify.when(() -> {
                Verify.getDigest(argTarFile2.capture());
            }).thenReturn("TEST-DIGEST");

            mockedFileSplitter.when(() -> {
                FileSplitter.splitFile(argTarFile3.capture(), argBytesPerChunk.capture());
            }).thenReturn(new File[]{mChunkFile1, mChunkFile2});

            when(mContext.isChunkingEnabled()).thenReturn(true);
            when(mContext.isEncryptionEnabled()).thenReturn(false);

            // simulate what happens in the 1st executor
            doAnswer(invocation -> {
                Consumer<ChecksumHelper> checksumHelperConsumer = invocation.getArgument(0, Consumer.class);
                checksumHelperConsumer.accept(checksumHelper1);
                checksumHelperConsumer.accept(checksumHelper2);
                return null;
            }).when(mTaskExecutor1).execute(any(Consumer.class));

            PackageHelper result = this.depositPackager.packageStep(testBagDir);
            var chunkHelpersMap = result.getChunkHelpers();
            assertThat(chunkHelpersMap.size()).isEqualTo(2);
            var helper1 = chunkHelpersMap.get(1);
            assertThat(helper1.isEncrypted()).isFalse();
            assertThat(helper1.getChunkNumber()).isEqualTo(1);
            assertThat(helper1.getChunkFile()).isEqualTo(mChunkFile1);
            assertThat(helper1.getChunkHash()).isEqualTo("CHUNK-HASH-1");
            assertThat(helper1.getChunkEncHash()).isNull();
            assertThat(helper1.getChunkIV()).isNull();

            var helper2 = chunkHelpersMap.get(2);
            assertThat(helper2.isEncrypted()).isFalse();
            assertThat(helper2.getChunkNumber()).isEqualTo(2);
            assertThat(helper2.getChunkFile()).isEqualTo(mChunkFile2);
            assertThat(helper2.getChunkHash()).isEqualTo("CHUNK-HASH-2");
            assertThat(helper2.getChunkEncHash()).isNull();
            assertThat(helper2.getChunkIV()).isNull();

            assertThat(result.isEncrypted()).isFalse();
            assertThat(result.isChunked()).isTrue();
            assertThat(result.getTarFile()).isEqualTo(expectedTarFile);
            assertThat(result.getArchiveSize()).isEqualTo(expectedTarFileSize);
            assertThat(result.getTarHash()).isEqualTo("TEST-DIGEST");

            // we did not encrypt the whole tar file - we encrypted the chunks
            assertThat(result.getIv()).isNull();
            assertThat(result.getEncTarHash()).isNull();

            assertThat(eventsSent.size()).isEqualTo(4);
            PackageComplete event1 = (PackageComplete) eventsSent.get(0);
            assertThat(event1.getDepositId()).isEqualTo(TEST_DEPOSIT_ID);
            assertThat(event1.getJobId()).isEqualTo(TEST_JOB_ID);

            ComputedDigest event2 = (ComputedDigest) eventsSent.get(1);
            assertThat(event2.getDepositId()).isEqualTo(TEST_DEPOSIT_ID);
            assertThat(event2.getJobId()).isEqualTo(TEST_JOB_ID);
            assertThat(event2.getDigest()).isEqualTo("TEST-DIGEST");
            assertThat(event2.getDigestAlgorithm()).isEqualTo("SHA-1");

            ComputedChunks event3 = (ComputedChunks) eventsSent.get(2);
            assertThat(event3.getDepositId()).isEqualTo(TEST_DEPOSIT_ID);
            assertThat(event3.getJobId()).isEqualTo(TEST_JOB_ID);

            assertThat(event3.getChunksDigest()).isEqualTo(result.getChunkHashes());

            Mockito.verify(mUserEventSender, times(4)).send(any(Event.class));
            assertThat(argLabel.getAllValues()).isEqualTo(List.of("Chunking failed."));
            verify(mTaskExecutor1, times(2)).add(any(Callable.class));
            verify(mTaskExecutor2, times(0)).add(any(Callable.class));
            verify(mTaskExecutor1, times(1)).execute(any(Consumer.class));
            verify(mTaskExecutor2, times(0)).execute(any(Consumer.class));
            assertThat(argBytesPerChunk.getAllValues()).isEqualTo(List.of(2112L));
            
            assertThat(argNumThreads.getAllValues().size()).isEqualTo(1);
            int actualThreads = argNumThreads.getValue();
            assertThat(actualThreads).isEqualTo(12);

            assertThat(argLabel.getAllValues().size()).isEqualTo(1);
            String actualLabel = argLabel.getValue();
            assertThat(actualLabel).isEqualTo("Chunking failed.");

            verify(depositPackager).getTaskExecutor1(actualThreads, actualLabel);
            verify(depositPackager, never()).getTaskExecutor2(any(Integer.class), any(String.class));

            File actualTarFile1 = argTarFile1.getValue();
            assertThat(actualTarFile1).isEqualTo(expectedTarFile);

            File actualTarFile2 = argTarFile2.getValue();
            assertThat(actualTarFile2).isEqualTo(expectedTarFile);

            File actualTarFile3 = argTarFile3.getValue();
            assertThat(actualTarFile3).isEqualTo(expectedTarFile);

            System.out.println("FIN.");
        }
    }

    @Test
    @SneakyThrows
    void testNotEncryptedAndNoChunks() {
        try (MockedStatic<Tar> mockedTar = mockStatic(Tar.class);
             MockedStatic<Verify> mockedVerify = mockStatic(Verify.class);
             ) {

            mockedTar.when(() -> {
                Tar.createTar(eq(testBagDir), argTarFile1.capture());
            }).thenAnswer(invocation -> null);

            mockedVerify.when(() -> {
                Verify.getDigest(argTarFile2.capture());
            }).thenReturn("TEST-DIGEST");

            when(mContext.isChunkingEnabled()).thenReturn(false);
            when(mContext.isEncryptionEnabled()).thenReturn(false);

            PackageHelper result = this.depositPackager.packageStep(testBagDir);
            var chunkHelpersMap = result.getChunkHelpers();
            assertThat(chunkHelpersMap.size()).isEqualTo(0);

            assertThat(result.isEncrypted()).isFalse();
            assertThat(result.isChunked()).isFalse();
            assertThat(result.getTarFile()).isEqualTo(expectedTarFile);
            assertThat(result.getArchiveSize()).isEqualTo(expectedTarFileSize);
            assertThat(result.getTarHash()).isEqualTo("TEST-DIGEST");

            // we did not encrypt the whole tar file - we encrypted the chunks
            assertThat(result.getIv()).isNull();
            assertThat(result.getEncTarHash()).isNull();

            assertThat(eventsSent.size()).isEqualTo(3);
            PackageComplete event1 = (PackageComplete) eventsSent.get(0);
            assertThat(event1.getDepositId()).isEqualTo(TEST_DEPOSIT_ID);
            assertThat(event1.getJobId()).isEqualTo(TEST_JOB_ID);

            ComputedDigest event2 = (ComputedDigest) eventsSent.get(1);
            assertThat(event2.getDepositId()).isEqualTo(TEST_DEPOSIT_ID);
            assertThat(event2.getJobId()).isEqualTo(TEST_JOB_ID);
            assertThat(event2.getDigest()).isEqualTo("TEST-DIGEST");
            assertThat(event2.getDigestAlgorithm()).isEqualTo("SHA-1");

            Mockito.verify(mUserEventSender, times(3)).send(any(Event.class));
            assertThat(argLabel.getAllValues()).isEqualTo(Collections.emptyList());
            verify(mTaskExecutor1, never()).add(any(Callable.class));
            verify(mTaskExecutor2, never()).add(any(Callable.class));
            verify(mTaskExecutor1, never()).execute(any(Consumer.class));
            verify(mTaskExecutor2, never()).execute(any(Consumer.class));
            assertThat(argBytesPerChunk.getAllValues()).isEqualTo(Collections.emptyList());

            verify(depositPackager, never()).getTaskExecutor1(any(Integer.class), any(String.class));
            verify(depositPackager, never()).getTaskExecutor2(any(Integer.class), any(String.class));

            File actualTarFile1 = argTarFile1.getValue();
            assertThat(actualTarFile1).isEqualTo(expectedTarFile);

            File actualTarFile2 = argTarFile2.getValue();
            assertThat(actualTarFile2).isEqualTo(expectedTarFile);

            System.out.println("FIN.");
        }
    }
    
    @AfterEach
    @SneakyThrows
    void tearDown(){
        
        String actualDepositMetaData = argDepositMetadata.getValue();
        String actualExternalMetaData = argExternalMetadata.getValue();
        String actualVaultMetadata = argVaultMetadata.getValue();
        String actualFileMetadata = argFileTypeMetadata.getValue();

        assertThat(actualDepositMetaData).isEqualTo(TEST_DEPOSIT_METADATA);
        assertThat(actualExternalMetaData).isEqualTo(TEST_EXTERNAL_METADATA);
        assertThat(actualVaultMetadata).isEqualTo(TEST_VAULT_METADATA);
        assertThat(actualFileMetadata).isNull();
        
        File actualBagDir = argBagDir1.getValue();
        assertThat(actualBagDir).isEqualTo(testBagDir);
        Mockito.verify(depositPackager).createBag(actualBagDir);
        
        actualBagDir = argBagDir2.getValue();
        assertThat(actualBagDir).isEqualTo(testBagDir);
        Mockito.verify(depositPackager).addMetadata(
                actualBagDir, 
                actualDepositMetaData, actualVaultMetadata, 
                actualFileMetadata, actualExternalMetaData);
        
        Mockito.verify(mContext,atLeast(1)).isEncryptionEnabled();
        Mockito.verify(mContext, atLeast(1)).isChunkingEnabled();
        Mockito.verify(mContext, atLeast(1)).getTempDir();
        Mockito.verify(mContext, atMost(1)).getChunkingByteSize();
        Mockito.verify(mContext, atMost(2)).getNoChunkThreads();
        
        Mockito.verify(mChunkFile1, atMost(1)).length();
        Mockito.verify(mChunkFile2, atMost(1)).length();
        Mockito.verify(mChunkFile1, atMost(1)).getAbsolutePath();
        Mockito.verify(mChunkFile2, atMost(1)).getAbsolutePath();
        
        Mockito.verifyNoMoreInteractions(
                mLastEvent, mContext, 
                mUserEventSender, mChunkFile1, 
                mChunkFile2, mTaskExecutor1, 
                mTaskExecutor2);
    }
}