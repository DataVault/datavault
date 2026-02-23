package org.datavaultplatform.worker;

import lombok.SneakyThrows;
import org.datavaultplatform.common.task.TaskConfig;
import org.datavaultplatform.common.task.TaskConfigTL;
import org.datavaultplatform.common.task.TaskExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ExecutorTest {
    
    @BeforeEach
    void setup() {
        TaskConfigTL.get().setExecutorProperShutdownEnabled(false);
    }
    
    @SneakyThrows
    void performSomeWork() {
        TaskExecutor<String> executor = new TaskExecutor<>(4, "test-te");
        executor.add(getErrorCallable());
        executor.add(getCallable(10, 1, "worker-A"));
        executor.add(getCallable(10, 1, "worker-B"));
        executor.add(getCallable(10, 1, "worker-C"));
        executor.execute();
    }
 
    @Test
    void test() {
        try {
            performSomeWork();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SneakyThrows
    private Callable<String> getErrorCallable() {
        return () -> {
            throw new Exception("oops");
        };
    }

    @SneakyThrows
    private Callable<String> getCallable(int iterations, int sleepTimeSeconds, String label) {
        return () -> {
            for (int i = 0; i < iterations; i++) {
                System.out.printf("%s : iteration %d%n", label, i + 1);
                TimeUnit.SECONDS.sleep(sleepTimeSeconds);
            }
            return label;
        };
    }
}
