package org.datavaultplatform.worker.queue;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeoutException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.WorkerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Receiver {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
    
    private String queueServer;
    private String queueName;
    private String queueUser;
    private String queuePassword;
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
        logger.info("Waiting for messages");
        
        QueueingConsumer consumer = new QueueingConsumer(channel);

        channel.basicQos(1);
        channel.basicConsume(queueName, false, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            
            // Note that the message body might contain keys/credentials
            logger.debug("Received " + message.length() + " bytes");
            logger.debug("Received message body '" + message + "'");
            
            // Decode and begin the job ...
            
            try {
                ObjectMapper mapper = new ObjectMapper();
                Task commonTask = mapper.readValue(message, Task.class);
                
                Class<?> clazz = Class.forName(commonTask.getTaskClass());
                Task concreteTask = (Task)(mapper.readValue(message, clazz));

                // Is the message a redelivery?
                if (delivery.getEnvelope().isRedeliver()) {
                    concreteTask.setIsRedeliver(true);
                }

                // Set up the worker temporary directory
                Path tempDirPath = Paths.get(tempDir, WorkerInstance.getWorkerName());
                tempDirPath.toFile().mkdir();
                
                Path metaDirPath = Paths.get(metaDir);
                
                Context context = new Context(tempDirPath, metaDirPath, events);
                concreteTask.performAction(context);
                
                // Clean up the temporary directory
                FileUtils.deleteDirectory(tempDirPath.toFile());
                
            } catch (Exception e) {
                logger.error("Error decoding message", e);
            }

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
        
        // Unreachable - the receiver never terminates
        // channel.close();
        // connection.close();
    }
}