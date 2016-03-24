package org.datavaultplatform.worker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.EventStream;
import org.datavaultplatform.common.model.Actor;

public class EventSender implements EventStream {

    private String queueServer;
    private String eventQueueName;
    private String queueUser;
    private String queuePassword;
    
    private int sequence = 0;
    private String workerName;

    public void setQueueServer(String queueServer) {
        this.queueServer = queueServer;
    }

    public void setEventQueueName(String eventQueueName) {
        this.eventQueueName = eventQueueName;
    }

    public void setQueueUser(String queueUser) {
        this.queueUser = queueUser;
    }

    public void setQueuePassword(String queuePassword) {
        this.queuePassword = queuePassword;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }
    
    @Override
    public void send(Event event) {
        
        // Add sequence for event ordering (where timestamp is equal)
        event.setSequence(sequence);
        sequence += 1;
        
        // Set common event properties
        event.setActorType(Actor.ActorType.WORKER);
        event.setActor(workerName);
        
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
            System.out.println(" [x] Sent '" + jsonEvent + "'");

            channel.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
