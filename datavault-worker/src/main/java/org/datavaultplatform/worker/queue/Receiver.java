package org.datavaultplatform.worker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.RecordingEventSender;
import org.datavaultplatform.common.io.DataVaultFileUtils;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Context.AESMode;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.worker.WorkerInstance;
import org.datavaultplatform.worker.rabbit.RabbitMessageInfo;
import org.datavaultplatform.worker.rabbit.RabbitMessageProcessor;
import org.datavaultplatform.worker.tasks.Deposit;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * A class to review messages from the message queue then process them.
 */
@Slf4j
public class Receiver implements RabbitMessageProcessor{

    private final String tempDir;
    private final String metaDir;
    private final boolean chunkingEnabled;
    private final long chunkingByteSize;
    private final boolean encryptionEnabled;
    private final AESMode  encryptionMode;
    private final boolean multipleValidationEnabled;
    private final int noChunkThreads;

    private final EventSender eventSender;

    private final StorageClassNameResolver storageClassNameResolver;

    private final boolean oldRecompose;

    private final String recomposeDate;
    
    private final ProcessedJobStore processedJobStore;
    
    public Receiver(
        String tempDir,
        String metaDir,

        boolean chunkingEnabled,
        String chunkingByteSize,

        boolean encryptionEnabled,
        String encryptionMode,

        boolean multipleValidationEnabled,
        int noChunkThreads,

        EventSender eventSender,
        StorageClassNameResolver storageClassNameResolver,
        boolean oldRecompose, String recomposeDate,
        ProcessedJobStore processedJobStore) {
        this.tempDir = tempDir;
        this.metaDir = metaDir;
        this.chunkingEnabled = chunkingEnabled;
        this.chunkingByteSize = DataVaultFileUtils.parseFormattedSizeToBytes(chunkingByteSize);

        this.encryptionEnabled = encryptionEnabled;
        this.encryptionMode = AESMode.valueOf(encryptionMode);

        this.multipleValidationEnabled = multipleValidationEnabled;
        this.noChunkThreads = noChunkThreads;
        this.eventSender = eventSender;
        this.storageClassNameResolver = storageClassNameResolver;
        this.oldRecompose = oldRecompose;
        this.recomposeDate = recomposeDate;
        this.processedJobStore = processedJobStore;
    }

    @Override
    public void onMessage(RabbitMessageInfo rabbitMessageInfo) {
        try {
            if (eventSender instanceof RecordingEventSender) {
                ((RecordingEventSender) eventSender).clear();
            }
            processMessageInternal(rabbitMessageInfo);
        } finally {
            if (eventSender instanceof RecordingEventSender res) {
                List<Event> events = res.getEvents();

                generateRetrieveMessageForDeposit(rabbitMessageInfo, events, new File("/tmp/retrieve"),
                    "retDir");

                if (log.isTraceEnabled()) {
                    log.trace("events send by worker: {}", events);
                }
            }
        }
    }

    private void generateRetrieveMessageForDeposit(RabbitMessageInfo messageInfo, List<Event> events, File retrieveBaseDir, String retrievePath ) {
        try {
            String message = messageInfo.getMessageBody();
            Task task = new ObjectMapper().readValue(message, Task.class);
            if (!"org.datavaultplatform.worker.tasks.Deposit".equals(task.getTaskClass())) {
                return;
            }
            Deposit deposit = new ObjectMapper().readValue(message, Deposit.class);
            DepositEvents de = new DepositEvents(deposit, events);
            String retrieveMessage = de.generateRetrieveMessage(retrieveBaseDir, retrievePath);
            log.info("retrieveMessageForDeposit {}", retrieveMessage);
        } catch(Exception ex) {
            log.warn("Failed to generate retrieveMessageForDeposit", ex);
        }
    }

    private void processMessageInternal(RabbitMessageInfo messageInfo) {
            long start = System.currentTimeMillis();
            String message = messageInfo.getMessageBody();

            // Decode and begin the job ...
            Path tempDirPath;
            try {
                ObjectMapper mapper = new ObjectMapper();

                String json = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(mapper.readTree(message));
                log.info("messageId[{}] json[{}]", messageInfo.message().getMessageProperties().getMessageId(), json);

                Task commonTask = mapper.readValue(message, Task.class);
                
                Class<?> clazz = Class.forName(commonTask.getTaskClass());
                Task concreteTask = (Task) mapper.readValue(message, clazz);

                // Is the message a redelivery?
                if (messageInfo.message().getMessageProperties().isRedelivered()) {
                    concreteTask.setIsRedeliver(true);
                }

                // Set up the worker temporary directory
                Event lastEvent = concreteTask.getLastEvent();
                if (lastEvent != null) {
                    log.debug("Restart using old temp dir");
                    tempDirPath = Paths.get(tempDir,  lastEvent.getAgent());
                } else {
                    log.debug("Normal using default temp dir");
                    tempDirPath = Paths.get(tempDir, WorkerInstance.getWorkerName());
                }
                log.debug("The temp dir:" + tempDirPath);
                tempDirPath.toFile().mkdir();
                
                Path metaDirPath = Paths.get(metaDir);

                String vaultAddress = null;
                String vaultToken = null;
                String vaultKeyName = null;
                String vaultKeyPath = null;
                String vaultSslPEMPath = null;

                Context context = new Context(
                        tempDirPath, metaDirPath, eventSender,
                        chunkingEnabled, chunkingByteSize,
                        encryptionEnabled, encryptionMode, 
                        vaultAddress, vaultToken, 
                        vaultKeyPath, vaultKeyName, vaultSslPEMPath,
                        this.multipleValidationEnabled, this.noChunkThreads,
                        this.storageClassNameResolver,
                        this.oldRecompose,
                        this.recomposeDate);

                if (isRestartTaskForOtherWorker(commonTask)) {
                    return;
                }
                processedJobStore.storeProcessedJob(concreteTask.getJobID());
                concreteTask.performAction(context);

                // Clean up the temporary directory (if success if failure we need it for retries)
                FileUtils.deleteDirectory(tempDirPath.toFile());
            } catch (Exception ex) {
                log.error("Error processing message[{}]", messageInfo, ex);
            } finally {
                long diff = System.currentTimeMillis() - start;
                log.info("Finished Processing message[{}]. Took [{}]secs",
                    messageInfo, TimeUnit.MILLISECONDS.toSeconds(diff));
            }
     }

    private boolean isRestartTaskForOtherWorker(Task commonTask) {
        Assert.isTrue(commonTask != null, "The commonTask cannot be null");
        Assert.isTrue(StringUtils.isNotBlank(commonTask.getJobID()), "The commonTask.JobID cannot be null");

        boolean isRestart = commonTask.getLastEvent() != null;
        if (isRestart) {
            String nonRestartJobId = commonTask.getProperties().get(PropNames.NON_RESTART_JOB_ID);
            return !processedJobStore.isProcessedJob(nonRestartJobId);
        } else {
            return false;
        }
    }
    
    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }
    public AESMode getEncryptionMode() {
        return this.encryptionMode;
    }
    
}