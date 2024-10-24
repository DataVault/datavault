package org.datavaultplatform.worker.rabbit;

public interface RabbitMessageProcessor {
    void onMessage(RabbitMessageInfo rabbitMessageInfo);
}
