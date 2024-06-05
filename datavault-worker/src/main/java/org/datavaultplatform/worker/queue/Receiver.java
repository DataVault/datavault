package org.datavaultplatform.worker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.RecordingEventSender;
import org.datavaultplatform.common.io.DataVaultFileUtils;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Context.AESMode;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.worker.WorkerInstance;
import org.datavaultplatform.worker.rabbit.RabbitMessageInfo;
import org.datavaultplatform.worker.rabbit.RabbitMessageProcessor;
import org.datavaultplatform.worker.tasks.Deposit;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.springframework.amqp.core.MessageProperties;
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
    @Getter
    private final boolean encryptionEnabled;
    @Getter
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
            if (eventSender instanceof RecordingEventSender res) {
                res.clear();
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
            if (!Job.TASK_CLASS_DEPOSIT.equals(task.getTaskClass())) {
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
            MessageProperties props = messageInfo.message().getMessageProperties();
            // Decode and begin the job ...
            try {
                logMessageAsFormattedJson(props.getMessageId(), message);
 
                Task concreteTask = getConcreteTask(message);

                // Is the message a redelivery ?
                if (props.isRedelivered()) {
                    concreteTask.setIsRedeliver(true);
                }

                // Set up the worker temporary directory
                Event lastEvent = concreteTask.getLastEvent();
                Path tempDirPath = getTempDirPath(lastEvent);
                
                Context context = getContext(tempDirPath);

                if (isRestartTaskForOtherWorker(concreteTask)) {
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
    

    private Path getTempDirPath(Event lastEvent) {
        Path tempDirPath;
        if (lastEvent != null) {
            log.debug("Restart using old temp dir");
            tempDirPath = Paths.get(tempDir,  lastEvent.getAgent());
        } else {
            log.debug("Normal using default temp dir");
            tempDirPath = Paths.get(tempDir, WorkerInstance.getWorkerName());
        }
        log.debug("The temp dir: [{}]",tempDirPath);
        tempDirPath.toFile().mkdir();
        return tempDirPath;
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
    
    @SneakyThrows
    protected Task getConcreteTask(String message) {
        ObjectMapper mapper = new ObjectMapper();
        Task commonTask = mapper.readValue(message, Task.class);

        Class<? extends Task> clazz = Class.forName(commonTask.getTaskClass()).asSubclass(Task.class);
        return mapper.readValue(message, clazz);
    }

    @SneakyThrows
    private void logMessageAsFormattedJson(String messageId, String message) {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(message));
        log.info("messageId[{}] json[{}]", messageId, json);
    }

    protected Context getContext(Path tempDirPath) {

        Path metaDirPath = Paths.get(metaDir);

        String vaultAddress = null;
        String vaultToken = null;
        String vaultKeyName = null;
        String vaultKeyPath = null;
        String vaultSslPEMPath = null;

        return new Context(
                tempDirPath, metaDirPath, this.eventSender,
                this.chunkingEnabled, this.chunkingByteSize,
                this.encryptionEnabled, this.encryptionMode,
                vaultAddress, vaultToken,
                vaultKeyPath, vaultKeyName, vaultSslPEMPath,
                this.multipleValidationEnabled, this.noChunkThreads,
                this.storageClassNameResolver,
                this.oldRecompose,
                this.recomposeDate);
    }
}