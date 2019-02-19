package org.datavaultplatform.worker.queue;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeoutException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.common.io.FileUtils;

import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Context.AESMode;
import org.datavaultplatform.worker.WorkerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to review messages from the message queue then process them.
 */
public class Receiver {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    private String queueServer;
    private String queueName;
    private String queueUser;
    private String queuePassword;
    private String tempDir;
    private String metaDir;
    private Boolean chunkingEnabled;
    private Long chunkingByteSize;
    private Boolean encryptionEnabled;
    private String vaultAddress;
    private String vaultToken;
    private String vaultKeyPath;
    private String vaultKeyName;
    private String vaultSslPEMPath;
    AESMode encryptionMode = AESMode.GCM;

    /**
     * Set the queue server
     * @param queueServer the queueServer value
     */
    public void setQueueServer(String queueServer) {
        this.queueServer = queueServer;
    }

    /**
     * Set the queue name
     * @param queueName the queueName value
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     * Set the queue user
     * @param queueUser the queueUser value
     */
    public void setQueueUser(String queueUser) {
        this.queueUser = queueUser;
    }

    /**
     * Set the queue password
     * @param queuePassword the queuePassword value
     */
    public void setQueuePassword(String queuePassword) {
        this.queuePassword = queuePassword;
    }

    /**
     * Set the temp dir
     * @param tempDir the tempDir value
     */
    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
    
    /**
     * Set the meta dir
     * @param metaDir the metaDir value
     */
    public void setMetaDir(String metaDir) {
        this.metaDir = metaDir;
    }

    /**
     * Define if archived tar file is chunked
     * @param chunkingEnabled true or false
     */
    public void setChunkingEnabled(Boolean chunkingEnabled) {
        this.chunkingEnabled = chunkingEnabled;
    }

    /**
     * Define the size of each chunks
     * @param chunkingByteSize true or false
     */
    public void setChunkingByteSize(String chunkingByteSize) {
        long bytes = FileUtils.parseFormattedSizeToBytes(chunkingByteSize);
        this.chunkingByteSize = bytes;
    }

    /**
     * Define if should perform encryption before archiving
     * @param encryptionEnabled true or false
     */
    public void setEncryptionEnabled(Boolean encryptionEnabled) {
        this.encryptionEnabled = encryptionEnabled;
    }

    /**
     * Define the AES mode for the encryption
     * @param encryptionMode true or false
     */
    public void setEncryptionMode(String encryptionMode) { this.encryptionMode = AESMode.valueOf(encryptionMode); }
    
    /**
     * @return Encryiption mode
     */
    public AESMode getEncryptionMode() { return encryptionMode; }
    
    /**
     * @return true if encryption is enabled, false otherwise
     */
    public Boolean isEncryptionEnabled() { return encryptionEnabled; }
    
    /**
     * @return HarshiCorp Vault server address
     */
    public String getVaultAddress() {
        return vaultAddress;
    }
    
    /**
     * Define HarshiCorp Vault server address
     */
    public void setVaultAddress(String vaultAddress) {
        this.vaultAddress = vaultAddress;
    }
    
    /**
     * @return HarshiCorp Vault Token ID
     */
    public String getVaultToken() {
        return vaultToken;
    }
    
    /**
     * Define HarshiCorp Vault Token ID
     */
    public void setVaultToken(String vaultToken) {
        this.vaultToken = vaultToken;
    }
    
    public String getVaultKeyPath() {
        return vaultKeyPath;
    }

    public void setVaultKeyPath(String vaultKeyPath) {
        this.vaultKeyPath = vaultKeyPath;
    }

    public String getVaultKeyName() {
        return vaultKeyName;
    }

    public void setVaultKeyName(String vaultKeyName) {
        this.vaultKeyName = vaultKeyName;
    }

    public String getVaultSslPEMPath() {
        return vaultSslPEMPath;
    }

    public void setVaultSslPEMPath(String sslPEMPath) {
        this.vaultSslPEMPath = sslPEMPath;
    }

    /**
     * Setup a connection to the queue then wait for messages to arrive.  When we recieve a message delivery
     * work out the type of task, check if it is a redelivery then complete the task.
     * 
     * To complete the task we set up a tmp dir then perform the tasks action (I'm not sure this is actually 
     * implemented atm afaics perform action is empty and insn't overridden)
     * 
     * Once the task is complete we clean up and acknowledge the message
     * 
     * @param events an EventSender object
     * @throws IOException if an IOException occurs
     * @throws InterruptedException if an InterruptedException occurs to a thread
     * @throws TimeoutException if a thread times out
     */
    public void receive(EventSender events) throws IOException, InterruptedException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(queueServer);
        factory.setUsername(queueUser);
        factory.setPassword(queuePassword);
        
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queueName, true, false, false, null);
        logger.info("Waiting for messages");
        
        QueueingConsumer consumer = new QueueingConsumer(channel);

        channel.basicQos(1);
        channel.basicConsume(queueName, false, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            
            // Note that the message body might contain keys/credentials
            logger.info("Received " + message.length() + " bytes");
            logger.info("Received message body '" + message + "'");

            if (message.compareToIgnoreCase("shutdown") == 0) {
                channel.basicQos(0);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                logger.info("Shutdown message received, exiting");
                System.exit(0);
            }
            
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
                
                Context context = new Context(
                        tempDirPath, metaDirPath, events,
                        chunkingEnabled, chunkingByteSize,
                        encryptionEnabled, encryptionMode, 
                        vaultAddress, vaultToken, 
                        vaultKeyPath, vaultKeyName, vaultSslPEMPath);
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