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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.deposit.*;
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
    private static File testDir;
    
    private static String testQueueServer = "localhost";
    private static String testQueueUser = "datavault";
    private static String testQueuePassword = "datavault";
    
    @BeforeClass
    public static void setUpClass() {
        String resourcesDir = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources";
        
        workerInstanceResources = resourcesDir + File.separator + "workerInstance";
    }
    
    @Test
    public void testSimpleDeposite() {        
        System.out.println("Running testSimpleDeposite()");
        
        String jsonFilePath = workerInstanceResources + File.separator + "rabbitmq_deposit_sent.json";
        
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
                QueueingConsumer.Delivery delivery = qConsumer.nextDelivery();
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
                            state++;break;
                        case 6:
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
        
        // Check output        
        String archiveDir = ".." + File.separator + "docker" + File.separator + "tmp" +
                File.separator + "datavault" + File.separator + "archive";

        String expectedTarFilePath = workerInstanceResources + File.separator + "829c8329-f995-4fc7-9e79-f96b77359f4e.tar";
        String tarFilePath = archiveDir +  File.separator + "829c8329-f995-4fc7-9e79-f96b77359f4e.tar";

        File tarfile = new File(tarFilePath);
        File expectedTarfile = new File(expectedTarFilePath);
        
        System.out.println("Check output file: " + tarFilePath);
        
        try {
            assertTrue("The tar file doesn't exist: "+tarfile.getAbsolutePath(), tarfile.exists());
            assertTrue("The expected file doesn't exist: "+expectedTarfile.getAbsolutePath(), expectedTarfile.exists());
            
            assertTarEquals(tarFilePath, expectedTarFilePath);
        } catch (NoSuchAlgorithmException nsae) {
            fail("We should not have a NoSuchAlgorithmException: "+nsae);
        } catch (IOException e) {
            fail("We should not have a IOException: "+e);
        }

        // assertMatches("matches existing file", FileMatchers.anExistingFile(), file);
        
        System.out.println("Done");
    }
    
    /**
     * @param archive1
     * @param archive2
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static final void assertTarEquals(String archive1, String archive2) throws IOException, NoSuchAlgorithmException {
        // Get Member Hash
        HashMap<String, TarArchiveEntry> files1 = getMembers(archive1);
        HashMap<String, TarArchiveEntry> files2 = getMembers(archive2);

        // Compare Files
        assertMembersEqual(archive1, files1, archive2, files2);
    }
    
    /**
     * @param archive
     * @return
     * @throws IOException
     */
    private static final HashMap<String,TarArchiveEntry> getMembers(String archive) throws IOException {
        TarArchiveInputStream input = new TarArchiveInputStream(
                new BufferedInputStream(
                new FileInputStream(archive)));

        TarArchiveEntry entry;
        HashMap<String, TarArchiveEntry> map = new HashMap<String, TarArchiveEntry>();
        while ((entry = input.getNextTarEntry()) != null) {
            map.put(entry.getName(), entry);
        }
        input.close();
        return map;
    }
    
    /**
     * @param tar1
     * @param files1
     * @param tar2
     * @param files2
     * @throws IOException
     */
    private static final void assertMembersEqual(String tar1, HashMap<String, TarArchiveEntry> files1, 
                                                 String tar2, HashMap<String, TarArchiveEntry> files2) throws IOException {
        if (files1.size() != files2.size()) {
            fail("Different Sizes, expected " + Integer.toString(files1.size()) + " found " + Integer.toString(files2.size()));
        }

        for (String key : files1.keySet()) {
            if (!files2.containsKey(key)) {
                fail("Expected file not in target " + key);
            }else{
                TarArchiveEntry entry1 = files1.get(key);
                TarArchiveEntry entry2 = files2.get(key);
                
                assertEquals(entry1.getSize(), entry2.getSize());
                assertEquals(entry1.getName(), entry2.getName());
                assertEquals(entry1.getMode(), entry2.getMode());
                assertEquals(entry1.getMode(), entry2.getMode());
                
                // TODO: Add more for links, directories,etc...
            }
        }
    }

}
