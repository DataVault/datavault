package org.datavaultplatform.worker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventStream;
import org.datavaultplatform.common.model.Agent;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * A Spring Bean to add Events to the event queue
 */
@Slf4j
public class EventSender implements EventStream {

    private final RabbitTemplate template;
    private final String eventQueueName;

    private final String workerName;

    private final AtomicInteger sequence;
    private final ObjectMapper mapper;

    public EventSender(RabbitTemplate template, String eventQueueName, String workerName, int sequenceStart, ObjectMapper mapper) {
        this.template = template;
        this.eventQueueName = eventQueueName;
        this.workerName = workerName;
        this.sequence = new AtomicInteger(sequenceStart);
        this.mapper = mapper;
    }

    /* (non-Javadoc)
     * @see org.datavaultplatform.common.event.EventStream#send(org.datavaultplatform.common.event.Event)
     * 
     * Add a sequence to the event to allow ordering where the timestamp is equal,
     * then encode the event as json, and publish the message to the defined queue.
     * 
     * @param event An event object
     */
    @Override
    public void send(Event event) {

        // Add sequence for event ordering (where timestamp is equal)
        event.setSequence(sequence.getAndIncrement());

        // Set common event properties
        event.setAgentType(Agent.AgentType.WORKER);
        event.setAgent(workerName);
        
        try {
            String jsonEvent = mapper.writeValueAsString(event);
            byte[] messageBytes = jsonEvent.getBytes(StandardCharsets.UTF_8);
            String messageId = UUID.randomUUID().toString();
            Message message = new Message(messageBytes, getProps(messageId));
            template.send(this.eventQueueName, message);
            log.debug("Sent msgId[{}]size[{}]bytes",messageBytes.length);
            log.debug("Sent msgId[{}]message body [{}]", jsonEvent);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    MessageProperties getProps(String messageId) {
        MessageProperties result = new MessageProperties();
        result.setMessageId(messageId);

        // TODO this should be application/json really
        result.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);

        // we could add extra 'headers' to message if we like
        return result;
    }
}
