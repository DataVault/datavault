package org.datavaultplatform.broker.queue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageIdProcessedListener {
    public void processedMessageId(String messageId) {
        log.debug("Processed Message Id: [{}]", messageId);
    }
}
