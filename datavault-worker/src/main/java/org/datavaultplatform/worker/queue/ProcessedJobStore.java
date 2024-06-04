package org.datavaultplatform.worker.queue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * This is not an efficient implementation, but the number of jobs that DataVault processes is low volume ATM.
 * TODO : We can revisit if Workers ever get a connection to the datavault database.
 */
@Slf4j
public class ProcessedJobStore {

    private static final TypeReference<ArrayList<ProcessedJob>> PROCESSED_JOB_LIST_TYPE_REF = new TypeReference<>() {
    };

    private final Clock clock;
    private final Path jobStorePath;
    private final ObjectMapper mapper;

    @SneakyThrows
    public ProcessedJobStore(Clock clock, Path jobStorePath) {
        this.clock = clock;
        this.jobStorePath = jobStorePath;
        if (!Files.exists(jobStorePath)) {
            Files.createFile(jobStorePath);
        }
        Assert.isTrue(Files.isRegularFile(jobStorePath), "The jobStore path [%s] is not a regular file".formatted(jobStorePath));
        Assert.isTrue(Files.isReadable(jobStorePath), "The jobStore path [%s] is not readable".formatted(jobStorePath));
        Assert.isTrue(Files.isWritable(jobStorePath), "The jobStore path [%s] is not writable".formatted(jobStorePath));
        mapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @SneakyThrows
    protected synchronized void storeProcessedJob(String jobID) {
        if (this.isProcessedJob(jobID)) {
            return;
        }
        ProcessedJob processedJob = new ProcessedJob(jobID, ZonedDateTime.now(clock));
        ArrayList<ProcessedJob> processedJobs = getExistingJobs();
        processedJobs.add(processedJob);
        String json = mapper.writeValueAsString(processedJobs);
        log.debug("processedJobs[{}]", json);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jobStorePath.toFile(), StandardCharsets.UTF_8))) {
            writer.write(json);
        }
    }

    @SneakyThrows
    private ArrayList<ProcessedJob> getExistingJobs() {
        if (Files.size(jobStorePath) != 0) {
            return mapper.readValue(jobStorePath.toFile(), PROCESSED_JOB_LIST_TYPE_REF);
        } else {
            return new ArrayList<>();
        }
    }

    protected boolean isProcessedJob(String jobID) {
        return this.getExistingJobs().stream().anyMatch(pj -> jobID.equals(pj.JobID));
    }

    public synchronized long size() {
        return getExistingJobs().size();
    }

    record ProcessedJob(String JobID, ZonedDateTime timestamp) {
    }
}
