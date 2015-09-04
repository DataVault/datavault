package org.datavaultplatform.worker.queue;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.datavaultplatform.common.job.Job;
import org.datavaultplatform.common.job.Context;

public class Receiver {

    private String queueServer;
    private String queueName;
    private String queueUser;
    private String queuePassword;
    private String archiveDir;
    private String tempDir;
    private String metaDir;

    public void setQueueServer(String queueServer) {
        this.queueServer = queueServer;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setQueueUser(String queueUser) {
        this.queueUser = queueUser;
    }

    public void setQueuePassword(String queuePassword) {
        this.queuePassword = queuePassword;
    }

    public void setArchiveDir(String archiveDir) {
        this.archiveDir = archiveDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
    
    public void setMetaDir(String metaDir) {
        this.metaDir = metaDir;
    }

    public void receive(EventSender events) throws IOException, InterruptedException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(queueServer);
        factory.setUsername(queueUser);
        factory.setPassword(queuePassword);
        
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queueName, false, false, false, null);
        System.out.println(" [*] Waiting for messages.");
        
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [x] Received '" + message + "'");

            // Decode and begin the job ...
            
            try {
                ObjectMapper mapper = new ObjectMapper();
                Job commonjob = mapper.readValue(message, Job.class);
                
                Class<?> clazz = Class.forName(commonjob.getJobClass());
                Job concreteJob = (Job)(mapper.readValue(message, clazz));
                
                Context context = new Context(archiveDir, tempDir, metaDir, events);
                concreteJob.performAction(context);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Unreachable - this demo never terminates
        // channel.close();
        // connection.close();
    }
}