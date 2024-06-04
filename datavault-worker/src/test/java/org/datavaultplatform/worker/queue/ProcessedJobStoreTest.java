package org.datavaultplatform.worker.queue;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedJobStoreTest {

    Clock clock;
    Path processedJobStorePath;
    ProcessedJobStore processedJobStore;

    @BeforeEach
    @SneakyThrows
    void setup() {
       clock = Clock.systemDefaultZone(); 
       processedJobStorePath = Files.createTempFile("processedJobStore",".json"); 
       processedJobStore = new ProcessedJobStore(clock, processedJobStorePath);        
       assertThat(processedJobStore.size()).isEqualTo(0);
    }
    
    @Test
    void testJobStore() {
        // initial checks
        assertThat(processedJobStore.size()).isEqualTo(0);
        assertThat(processedJobStore.isProcessedJob("abc")).isFalse();
        
        // add job:001
        processedJobStore.storeProcessedJob("001");
        assertThat(processedJobStore.size()).isEqualTo(1);
        assertThat(processedJobStore.isProcessedJob("001")).isTrue();
        assertThat(processedJobStore.isProcessedJob("002")).isFalse();

        // add job:002
        processedJobStore.storeProcessedJob("002");
        assertThat(processedJobStore.size()).isEqualTo(2);
        assertThat(processedJobStore.isProcessedJob("001")).isTrue();
        assertThat(processedJobStore.isProcessedJob("002")).isTrue();
        assertThat(processedJobStore.isProcessedJob("003")).isFalse();

        // add job:002 again (doesn't add as already stored)
        processedJobStore.storeProcessedJob("002");
        assertThat(processedJobStore.size()).isEqualTo(2);
        assertThat(processedJobStore.isProcessedJob("001")).isTrue();
        assertThat(processedJobStore.isProcessedJob("002")).isTrue();
        assertThat(processedJobStore.isProcessedJob("003")).isFalse();
        
        List<String> jobIds= new ArrayList<>();
        for(int i=0;i<500;i++){
                String jobId = UUID.randomUUID().toString();
                jobIds.add(jobId);
                processedJobStore.storeProcessedJob(jobId);
        }
        for(String jobId : jobIds){
            assertThat(processedJobStore.isProcessedJob(jobId)).isTrue();
        }
    }
}