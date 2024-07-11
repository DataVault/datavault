package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.audit.AuditComplete;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public abstract class BasePerformDepositThenAuditIT extends BaseDepositIT {

  abstract void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize);

  @Test
  @SneakyThrows
  void testDepositThenAudit() {
    assertEquals(0, destDir.listFiles().length);

    String depositMessage = getSampleDepositMessage();
    Deposit deposit = new ObjectMapper().readValue(depositMessage, Deposit.class);
    log.info("depositMessage {}", depositMessage);
    sendNormalMessage(depositMessage);
    waitUntil(this::foundComplete);

    DepositEvents depositEvents = new DepositEvents(deposit, this.events);

    checkDepositWorkedOkay(depositMessage, depositEvents);

    String auditMessage = buildAuditMessage(depositEvents);

    sendNormalMessage(auditMessage);
    checkAudit();
  }

  @SneakyThrows
  final void checkAudit() {
    waitUntil(this::foundAuditComplete);
  }

  boolean foundAuditComplete() {
    synchronized (events) {
      return events.stream()
              .anyMatch(e -> e.getClass().equals(AuditComplete.class));
    }
  }

  @SuppressWarnings("UnnecessaryLocalVariable")
  @SneakyThrows
  final String buildAuditMessage(List<DepositEvents> depositEvents) {
    String auditMessage = DepositEvents.generateAuditMessage(depositEvents);
    return auditMessage;
  }
  @SneakyThrows
  final String buildAuditMessage(DepositEvents depositEvents) {
    return buildAuditMessage(Collections.singletonList(depositEvents));
  }

  @Override
  void taskSpecificSetup() {
  }

}