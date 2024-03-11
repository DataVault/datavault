package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public abstract class BasePerformDepositThenRetrieveIT extends BaseDepositIntegrationTest {

    File retrieveBaseDir;
    File retrieveDir;

    @Override
    void taskSpecificSetup() throws IOException {
        Path baseTemp = Paths.get(this.tempDir);
        retrieveBaseDir = baseTemp.resolve("retrieve").toFile();
        retrieveDir = retrieveBaseDir.toPath().resolve("ret-folder").toFile();
        Files.createDirectories(retrieveBaseDir.toPath());
        Files.createDirectories(retrieveDir.toPath());
        assertThat(retrieveBaseDir).exists();
        assertThat(retrieveBaseDir).isDirectory();

        assertThat(retrieveDir).exists();
        assertThat(retrieveDir).isDirectory();
        log.info("retrieve base dir [{}]", retrieveBaseDir);
        log.info("retrieve dir [{}]", retrieveDir);

    }

    @Test
    @SneakyThrows
    void testDepositThenRetrieve() {
        assertEquals(0, destDir.listFiles().length);
        String depositMessage = getSampleDepositMessage();
        Deposit deposit = new ObjectMapper().readValue(depositMessage, Deposit.class);
        log.info("depositMessage {}", depositMessage);
        sendNormalMessage(depositMessage);
        waitUntil(this::foundComplete);

        DepositEvents depositEvents = new DepositEvents(deposit, this.events);

        checkDepositWorkedOkay(depositMessage, depositEvents);

        buildAndSendRetrieveMessage(depositEvents);
        checkRetrieve();

    }

    @SneakyThrows
    private void checkRetrieve() {
        log.info("FIN {}", retrieveDir.getCanonicalPath());
        waitUntil(this::foundRetrieveComplete);
        log.info("FIN {}", retrieveDir.getCanonicalPath());
        File retrieved = new File(this.retrieveDir + "/"  + SRC_PATH_DEFAULT + "/src-file-1");

        String digestOriginal = Verify.getDigest(this.largeFile.getFile());
        String digestRetrieved = Verify.getDigest(retrieved);

        assertEquals(digestOriginal, digestRetrieved);
    }

    boolean foundRetrieveComplete() {
        return events.stream()
                .anyMatch(e -> e.getClass().equals(RetrieveComplete.class));
    }

    @SneakyThrows
    private void buildAndSendRetrieveMessage(DepositEvents depositEvents) {
        String retrieveMessage2 = depositEvents.generateRetrieveMessage(this.retrieveBaseDir, this.retrieveDir.getName());
        sendNormalMessage(retrieveMessage2);
    }
}
