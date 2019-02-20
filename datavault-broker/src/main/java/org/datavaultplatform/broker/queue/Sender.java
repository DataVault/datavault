package org.datavaultplatform.broker.queue;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.MessageProperties;
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

        // Allow for priority messages so that a shutdown message can be prioritised if required.
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-max-priority", 2);
        
        channel.queueDeclare(queueName, true, false, false, args);
        channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
        logger.info("Sent '" + message + "'");
        
        channel.close();
        connection.close();
    }
}