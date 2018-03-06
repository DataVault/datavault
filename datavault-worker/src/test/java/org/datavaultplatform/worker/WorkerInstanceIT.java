package org.datavaultplatform.worker;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.event.retrieve.RetrieveStart;
import org.datavaultplatform.worker.operations.FileSplitter;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 * Integration Test for the Worker Instance
 * 
 * These tests are run with a worker instance running in a docker container
 */
public class WorkerInstanceIT {
    private static String workerInstanceResources;
    private static String testQueueServer = "localhost";
    private static String testQueueUser = "datavault";
    private static String testQueuePassword = "datavault";
    private static String dockerLocalStorage;
    private static String bagId = "b6fa3676-2018-4fc5-9f0e-97cfb77a250a";
    
    @BeforeClass
    public static void setUpClass() {
        String resourcesDir = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources";
        
        workerInstanceResources = resourcesDir + File.separator + "workerInstance";
        
        dockerLocalStorage = System.getProperty("user.dir") + File.separator + ".." +
                File.separator + "docker" + File.separator + "tmp" + 
                File.separator + "Users";

        BasicConfigurator.configure();
    }
    
    @Test
    public void testSimpleDeposite() {
        System.out.println("Running testSimpleDeposite()");
        
        String tarHash = "";
        Map<Integer, String> chunksHash = new HashMap<Integer, String>();
        
        // Read Json file with example of valid Deposit Task
        String message = getJsonMessage(
                workerInstanceResources + File.separator + "rabbitmq_deposit_sent.json");
                
        // Connect to rabbitmq and send message
        try{            
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(testQueueServer);
            factory.setUsername(testQueueUser);
            factory.setPassword(testQueuePassword);

            Connection connection1 = factory.newConnection();
            Channel channel1 = connection1.createChannel();
            
            Connection connection2 = factory.newConnection();
            Channel channel2 = connection2.createChannel();
            
            channel1.queueDeclare("datavault", false, false, false, null);
            channel2.queueDeclare("datavault-event", false, false, false, null);
            
            channel1.basicPublish("", "datavault", null, message.getBytes());

            System.out.println(" [x] Sent '" + message + "'");
            
            System.out.println(" [*] Waiting for messages.");
            
            QueueingConsumer qConsumer = new QueueingConsumer(channel2);
            
//            channel2.basicQos(1);
            channel2.basicConsume("datavault-event", true, qConsumer);
            
            int state = 0;
            boolean done =false;
            while (!done) {
                // Timeout after 1 minute
                QueueingConsumer.Delivery delivery = qConsumer.nextDelivery(60000);
                
                // If nothing has been received after 1 minute fail
                if(delivery == null){
                    fail("Haven't receiving any progress from the Worker for 1 minute (state "+state+")");
                }
                
                message = new String(delivery.getBody());
                
                System.out.println("State "+state);
                System.out.println("Received " + message.length() + " bytes");
                System.out.println("Received message body '" + message + "'");
                
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Event commonEvent = mapper.readValue(message, Event.class);
                    
                    Class<?> clazz = Class.forName(commonEvent.getEventClass());
                    Event concreteEvent = (Event)(mapper.readValue(message, clazz));
                    
                    switch(state){
                        case 0:
                            assertThat(concreteEvent, instanceOf(InitStates.class));
                            state++;break;
                        case 1:
                            assertThat(concreteEvent, instanceOf(Start.class));
                            state++;break;
                        case 2:
                            assertThat(concreteEvent, instanceOf(ComputedSize.class));
                            state++;break;
                        case 3:
                            if(concreteEvent instanceof UpdateProgress){
                                break;
                            }else{
                                assertThat(concreteEvent, instanceOf(TransferComplete.class));
                                state++;break;
                            }
                        case 4:
                            assertThat(concreteEvent, instanceOf(PackageComplete.class));
                            state++;break;
                        case 5:
                            assertThat(concreteEvent, instanceOf(ComputedDigest.class));
                            tarHash = ((ComputedDigest)concreteEvent).getDigest();
                            state++;break;
                        case 6:
                            assertThat(concreteEvent, instanceOf(ComputedChunks.class));
                            chunksHash = ((ComputedChunks)concreteEvent).getChunksDigest();
                            state++;break;
                        case 7:
                            if(concreteEvent instanceof UpdateProgress){
                                break;
                            }else{
                                assertThat(concreteEvent, instanceOf(Complete.class));
                                state++;
                                done=true;
                                break;
                            }
                        default:
                            fail("Unexpected state: "+state);
                    }
                } catch (Exception e) {
                    fail("Error decoding message: "+e);
                }
            }
            
            channel1.close();
            channel2.close();
            
            connection1.close();
            connection2.close();
        }catch(IOException ioe){
            fail("We should not have a IOException: "+ioe);
        }catch(TimeoutException te){
            fail("We should not have a TimeoutException: "+te);
        } catch (ShutdownSignalException sse) {
            fail("We should not have a ShutdownSignalException: "+sse);
        } catch (ConsumerCancelledException cce) {
            fail("We should not have a ConsumerCancelledException: "+cce);
        } catch (InterruptedException ie) {
            fail("We should not have a InterruptedException: "+ie);
        }

        // UNECESSARY AS RETRIEVING DATA WILL ALREADY CHECK THAT
        // Check output       
        String archiveDir = ".." + File.separator + "docker" + File.separator + "tmp" +
                File.separator + "datavault" + File.separator + "archive";

        String expectedArchiveDirPath = workerInstanceResources + File.separator + "expected-archive";
        File expectedArchiveDir = new File(expectedArchiveDirPath);
        
        for (File expectedTarFile : expectedArchiveDir.listFiles()) {
            System.out.println("Check output file: " + expectedTarFile.getName());

            // Only work if file is smaller than chunks size
            String tarFilePath = archiveDir +  File.separator + expectedTarFile.getName();
            File tarfile = new File(tarFilePath);
            
            assertTrue("The tar file doesn't exist: "+tarfile.getAbsolutePath(), tarfile.exists());
            
            try {
                FileUtils.contentEquals(tarfile, expectedTarFile);
            } catch (IOException e) {
                e.printStackTrace();
                fail("We should not have a IOException: "+e);
            }
        }

        // assertMatches("matches existing file", FileMatchers.anExistingFile(), file);
        
        System.out.println("Retrieving data...");
        
        message = getJsonMessage(
                workerInstanceResources + File.separator + "rabbitmq_retrieve_sent.json");
        
        // Fill the Json with the actual tar and chunks digest
        System.out.println("Fill json with tarhash:"+tarHash);
        message = message.replaceAll("<enter_archiveDigest_here>", tarHash);
        Set<Integer> keys = chunksHash.keySet();
        for(Integer chunkNum : keys) {
            String chunkHash = chunksHash.get(chunkNum);
            System.out.println("Fill json with chunk "+chunkNum+" hash:"+chunkHash);
            message = message.replaceAll("<enter_chunk_"+chunkNum+"_digest_here>", chunkHash);
        }
        
        Channel channel = sendMessageToRabbitMQ(message);
        listenRetrieveChannel(channel);
        
        // Compare origin files with retrieved files.
        File retrieveDataDir = new File(dockerLocalStorage + File.separator + bagId +
                File.separator + "data" + File.separator + "dir");
        
        File originDataDir = new File(dockerLocalStorage + File.separator + "dir");
        
        assertTrue("The retrieved dir doesn't exist: "+retrieveDataDir.getAbsolutePath(), retrieveDataDir.exists());
        
        File[] originFiles = originDataDir.listFiles();
        for(File originFile : originFiles) {
            File retrievedFile = new File(retrieveDataDir, originFile.getName());
            
            assertTrue("The retrieved file doesn't exist: "+retrievedFile.getAbsolutePath(), retrievedFile.exists());
            
            if (originFile.isFile()) {
                long csumOriginFile = 0;
                long csumRetrievedFile = 0;
                try {
                    csumOriginFile = FileUtils.checksum(originFile, new CRC32()).getValue();
                    csumRetrievedFile = FileUtils.checksum(retrievedFile, new CRC32()).getValue();
                } catch (IOException ioe) {
                    fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
                }
            
                assertEquals(csumOriginFile, csumRetrievedFile);
            }
        }
    }
    
    private void listenRetrieveChannel(Channel channel) {
        try {
            QueueingConsumer qConsumer = new QueueingConsumer(channel);
            
            //      channel2.basicQos(1);
            channel.basicConsume("datavault-event", true, qConsumer);
          
            int state = 0;
            boolean done =false;
            while (!done) {
                // Timeout after 1 minute
                QueueingConsumer.Delivery delivery = qConsumer.nextDelivery(60000);
              
                // If nothing has been received after 1 minute fail
                if(delivery == null){
                    fail("Haven't receiving any progress from the Worker for 1 minute (state "+state+")");
                }
              
                String message = new String(delivery.getBody());
              
                System.out.println("State "+state);
                System.out.println("Received " + message.length() + " bytes");
                System.out.println("Received message body '" + message + "'");
              
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Event commonEvent = mapper.readValue(message, Event.class);
                  
                    Class<?> clazz = Class.forName(commonEvent.getEventClass());
                    Event concreteEvent = (Event)(mapper.readValue(message, clazz));
                  
                    switch(state){
                        case 0:
                            assertThat(concreteEvent, instanceOf(InitStates.class));
                            state++;break;
                        case 1:
                            assertThat(concreteEvent, instanceOf(RetrieveStart.class));
                            state++;break;
                        case 2:
                            if(concreteEvent instanceof UpdateProgress){
                                break;
                            }else{
                                assertThat(concreteEvent, instanceOf(RetrieveComplete.class));
                                state++;done=true;break;
                            }
                        default:
                            fail("Unexpected state: "+state);
                    }
                } catch (Exception e) {
                    fail("Error decoding message: "+e);
                }
            }
            
            channel.close();
            
        } catch(IOException ioe){
            fail("We should not have a IOException: "+ioe);
        } catch(TimeoutException te){
            fail("We should not have a TimeoutException: "+te);
        } catch (ShutdownSignalException sse) {
            fail("We should not have a ShutdownSignalException: "+sse);
        } catch (ConsumerCancelledException cce) {
            fail("We should not have a ConsumerCancelledException: "+cce);
        } catch (InterruptedException ie) {
            fail("We should not have a InterruptedException: "+ie);
        }
    }
    
    private Channel sendMessageToRabbitMQ(String message){
        
        try {
            // Connect to rabbitmq and send message          
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(testQueueServer);
            factory.setUsername(testQueueUser);
            factory.setPassword(testQueuePassword);
    
            Connection connection1 = factory.newConnection();
            Channel channel1 = connection1.createChannel();
            
            Connection connection2 = factory.newConnection();
            Channel channel2 = connection2.createChannel();
                channel1.queueDeclare("datavault", false, false, false, null);
            channel2.queueDeclare("datavault-event", false, false, false, null);
            
            channel1.basicPublish("", "datavault", null, message.getBytes());
            
            System.out.println(" [x] Sent '" + message + "'");
            
            System.out.println(" [*] Waiting for messages.");
            
            channel1.close();
            connection1.close();
            
            return channel2;
        } catch (IOException ioe) {
            // TODO Auto-generated catch block
            fail("We should not have a IOException: "+ioe);
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    private String getJsonMessage(String jsonFilePath) {
        // Read Json file with example of valid Deposit Task
        String message = "";
        try {
            message = new String(
                    Files.readAllBytes(
                            Paths.get(jsonFilePath)
                            )
                    );
        } catch (IOException ioe) {
            fail("Problem reading the json file: "+ioe);
        }
        return message;
    }
    
    @After
    public void tearDown() {
        String archiveDir = ".." + File.separator + "docker" + File.separator + "tmp" +
                File.separator + "datavault" + File.separator + "archive";
        String tarFilePath = archiveDir +  File.separator + "829c8329-f995-4fc7-9e79-f96b77359f4e.tar"+
                FileSplitter.CHUNK_SEPARATOR+"1";

        File tarfile = new File(tarFilePath);
        // delete file
        tarfile.delete();
        // delete meta folder
        String metaFolderPath = ".." + File.separator + "docker" + File.separator + "tmp" +
                File.separator + "datavault" + File.separator + "meta" +
                File.separator + "829c8329-f995-4fc7-9e79-f96b77359f4e";
        File metaFolder = new File(metaFolderPath);
        metaFolder.delete();

        BasicConfigurator.resetConfiguration();
    }

}
