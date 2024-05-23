package org.datavaultplatform.worker.rabbit;

import org.springframework.amqp.core.Message;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;

/**
 * Used to hold a message - before the message is selected for processing or rejected
 * @param hiPriority
 * @param message
 * @param queueName
 */
public record RabbitMessageInfo(boolean hiPriority, Message message, String queueName) implements Comparable<RabbitMessageInfo> {

    public static final String SHUTDOWN = "shutdown";

    public static final Comparator<RabbitMessageInfo> COMP =
            Comparator.comparing(RabbitMessageInfo::hiPriority).reversed()
                    .thenComparing(wm -> wm.message.getMessageProperties(). getTimestamp())
                    .thenComparing(wm -> wm.message.getMessageProperties(). getMessageId());

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(RabbitMessageInfo o) {
        return COMP.compare(this, o);
    }

    public String getMessageBody(){
        return new String(message.getBody(), StandardCharsets.UTF_8);
    }
    
    boolean isShutdown() {
        return SHUTDOWN.equalsIgnoreCase(getMessageBody());
    }

}
