package org.datavaultplatform.common.storage.impl;

import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.util.ProcessHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SuppressWarnings("CodeBlock2Expr")
@ExtendWith(MockitoExtension.class)
class TivoliStorageManagerTest {

    public static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2022-11-08T14:13:06.618Z"),
            ZoneId.of("Europe/London"));


    @TempDir
    Path tempPath;
    
    @Test
    void testConstructorFromEmptyProps() {
        Map<String, String> props = new HashMap<>();
        TivoliStorageManager tsm = new TivoliStorageManager("test123", props);
        assertThat(tsm.name).isEqualTo("test123");
        assertThat(tsm.isReverse()).isEqualTo(TivoliStorageManager.DEFAULT_REVERSE);
        assertThat(tsm.getRetryTimeMinutes()).isEqualTo(TivoliStorageManager.DEFAULT_RETRY_TIME);
        assertThat(tsm.getMaxRetries()).isEqualTo(TivoliStorageManager.DEFAULT_MAX_RETRIES);
        assertThat(tsm.getTsmServerNodeOpt1()).isEqualTo(TivoliStorageManager.DEFAULT_TSM_SERVER_NODE1_OPT);
        assertThat(tsm.getTsmServerNodeOpt2()).isEqualTo(TivoliStorageManager.DEFAULT_TSM_SERVER_NODE2_OPT);
        assertThat(tsm.getTempPathPrefix()).isEqualTo(TivoliStorageManager.DEFAULT_TEMP_PATH_PREFIX);
    }

    @Test
    void testConstructorFromFullPropsReversed() {
        Map<String, String> props = new HashMap<>();
        props.put(PropNames.TSM_REVERSE, "true");
        props.put(PropNames.TSM_MAX_RETRIES, "123");
        props.put(PropNames.TSM_RETRY_TIME, "321");
        props.put(PropNames.OPTIONS_DIR, "/testOptionsDir");
        props.put(PropNames.TEMP_DIR, "/tmp");
        TivoliStorageManager tsm = new TivoliStorageManager("test123", props);
        assertThat(tsm.name).isEqualTo("test123");
        assertThat(tsm.isReverse()).isEqualTo(true);
        assertThat(tsm.getRetryTimeMinutes()).isEqualTo(321);
        assertThat(tsm.getMaxRetries()).isEqualTo(123);
        assertThat(tsm.getTsmServerNodeOpt1()).isEqualTo("/testOptionsDir/" + TivoliStorageManager.DSM_OPT_1);
        assertThat(tsm.getTsmServerNodeOpt2()).isEqualTo("/testOptionsDir/" + TivoliStorageManager.DSM_OPT_2);
        assertThat(tsm.getTempPathPrefix()).isEqualTo("/tmp");
        assertThat(tsm.getLocations()).containsExactly(tsm.getTsmServerNodeOpt2(), tsm.getTsmServerNodeOpt1());
    }

    @Test
    void testConstructorFromFullPropsNonReversed() {
        Map<String, String> props = new HashMap<>();
        props.put(PropNames.TSM_REVERSE, "false");
        props.put(PropNames.TSM_MAX_RETRIES, "123");
        props.put(PropNames.TSM_RETRY_TIME, "321");
        props.put(PropNames.OPTIONS_DIR, "/testOptionsDir");
        props.put(PropNames.TEMP_DIR, "/tmp");
        TivoliStorageManager tsm = new TivoliStorageManager("test123", props);
        assertThat(tsm.name).isEqualTo("test123");
        assertThat(tsm.isReverse()).isEqualTo(false);
        assertThat(tsm.getRetryTimeMinutes()).isEqualTo(321);
        assertThat(tsm.getMaxRetries()).isEqualTo(123);
        assertThat(tsm.getTsmServerNodeOpt1()).isEqualTo("/testOptionsDir/" + TivoliStorageManager.DSM_OPT_1);
        assertThat(tsm.getTsmServerNodeOpt2()).isEqualTo("/testOptionsDir/" + TivoliStorageManager.DSM_OPT_2);
        assertThat(tsm.getLocations()).containsExactly(tsm.getTsmServerNodeOpt1(), tsm.getTsmServerNodeOpt2());
        assertThat(tsm.getTempPathPrefix()).isEqualTo("/tmp");
    }

    @Test
    void testMaxRetriesCannotBeLessThanOne() {
        Map<String, String> props = new HashMap<>();
        props.put(PropNames.TSM_MAX_RETRIES, "0");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TivoliStorageManager("test123", props);
        });
        assertThat(ex).hasMessage("The config property of tsmMaxRetries[0] cannot be less than 1");
    }

    @Test
    void testRetryTimeMinutesCannotBeLessThanZero() {
        Map<String, String> props = new HashMap<>();
        props.put(PropNames.TSM_RETRY_TIME, "-1");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TivoliStorageManager("test123", props);
        });
        assertThat(ex).hasMessage("The config property of tsmRetryTime[-1] cannot be less than 0");
    }

    @Nested
    class LookupValueTests {
        final Map<String, String> config = new HashMap<>();
        final Function<String, Integer> parser = Integer::parseInt;

        @Test
        void lookupPropertyNotFound() {
            assertThat(TivoliStorageManager.lookup(config, "name", parser, 123)).isEqualTo(123);
        }

        @Test
        void lookupPropertyFoundAndParsed() {
            config.put("name", "1234");
            assertThat(TivoliStorageManager.lookup(config, "name", parser, 123)).isEqualTo(1234);
        }

        @Test
        void lookupPropertyFoundAndNotParsed() {
            config.put("name", "BLAH");
            assertThat(TivoliStorageManager.lookup(config, "name", parser, 123)).isEqualTo(123);
        }
    }

    @Nested
    class TSMTapeDriverTests {

        @Test
        void testNotFound() {
            assertThat(TivoliStorageManager.checkTSMTapeDriver()).isFalse();
        }
    }
    
    @Nested
    class StoreTests {

        Path tsmTemp;
        File depositSourceFile;
        TivoliStorageManager tsm;
        
        @Captor
        ArgumentCaptor<String> argLocation;
        @Captor
        ArgumentCaptor<Progress> argProgress;
        @Captor
        ArgumentCaptor<File> argTsmFile;
        @Captor
        ArgumentCaptor<String> argDepositId;
        @Captor
        ArgumentCaptor<Integer> argMaxRetries;
        @Captor
        ArgumentCaptor<Integer> argRetryTimeMins;

        @BeforeEach
        void setup() throws IOException {
            Path depositSourcePath = tempPath.resolve("source.txt");
            Files.createFile(depositSourcePath);
            
            tsmTemp = tempPath.resolve("tsmTemp");
            Files.createDirectories(tsmTemp);
            
            depositSourceFile = depositSourcePath.toFile();
            Map<String, String> props = new HashMap<>();
            props.put(PropNames.TEMP_DIR, tsmTemp.toString());
            props.put(PropNames.OPTIONS_DIR, "/tmp/opt");
            tsm = Mockito.spy(new TivoliStorageManager("testTSM", props));
        }
        
        @Test
        void testStoreSucceeds() throws Exception {
            assertThat(depositSourceFile.exists()).isTrue();
            TSMTracker mTSMTracker1 = Mockito.mock(TSMTracker.class);
            TSMTracker mTSMTracker2 = Mockito.mock(TSMTracker.class);

            Mockito.doReturn(mTSMTracker1, mTSMTracker2).when(tsm).getTSMTracker(
                    argLocation.capture(),
                    argTsmFile.capture(),
                    argProgress.capture(),
                    argDepositId.capture(),
                    argMaxRetries.capture(),
                    argRetryTimeMins.capture());

            Mockito.when(mTSMTracker1.call()).thenReturn("one");
            Mockito.when(mTSMTracker2.call()).thenReturn("two");

            Progress progress = new Progress();
            String result = tsm.store("testDepositId", depositSourceFile, progress);
            assertThat(result).isEqualTo("testDepositId");

            assertThat(argLocation.getAllValues().get(0)).isEqualTo("/tmp/opt/dsm1.opt");
            assertThat(argTsmFile.getAllValues().get(0)).isEqualTo(tsmTemp.resolve("testDepositId/source.txt").toFile());
            assertThat(argProgress.getAllValues().get(0)).isEqualTo(progress);
            assertThat(argLocation.getAllValues().get(0)).isEqualTo("/tmp/opt/dsm1.opt");
            assertThat(argDepositId.getAllValues().get(0)).isEqualTo("testDepositId");
            assertThat(argMaxRetries.getAllValues().get(0)).isEqualTo( TivoliStorageManager.DEFAULT_MAX_RETRIES);
            assertThat(argRetryTimeMins.getAllValues().get(0)).isEqualTo(TivoliStorageManager.DEFAULT_RETRY_TIME);
            
            // check that the original file we want to deposit has not been deleted!
            assertThat(depositSourceFile).exists();
        }

        @Test
        void testStoreFailsAtLocation1() throws Exception {
            assertThat(depositSourceFile.exists()).isTrue();
            TSMTracker mTSMTracker1 = Mockito.mock(TSMTracker.class);
            TSMTracker mTSMTracker2 = Mockito.mock(TSMTracker.class);

            Mockito.doReturn(mTSMTracker1, mTSMTracker2).when(tsm).getTSMTracker(
                    argLocation.capture(),
                    argTsmFile.capture(),
                    argProgress.capture(),
                    argDepositId.capture(),
                    argMaxRetries.capture(),
                    argRetryTimeMins.capture());

            Mockito.when(mTSMTracker1.call()).thenThrow(new IOException("oops1"));
            Mockito.when(mTSMTracker2.call()).thenReturn("two");

            Progress progress = new Progress();
            
            IOException ex = assertThrows(IOException.class, () -> {
                tsm.store("testDepositId", depositSourceFile, progress);
            });
            assertThat(ex).hasMessage("oops1");

            assertThat(argLocation.getAllValues().get(0)).isEqualTo("/tmp/opt/dsm1.opt");
            assertThat(argTsmFile.getAllValues().get(0)).isEqualTo(tsmTemp.resolve("testDepositId/source.txt").toFile());
            assertThat(argProgress.getAllValues().get(0)).isEqualTo(progress);
            assertThat(argLocation.getAllValues().get(0)).isEqualTo("/tmp/opt/dsm1.opt");
            assertThat(argDepositId.getAllValues().get(0)).isEqualTo("testDepositId");
            assertThat(argMaxRetries.getAllValues().get(0)).isEqualTo( TivoliStorageManager.DEFAULT_MAX_RETRIES);
            assertThat(argRetryTimeMins.getAllValues().get(0)).isEqualTo(TivoliStorageManager.DEFAULT_RETRY_TIME);

            // check that the original file we want to deposit has not been deleted!
            assertThat(depositSourceFile).exists();
        }
        @Test
        void testStoreFailsAtLocation2() throws Exception {
            assertThat(depositSourceFile.exists()).isTrue();
            TSMTracker mTSMTracker1 = Mockito.mock(TSMTracker.class);
            TSMTracker mTSMTracker2 = Mockito.mock(TSMTracker.class);

            Mockito.doReturn(mTSMTracker1, mTSMTracker2).when(tsm).getTSMTracker(
                    argLocation.capture(),
                    argTsmFile.capture(),
                    argProgress.capture(),
                    argDepositId.capture(),
                    argMaxRetries.capture(),
                    argRetryTimeMins.capture());

            Mockito.when(mTSMTracker1.call()).thenReturn("one");
            Mockito.when(mTSMTracker2.call()).thenThrow(new IOException("oops2"));

            Progress progress = new Progress();

            IOException ex = assertThrows(IOException.class, () -> {
                tsm.store("testDepositId", depositSourceFile, progress);
            });
            assertThat(ex).hasMessage("oops2");

            assertThat(argLocation.getAllValues().get(0)).isEqualTo("/tmp/opt/dsm1.opt");
            assertThat(argTsmFile.getAllValues().get(0)).isEqualTo(tsmTemp.resolve("testDepositId/source.txt").toFile());
            assertThat(argProgress.getAllValues().get(0)).isEqualTo(progress);
            assertThat(argLocation.getAllValues().get(0)).isEqualTo("/tmp/opt/dsm1.opt");
            assertThat(argDepositId.getAllValues().get(0)).isEqualTo("testDepositId");
            assertThat(argMaxRetries.getAllValues().get(0)).isEqualTo( TivoliStorageManager.DEFAULT_MAX_RETRIES);
            assertThat(argRetryTimeMins.getAllValues().get(0)).isEqualTo(TivoliStorageManager.DEFAULT_RETRY_TIME);

            // check that the original file we want to deposit has not been deleted!
            assertThat(depositSourceFile).exists();
        }
    }


    @Nested
    class DeleteTests {

        Path tsmTemp;
        TivoliStorageManager tsm;

        @Captor
        ArgumentCaptor<String> argDesc;
        @Captor
        ArgumentCaptor<String> argCommands;

        @BeforeEach
        void setup() throws IOException {
            tsmTemp = tempPath.resolve("tsmTemp");
            Files.createDirectories(tsmTemp);

            Map<String, String> props = new HashMap<>();
            props.put(PropNames.TEMP_DIR, tsmTemp.toString());
            props.put(PropNames.OPTIONS_DIR, "/tmp/opt");
            tsm = Mockito.spy(new TivoliStorageManager("testTSM", props));
        }
        
        @Test
        void testDeleteSucceeds() throws Exception {
            
            File fileToDelete = Files.createTempFile("test",".txt").toFile();
            assertThat(fileToDelete).exists();
            
            ProcessHelper.ProcessInfo mProcessInfo = Mockito.mock(ProcessHelper.ProcessInfo.class);
            lenient().when(mProcessInfo.wasFailure()).thenReturn(false);
            lenient().when(mProcessInfo.wasSuccess()).thenReturn(true);

            Mockito.doReturn(mProcessInfo).when(tsm).getProcessInfo(argDesc.capture(), argCommands.capture());

            Progress progress = new Progress();
            tsm.delete("testDepositId", fileToDelete, progress, "specificLocation");

            assertThat(argDesc.getValue()).isEqualTo("tsmDelete");
            
            String expectedTsmFile = tsmTemp.resolve("testDepositId").resolve(fileToDelete.getName()).toString();
            assertThat(argCommands.getAllValues()).containsExactly("dsmc","delete","archive", expectedTsmFile, "-noprompt","-optfile=specificLocation");

            //Check that the local file has not been deleted. We are trying to delete file on TSM ONLY
            assertThat(fileToDelete).exists();
        }
        
        @Test
        void testDeleteFails() throws Exception {

            File fileToDelete = Files.createTempFile("test",".txt").toFile();
            assertThat(fileToDelete).exists();

            ProcessHelper.ProcessInfo mProcessInfo = Mockito.mock(ProcessHelper.ProcessInfo.class);
            lenient().when(mProcessInfo.wasFailure()).thenReturn(true);
            lenient().when(mProcessInfo.wasSuccess()).thenReturn(false);
            when(mProcessInfo.getErrorMessages()).thenReturn(Arrays.asList("error-message-1","error-message-2"));
            when(mProcessInfo.getOutputMessages()).thenReturn(Arrays.asList("info-message-1","info-message-2"));

            Mockito.doReturn(mProcessInfo).when(tsm).getProcessInfo(argDesc.capture(), argCommands.capture());

            Progress progress = new Progress();
            Exception ex = assertThrows(Exception.class, () -> {
                tsm.delete("testDepositId", fileToDelete, progress, "specificLocation");
            });
            String expectedTsmFile = tsmTemp.resolve("testDepositId").resolve(fileToDelete.getName()).toString();
            String expectedErrorMessage = String.format("Delete of [%s] failed.",expectedTsmFile);
            assertThat(ex).hasMessage(expectedErrorMessage);

            assertThat(argDesc.getValue()).isEqualTo("tsmDelete");

            assertThat(argCommands.getAllValues()).containsExactly("dsmc","delete","archive", expectedTsmFile, "-noprompt","-optfile=specificLocation");

            //Check that the local file has not been deleted. We are trying to delete file on TSM ONLY
            assertThat(fileToDelete).exists();
        }

    }

    @Nested
    class RetrieveTests {
        
        Path tsmTemp;
        TivoliStorageManager tsm;
        
        @Captor
        ArgumentCaptor<String> argDesc;
        @Captor
        ArgumentCaptor<String> argCommands;
        
        File targetFile;
        
        final String timestampedDir = SftpUtils.getTimestampedDirectoryName(FIXED_CLOCK);

        @BeforeEach
        void setup() throws IOException {
            tsmTemp = tempPath.resolve("tsmTemp");
            Files.createDirectories(tsmTemp);
            
            targetFile = tempPath.resolve("target.txt").toFile();

            Map<String, String> props = new HashMap<>();
            props.put(PropNames.TEMP_DIR, tsmTemp.toString());
            props.put(PropNames.TSM_MAX_RETRIES, "5");
            props.put(PropNames.TSM_RETRY_TIME, "0");
            tsm = Mockito.spy(new TivoliStorageManager("testTSM", props));
            tsm.setClock(FIXED_CLOCK);
        }
         
        void checkGetProcessInfo(InvocationOnMock invocation) {
            assertThat(invocation.getArguments()[0]).isEqualTo("tsmRetrieve");
            assertThat(invocation.getArguments()[1]).isEqualTo("dsmc");
            assertThat(invocation.getArguments()[2]).isEqualTo("retrieve");
            assertThat(invocation.getArguments()[3]).isEqualTo(tsmTemp.resolve("testDepositId").resolve(targetFile.getName()).toString());
            assertThat(invocation.getArguments()[4]).isEqualTo(tsmTemp.resolve(timestampedDir).resolve(targetFile.getName()).toString());
            assertThat(invocation.getArguments()[5]).isEqualTo("-description=testDepositId");
            assertThat(invocation.getArguments()[6]).isEqualTo("-optfile=testLocation");
            assertThat(invocation.getArguments()[7]).isEqualTo("-replace=true");
        }
        
        @ParameterizedTest
        @ValueSource(ints = {1,2,3,4,5})
        void testRetrieveSucceeds(int attemptWhichSucceeds) throws Exception {
            
            Path targetFilePath = targetFile.toPath();
            assertThat(Files.exists(targetFilePath)).isFalse();

            AtomicInteger attempts = new AtomicInteger(0);
            
            Mockito.doAnswer(invocation -> {

                       checkGetProcessInfo(invocation);

                        boolean willSucceed = attemptWhichSucceeds == attempts.incrementAndGet();
                        ProcessHelper.ProcessInfo mProcessInfo = Mockito.mock(ProcessHelper.ProcessInfo.class);
                        lenient().when(mProcessInfo.wasFailure()).thenReturn(!willSucceed);
                        lenient().when(mProcessInfo.wasSuccess()).thenReturn(willSucceed);
                        if (willSucceed) {
                            Path retrieveToParentPath = tsmTemp.resolve(timestampedDir);
                            Files.createDirectories(retrieveToParentPath);
                            File retrieveTo = retrieveToParentPath.resolve(targetFile.getName()).toFile();
                            try (PrintWriter writer = new PrintWriter(new FileWriter(retrieveTo))) {
                                writer.println("line1");
                                writer.println("line2");
                            }
                        }
                        return mProcessInfo;
                    }).when(tsm).getProcessInfo(argDesc.capture(), argCommands.capture());
            
            Progress progress = new Progress();
            tsm.retrieve("testDepositId", targetFile, progress, "testLocation");
            
            assertThat(Files.exists(targetFilePath)).isTrue();
            assertThat(Files.lines(targetFilePath)).containsExactly("line1","line2");
        }

        @ParameterizedTest
        @ValueSource(ints = {1,2,3,4,5})
        void testRetrieveFailsBecauseOfTSMFailure(int attemptWhichProcessSucceeds) throws Exception {

            Path targetFilePath = targetFile.toPath();
            assertThat(Files.exists(targetFilePath)).isFalse();

            AtomicInteger attempts = new AtomicInteger(0);

            Mockito.doAnswer(invocation -> {
                checkGetProcessInfo(invocation);
                boolean willSucceed = attemptWhichProcessSucceeds == attempts.incrementAndGet();
                ProcessHelper.ProcessInfo mProcessInfo = Mockito.mock(ProcessHelper.ProcessInfo.class);
                lenient().when(mProcessInfo.wasFailure()).thenReturn(!willSucceed);
                lenient().when(mProcessInfo.wasSuccess()).thenReturn(willSucceed);
                return mProcessInfo;
            }).when(tsm).getProcessInfo(argDesc.capture(), argCommands.capture());

            Progress progress = new Progress();
            Exception ex = assertThrows(Exception.class, () -> {
                tsm.retrieve("testDepositId", targetFile, progress, "testLocation");
            });
            String expectedMessage = String.format("The file [%s/%s/target.txt] does not exist after retrieved from TSM.", tsmTemp, timestampedDir);
            assertThat(ex).hasMessage(expectedMessage);

            assertThat(Files.exists(targetFilePath)).isFalse();
        }
        
        @Test
        void testRetrieveFailsBecauseOfProcessFailure() throws Exception {

            Path targetFilePath = targetFile.toPath();
            assertThat(Files.exists(targetFilePath)).isFalse();

            AtomicInteger counter = new AtomicInteger();
            Mockito.doAnswer(invocation -> {
                checkGetProcessInfo(invocation);
                counter.incrementAndGet();
                boolean willSucceed = false;
                ProcessHelper.ProcessInfo mProcessInfo = Mockito.mock(ProcessHelper.ProcessInfo.class);
                lenient().when(mProcessInfo.wasFailure()).thenReturn(!willSucceed);
                lenient().when(mProcessInfo.wasSuccess()).thenReturn(willSucceed);
                return mProcessInfo;
            }).when(tsm).getProcessInfo(argDesc.capture(), argCommands.capture());

            Progress progress = new Progress();
            Exception ex = assertThrows(Exception.class, () -> {
                tsm.retrieve("testDepositId", targetFile, progress, "testLocation");
            });
            assertThat(ex).hasMessage("Retrieval of [testDepositId/target.txt] failed using location[testLocation]");
            assertThat(counter.get()).isEqualTo(5);
            assertThat(Files.exists(targetFilePath)).isFalse();
        }
    }
    
    @Nested
    class  CheckTSMTapeDriverTests {
        
        ProcessHelper.ProcessInfo mProcessInfo;
        
        @BeforeEach
        void setup(){
            mProcessInfo = mock(ProcessHelper.ProcessInfo.class);

            lenient().when(mProcessInfo.getErrorMessages()).thenReturn(Collections.emptyList());
            lenient().when(mProcessInfo.isTimedOut()).thenReturn(false);

        }
        
        @Test
        void testSuccess() {

            lenient().when(mProcessInfo.wasSuccess()).thenReturn(true);
            lenient().when(mProcessInfo.wasFailure()).thenReturn(false);
            lenient().when(mProcessInfo.getExitValue()).thenReturn(0);
            lenient().when(mProcessInfo.getOutputMessages()).thenReturn(Collections.singletonList("/tmp/dsmc"));

            checkCheckTSMTapeDrive(true);
        }

        @Test
        void testFailure() {

            lenient().when(mProcessInfo.wasSuccess()).thenReturn(false);
            lenient().when(mProcessInfo.wasFailure()).thenReturn(true);
            lenient().when(mProcessInfo.getExitValue()).thenReturn(1);
            lenient().when(mProcessInfo.getOutputMessages()).thenReturn(Collections.emptyList());

            checkCheckTSMTapeDrive(false);
        }

        void checkCheckTSMTapeDrive(boolean expectedSuccess) {
            try(MockedStatic<TivoliStorageManager.CheckerUtils> mockedCheckerUtils = Mockito.mockStatic(TivoliStorageManager.CheckerUtils.class)){
                
                mockedCheckerUtils.when(() -> {
                    TivoliStorageManager.CheckerUtils.getProcessInfo(any(String.class), any(Duration.class), any(String[].class));                 
                }).thenAnswer(invocation -> {
                    assertThat(invocation.getArguments().length).isEqualTo(4);
                    String argDesc = invocation.getArgument(0, String.class);
                    Duration argDuration = invocation.getArgument(1, Duration.class);
                    String argWhich = invocation.getArgument(2, String.class);
                    String argDsmc = invocation.getArgument(3, String.class);

                    assertThat(argDesc).isEqualTo("tsmCheckTapeDriver");
                    assertThat(argDuration).isEqualTo(Duration.ofSeconds(5));
                    assertThat(argWhich).isEqualTo("which");
                    assertThat(argDsmc).isEqualTo("dsmc");

                    return mProcessInfo;
                });
                
                boolean result = TivoliStorageManager.checkTSMTapeDriver();
                assertThat(result).isEqualTo(expectedSuccess);
            }
        }

   }
}