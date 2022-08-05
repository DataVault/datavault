package org.datavaultplatform.worker.queue;

import java.nio.file.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventStream;
import org.datavaultplatform.common.io.FileUtils;

import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Context.AESMode;
import org.datavaultplatform.worker.WorkerInstance;
import org.datavaultplatform.worker.rabbit.MessageInfo;
import org.datavaultplatform.worker.rabbit.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to review messages from the message queue then process them.
 */
public class Receiver implements MessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    private final String tempDir;
    private final String metaDir;
    private final boolean chunkingEnabled;
    private final Long chunkingByteSize;
    private final boolean encryptionEnabled;
    private final AESMode  encryptionMode;
    private final Boolean multipleValidationEnabled;
    private final int noChunkThreads;

    private EventStream eventStream;

    public Receiver(
        String tempDir,
        String metaDir,

        boolean chunkingEnabled,
        String chunkingByteSize,

        boolean encryptionEnabled,
        String encryptionMode,

        boolean multipleValidationEnabled,
        int noChunkThreads,

        EventSender eventSender) {
        this.tempDir = tempDir;
        this.metaDir = metaDir;
        this.chunkingEnabled = chunkingEnabled;
        this.chunkingByteSize = FileUtils.parseFormattedSizeToBytes(chunkingByteSize);

        this.encryptionEnabled = encryptionEnabled;
        this.encryptionMode = AESMode.valueOf(encryptionMode);

        this.multipleValidationEnabled = multipleValidationEnabled;
        this.noChunkThreads = noChunkThreads;
        this.eventStream = eventSender;
    }

    @Override
    public boolean processMessage(MessageInfo messageInfo) {
            long start = System.currentTimeMillis();
            String message = messageInfo.getValue();

            // Decode and begin the job ...
            Path tempDirPath = null;
            try {
                ObjectMapper mapper = new ObjectMapper();
                Task commonTask = mapper.readValue(message, Task.class);
                
                Class<?> clazz = Class.forName(commonTask.getTaskClass());
                Task concreteTask = (Task)(mapper.readValue(message, clazz));

                // Is the message a redelivery?
                if (messageInfo.getIsRedeliver()) {
                    concreteTask.setIsRedeliver(true);
                }

                // Set up the worker temporary directory
                Event lastEvent = concreteTask.getLastEvent();
                if (lastEvent != null) {
                    logger.debug("Restart using old temp dir");
                    tempDirPath = Paths.get(tempDir,  lastEvent.getAgent());
                } else {
                    logger.debug("Normal using default temp dir");
                    tempDirPath = Paths.get(tempDir, WorkerInstance.getWorkerName());
                }
                logger.debug("The temp dir:" + tempDirPath.toString());
                tempDirPath.toFile().mkdir();
                
                Path metaDirPath = Paths.get(metaDir);

                String vaultAddress = null;
                String vaultToken = null;
                String vaultKeyName = null;
                String vaultKeyPath = null;
                String vaultSslPEMPath = null;

                Context context = new Context(
                        tempDirPath, metaDirPath, eventStream,
                        chunkingEnabled, chunkingByteSize,
                        encryptionEnabled, encryptionMode, 
                        vaultAddress, vaultToken, 
                        vaultKeyPath, vaultKeyName, vaultSslPEMPath,
                        this.multipleValidationEnabled, this.noChunkThreads);
                concreteTask.performAction(context);

                // Clean up the temporary directory (if success if failure we need it for retries)
                FileUtils.deleteDirectory(tempDirPath.toFile());
            } catch (Exception ex) {
                logger.error("Error processing message[{}]", messageInfo, ex);
            } finally {
                long diff = System.currentTimeMillis() - start;
                logger.info("Finished Processing message[{}]. Took [{}]secs",
                    messageInfo, TimeUnit.MILLISECONDS.toSeconds(diff));
            }

            return false;
        }

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }
    public AESMode getEncryptionMode() {
        return this.encryptionMode;
    }
}