package org.datavaultplatform.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * A Worker class which listens to the message queue and process messages as they arrive.
 */
public class WorkerInstance {

    /**
     * Get the workers name
     * @return the name as a string
     */
    public static String getWorkerNameOLD() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
    }
    
    public static String getWorkerName(Environment env) {
        Assert.isTrue(env != null, "The env cannot be null");
        String workerName = env.getProperty("spring.application.name","datavault-worker");
        long pid = ProcessHandle.current().pid();
        return pid + "@" + workerName;
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
        
    }
}
