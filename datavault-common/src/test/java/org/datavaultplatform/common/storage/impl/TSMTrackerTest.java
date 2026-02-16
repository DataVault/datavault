package org.datavaultplatform.common.storage.impl;

import org.assertj.core.api.Assertions;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.util.ProcessHelper;
import org.datavaultplatform.common.util.ProcessInfo;
import org.datavaultplatform.common.util.TestUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TSMTrackerTest {
    
    
    @ValueSource(ints = {1,2,3,4,5})
    @ParameterizedTest
    void testSucceedsAfterNthAttempt(int attemptWhichSucceeds) throws Exception {
        String location = "testLocation";
        File mWorking = mock(File.class);
        when(mWorking.getAbsolutePath()).thenReturn("/test/absolute/path");
        
        Progress progress = new Progress();
        String description = "testDescription";
        int maxRetries = 5;
        int retryTimeMins=0;

        AtomicInteger attempts = new AtomicInteger(0);
        
        TSMTracker tracker = spy(new TSMTracker(location, mWorking, progress, description, maxRetries, retryTimeMins));
        
        doAnswer(invocation -> {
            
            assertThat(invocation.getArgument(0, String.class)).isEqualTo("tsmStore");

            String[] expectedCommands = TivoliStorageManager.cleanTsmCommand(
                    "dsmc",
                    "archive",
                    "/test/absolute/path",
                    "-description=testDescription",
                    "-optfile=testLocation");
            
            TestUtils.testExpectedCommands(invocation, expectedCommands);

            boolean willSucceed = attempts.incrementAndGet() == attemptWhichSucceeds;
            ProcessInfo mProcessInfo = Mockito.mock(ProcessInfo.class);
            if(willSucceed){
                lenient().when(mProcessInfo.wasSuccess()).thenReturn(true);
                lenient().when(mProcessInfo.wasFailure()).thenReturn(false);
            }else{
                lenient().when(mProcessInfo.wasSuccess()).thenReturn(false);
                lenient().when(mProcessInfo.wasFailure()).thenReturn(true);
            }
            return mProcessInfo;
        }).when(tracker).getProcessInfo(any(String.class), any(String[].class));
        
        String result = tracker.call();
        assertThat(result).isEqualTo(description);
        
        assertThat(attempts.get()).isEqualTo(attemptWhichSucceeds);

    }
    @ValueSource(ints = {1,2,3})
    @ParameterizedTest
    void testFailsDuringFinalAttempt(int finalAttemptNumber) throws Exception {
        String location = "testLocation";
        File mWorking = mock(File.class);
        when(mWorking.getAbsolutePath()).thenReturn("/test/absolute/path");

        Progress progress = new Progress();
        String description = "testDescription";
        @SuppressWarnings("UnnecessaryLocalVariable")
        int maxRetries = finalAttemptNumber;
        int retryTimeMins=0;

        AtomicInteger attempts = new AtomicInteger(0);

        TSMTracker tracker = spy(new TSMTracker(location, mWorking, progress, description, maxRetries, retryTimeMins));

        doAnswer(invocation -> {

            attempts.incrementAndGet();
            
            Assertions.assertThat(invocation.getArguments()[0]).isEqualTo("tsmStore");

            String[] expectedCommands = TivoliStorageManager.cleanTsmCommand(
                    "dsmc",
                    "archive",
                    "/test/absolute/path",
                    "-description=testDescription",
                    "-optfile=testLocation");

            TestUtils.testExpectedCommands(invocation, expectedCommands);
            
            ProcessInfo mProcessInfo = Mockito.mock(ProcessInfo.class);
                lenient().when(mProcessInfo.wasSuccess()).thenReturn(false);
                lenient().when(mProcessInfo.wasFailure()).thenReturn(true);
                lenient().when(mProcessInfo.getOutputMessages()).thenReturn(Arrays.asList("message1","message2"));
            return mProcessInfo;
        }).when(tracker).getProcessInfo(any(String.class), any(String[].class));

        Exception ex = assertThrows(Exception.class, tracker::call);
        assertThat(ex).hasMessage("Storing [testDescription] in TSM location[testLocation] failed.");
        assertThat(attempts.get()).isEqualTo(finalAttemptNumber);
    }

}