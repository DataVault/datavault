package org.datavaultplatform.worker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventStream;
import org.datavaultplatform.common.model.Agent;
import org.datavaultplatform.worker.WorkerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ?
 * A class to add Events to the event queue
 *
 */
public class EventSender implements EventStream {

    private static final Logger logger = LoggerFactory.getLogger(EventSender.class);
    
    private String queueServer;
    private String eventQueueName;
    private String queueUser;
    private String queuePassword;
    
    private int sequence = 0;

    /**
     * Setter for the queue server
     * @param queueServer the queue server value
     */
    public void setQueueServer(String queueServer) {
        this.queueServer = queueServer;
    }

    /**
     * Setter for the event queue name
     * @param eventQueueName the eventQueueName value
     */
    public void setEventQueueName(String eventQueueName) {
        this.eventQueueName = eventQueueName;
    }

    /**
     * Setter for the queue user
     * @param queueUser the queueUser value
     */
    public void setQueueUser(String queueUser) {
        this.queueUser = queueUser;
    }

    /**
     * Setter for the queuePassword
     * @param queuePassword the queuePassword value
     */
    public void setQueuePassword(String queuePassword) {
        this.queuePassword = queuePassword;
    }
    
    /* (non-Javadoc)
     * @see org.datavaultplatform.common.event.EventStream#send(org.datavaultplatform.common.event.Event)
     * 
     * Add a sequence to the event to allow ordering where the timestamp is equal, create a connection to the queue
     * then encode the event as json, declare the queue and publish the message.
     * 
     * Finally close the connection.
     * 
     * @param event An event object
     */
    @Override
    public void send(Event event) {
        
        // Add sequence for event ordering (where timestamp is equal)
        event.setSequence(sequence);
        sequence += 1;
        
        // Set common event properties
        event.setAgentType(Agent.AgentType.WORKER);
        event.setAgent(WorkerInstance.getWorkerName());
        
        try {
            // TODO: should create queue once and keep open?
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(queueServer);
            factory.setUsername(queueUser);
            factory.setPassword(queuePassword);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            ObjectMapper mapper = new ObjectMapper();
            String jsonEvent = mapper.writeValueAsString(event);

            channel.queueDeclare(eventQueueName, false, false, false, null);
            channel.basicPublish("", eventQueueName, null, jsonEvent.getBytes());
            logger.debug("Sent " + jsonEvent.length() + " bytes");
            logger.debug("Sent message body '" + jsonEvent + "'");

            channel.close();
            connection.close();
            
        } catch (Exception e) {
            logger.error("Error sending message", e);
        }
    }
}
