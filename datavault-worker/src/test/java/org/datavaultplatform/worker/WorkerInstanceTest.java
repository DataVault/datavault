package org.datavaultplatform.worker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerInstanceTest {

    public String WORKER_NAME_PATTERN = "\\d+@.+";

    @Mock
    Environment mEnv;

    @Mock
    ProcessHandle mProcessHandle;

    @Test
    void testWorkerNameOLD() {
        String workerName = WorkerInstance.getWorkerNameOLD();
        System.out.println(workerName);
        assertThat(workerName).matches(WORKER_NAME_PATTERN);
    }

    @Test
    void testWorkerNameNullEnv() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            WorkerInstance.getWorkerName(null);
        });
        assertThat(ex).hasMessage("The env cannot be null");
    }

    @Test
    void testWorkerNameEnvHasSpringApplicationName() {

        try(MockedStatic<ProcessHandle> mockedStatic = Mockito.mockStatic(ProcessHandle.class)){
            when(mEnv.getProperty("spring.application.name","datavault-worker")).thenReturn("worker-1");
            when(mProcessHandle.pid()).thenReturn(12345L);
            mockedStatic.when(ProcessHandle::current).thenReturn(mProcessHandle);

            String workerName = WorkerInstance.getWorkerName(mEnv);
            assertThat(workerName).isEqualTo("12345@worker-1");
            assertThat(workerName).matches(WORKER_NAME_PATTERN);
            
            verify(mEnv).getProperty("spring.application.name","datavault-worker");
            verify(mProcessHandle).pid();
            mockedStatic.verify(ProcessHandle::current);
        }
    }

    @Test
    void testWorkerNameEnvHasNoSpringApplicationName() {

        try(MockedStatic<ProcessHandle> mockedStatic = Mockito.mockStatic(ProcessHandle.class)){
            when(mEnv.getProperty("spring.application.name","datavault-worker")).thenReturn("datavault-worker");
            when(mProcessHandle.pid()).thenReturn(12345L);
            mockedStatic.when(ProcessHandle::current).thenReturn(mProcessHandle);

            String workerName = WorkerInstance.getWorkerName(mEnv);
            assertThat(workerName).isEqualTo("12345@datavault-worker");
            assertThat(workerName).matches(WORKER_NAME_PATTERN);

            verify(mEnv).getProperty("spring.application.name","datavault-worker");
            verify(mProcessHandle).pid();
            mockedStatic.verify(ProcessHandle::current);
        }
    }
}