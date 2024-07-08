package org.datavaultplatform.worker.queue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * TODO : We could revisit if Workers ever get a connection to the datavault database.
 */
@Slf4j
public class ProcessedJobStore {

    private static final TypeReference<ArrayList<ProcessedJob>> PROCESSED_JOB_LIST_TYPE_REF = new TypeReference<>() {
    };

    private final Clock clock;
    private final Path jobStorePath;
    private final ObjectMapper mapper;

    @SneakyThrows
    public ProcessedJobStore(ObjectMapper mapper,Clock clock, Path jobStorePath) {
        this.mapper = mapper;
        this.clock = clock;
        this.jobStorePath = jobStorePath;
        if (!Files.exists(jobStorePath)) {
            Files.createFile(jobStorePath);
        }
        Assert.isTrue(Files.isRegularFile(jobStorePath), "The jobStore path [%s] is not a regular file".formatted(jobStorePath));
        Assert.isTrue(Files.isReadable(jobStorePath), "The jobStore path [%s] is not readable".formatted(jobStorePath));
        Assert.isTrue(Files.isWritable(jobStorePath), "The jobStore path [%s] is not writable".formatted(jobStorePath));
    }

    @SneakyThrows
    protected synchronized void storeProcessedJob(String jobId) {
        if(jobId == null){
            return;
        }
        if (this.isProcessedJob(jobId)) {
            return;
        }
        ProcessedJob processedJob = new ProcessedJob(jobId, ZonedDateTime.now(clock));
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

    protected boolean isProcessedJob(String jobId) {
        if (StringUtils.isBlank(jobId)) {
            log.warn("BLANK jobId[{}]", jobId);
            return true;
        } else {
            return this.getExistingJobs().stream().anyMatch(pj -> jobId.equals(pj.jobId));
        }
    }

    public synchronized long size() {
        return getExistingJobs().size();
    }

    record ProcessedJob(String jobId, ZonedDateTime timestamp) {
    }
}
