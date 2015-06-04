package org.datavault.worker.queue;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.datavault.common.model.Deposit;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Receiver {

    private String queueServer;
    private String queueName;
    private String queueUser;
    private String queuePassword;

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
    //private final static String QUEUE_NAME = "datavault";
    //private final static String QUEUE_USER = "datavault";
    //private final static String QUEUE_PASSWORD = "datavault";
    
    public void receive() throws IOException, InterruptedException, TimeoutException {

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

            // Dispatch the job ...

            // Just testing ...

            try {
                String archiveBase = "/Users/tom/datavault/archive";
                String tempBase = "/Users/tom/datavault/temp";

                ObjectMapper mapper = new ObjectMapper();
                Deposit deposit = mapper.readValue(message, Deposit.class);
                
                java.io.File inputFile = new java.io.File(deposit.getFilePath());
                if (inputFile.exists()) {

                    // Create a new directory based on a UUID
                    java.util.UUID bagID = java.util.UUID.randomUUID();
                    java.nio.file.Path bagPath = java.nio.file.Paths.get(tempBase, bagID.toString());
                    java.io.File bagDir = bagPath.toFile();
                    bagDir.mkdir();

                    // Copy the target file to the bag directory
                    String fileName = inputFile.getName();
                    java.nio.file.Path outputPath = bagPath.resolve(fileName);
                    
                    if (inputFile.isFile()) {
                        org.apache.commons.io.FileUtils.copyFile(inputFile, outputPath.toFile());
                    } else if (inputFile.isDirectory()) {
                        org.apache.commons.io.FileUtils.copyDirectory(inputFile, outputPath.toFile());
                    }
                    
                    // Bag the directory in-place
                    org.datavault.worker.operations.Packager.createBag(bagDir);

                    // Tar the bag directory
                    String tarFileName = bagID + ".tar";
                    java.nio.file.Path tarPath = java.nio.file.Paths.get(tempBase).resolve(tarFileName);
                    java.io.File tarFile = tarPath.toFile();
                    org.datavault.worker.operations.Tar.createTar(bagDir, tarFile);

                    // Copy the resulting tar file to the archive area
                    java.nio.file.Path archivePath = java.nio.file.Paths.get(archiveBase).resolve(tarFileName);
                    org.apache.commons.io.FileUtils.copyFile(tarFile, archivePath.toFile());

                } else {
                    System.err.println("File does not exist.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Unreachable - this demo never terminates
        // channel.close();
        // connection.close();
    }
}