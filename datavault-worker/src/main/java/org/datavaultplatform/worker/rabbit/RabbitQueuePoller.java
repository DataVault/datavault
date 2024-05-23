package org.datavaultplatform.worker.rabbit;

import lombok.Getter;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

@Getter
public class RabbitQueuePoller {
    private final String queueName;
    private final RabbitTemplate template;
    private final long pollWaitMs;
    private final boolean hiPriority;

    public RabbitQueuePoller(boolean hiPriority, String queueName, RabbitTemplate template, long pollWaitMs) {
        this.queueName = queueName;
        this.template = template;
        this.pollWaitMs = pollWaitMs;
        this.hiPriority = hiPriority;
    }

    public Optional<RabbitMessageInfo> poll() {
        Message message = template.receive(queueName, pollWaitMs);
        return Optional.ofNullable(message)
                .map(msg -> new RabbitMessageInfo(hiPriority, msg, queueName));
    }

}
