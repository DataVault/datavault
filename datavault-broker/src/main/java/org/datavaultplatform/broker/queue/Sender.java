package org.datavaultplatform.broker.queue;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sender {

    private String queueServer;
    private String queueName;
    private String queueUser;
    private String queuePassword;

    private static final Logger logger = LoggerFactory.getLogger(Sender.class);
    
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

    //private final static String QUEUE_SERVER = "debian-rabbitmq.local";
    //private final static String QUEUE_NAME = "datavaultplatform";
    //private final static String QUEUE_USER = "datavaultplatform";
    //private final static String QUEUE_PASSWORD = "datavaultplatform";

    public void send(String message) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(queueServer);
        factory.setUsername(queueUser);
        factory.setPassword(queuePassword);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        
        channel.queueDeclare(queueName, false, false, false, null);
        channel.basicPublish("", queueName, null, message.getBytes());
        logger.info("Sent '" + message + "'");
        
        channel.close();
        connection.close();
    }
}