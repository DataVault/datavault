package org.datavaultplatform.worker;

import org.datavaultplatform.worker.queue.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author ?
 * A Worker class which listens to the messge queue and processes messages as they arrive.
 */
public class WorkerInstance {

    /**
     * Get the workers name
     * @return the name as a string
     */
    public static String getWorkerName() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
    }
    
    /**
     * Set up the logger for the Workers and start listening to the message queue for something to do.
     * @param args a string array of arguments to the main class, this is unused so should be null.
     */
    public static void main(String [] args) {
        
        // Bind $DATAVAULT_HOME to a system variable for use by Log4j
        System.setProperty("datavault-home", System.getenv("DATAVAULT_HOME"));
        
        Logger logger = LoggerFactory.getLogger(WorkerInstance.class);
        logger.info("Worker starting");
        
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"datavault-worker.xml"});
        
        EventSender eventSender = context.getBean(EventSender.class);
        Receiver receiver = context.getBean(Receiver.class);
        
        // Listen to the message queue ...
        try {
            receiver.receive(eventSender);
        } catch (Exception e) {
            logger.error("Error in receive", e);
        }
    }
}
