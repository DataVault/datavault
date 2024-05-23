package org.datavaultplatform.worker.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.queue.Receiver;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TopLevelRabbitMessageProcessor implements RabbitMessageProcessor {

    private final Receiver receiver;
    private final ShutdownHandler shutdownHandler;

    public TopLevelRabbitMessageProcessor(Receiver receiver, ShutdownHandler shutdownHandler) {
        this.receiver = receiver;
        this.shutdownHandler = shutdownHandler;
    }

    @Override
    public void onMessage(RabbitMessageInfo rabbitMessageInfo) {
        if(rabbitMessageInfo.isShutdown()){
            shutdownHandler.handleShutdown(rabbitMessageInfo);
        }else{
            receiver.onMessage(rabbitMessageInfo);
        }
    }
}
