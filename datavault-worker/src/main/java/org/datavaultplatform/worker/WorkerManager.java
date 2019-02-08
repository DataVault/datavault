package org.datavaultplatform.worker;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class handles the start up and monitoring of the required number of Worker instances.  The number of instances
 * comes from the datavault.properties file (I think).
 * 
 * Each started Worker instance is killed if the Worker Manager fails.
 * 
 * We output a warning if an instance of a Workers fails to respond which then appears to trigger the exiting of the entire
 * WorkerManager
 */
public class WorkerManager {

    private String numberOfWorkers;

    /**
     * Setter for numberOfWorkers member
     * @param numberOfWorkers A string defining the number of worker instances that will be instantiated at startup
     */
    public void setNumberOfWorkers(String numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    /**
     * Initialise and monitor the worker instances and set up the manager logging 
     * if the relevant config file exists.
     * @param args a string array of arguments to the main class, this is unused so should be null.
     * @throws IOException if an IOException occurs
     * @throws InterruptedException if an InterruptedException occurs to a thread
     */
    public static void main(String [] args) throws IOException, InterruptedException {
        
        // Bind $DATAVAULT_HOME to a system variable for use by Log4j
        if (System.getenv("DATAVAULT_HOME") == null) {
            System.err.println("DATAVAULT_HOME environment variable not set");
            System.exit(1);
        }
        File check = new File(System.getenv("DATAVAULT_HOME"));
        if (!check.exists()) {
            System.err.println("DATAVAULT_HOME environment does not exist: " + System.getenv("DATAVAULT_HOME"));
            System.exit(1);
        }
        System.setProperty("datavault-home", System.getenv("DATAVAULT_HOME"));
        
        Logger logger = LoggerFactory.getLogger(WorkerManager.class);
        logger.info("Manager starting");
        
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"datavault-worker.xml"});

        WorkerManager workerManager = context.getBean(WorkerManager.class);
        List<DefaultExecuteResultHandler> resultHandlers = workerManager.startWorkers();
        workerManager.checkResultHandlers(resultHandlers);
    }

    /**
     * Start the required number of Worker instances
     * @return A list of DefaultExecuteResultHandler objects (one for each instance)
     * @throws IOException if an IOException occurs
     * @throws InterruptedException if an InterruptedException occurs to a thread
     */
    private List<DefaultExecuteResultHandler> startWorkers() throws IOException, InterruptedException {
        List<DefaultExecuteResultHandler> resultHandlers = new ArrayList<>();
        int i = 0;
        while (i < Integer.parseInt(numberOfWorkers)) {
            resultHandlers.add(i, startWorker());
            i++;
        }

        return resultHandlers;
    }

    /**
     * Start a worker instance via the command line adding a shutdown hook so that the instance is destoryed if the 
     * manager is killed.
     * @return The instances DefaultExecuteResultHandler object
     * @throws IOException if an IOException occurs
     * @throws InterruptedException if an InterruptedException occurs to a thread
     */
    private DefaultExecuteResultHandler startWorker() throws IOException, InterruptedException {
        CommandLine cmdLine = new CommandLine("java");
        cmdLine.addArgument("-cp");
        cmdLine.addArgument("datavault-worker-1.0-SNAPSHOT-jar:./*");
        cmdLine.addArgument("org.datavaultplatform.worker.WorkerInstance");

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        DefaultExecutor executor = new DefaultExecutor();
        
        // If the Manager gets killed then kill the Workers.
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

        executor.execute(cmdLine, resultHandler);

        return resultHandler;
    }

    /**
     * Iterate over each result handler and check if any have stopped.  If none have sleep then repeat.
     * If one has stopped output a message to system out (I've just noticed this goes to system.out and not a log
     * then exit the method (which in turn appears to exit the whole app).
     * @param resultHandlers A list of DefaultExecuteResultHandler objects one for each Worker instance
     * @throws IOException if an IOException occurs
     * @throws InterruptedException if an InterruptedException occurs to a thread
     */
    private void checkResultHandlers(List<DefaultExecuteResultHandler> resultHandlers) throws IOException, InterruptedException {
        while (true) {
            for (DefaultExecuteResultHandler resultHandler : resultHandlers) {
                if (resultHandler.hasResult()) {
                    // todo : what should I usefully do in this event?
                    System.out.println("A handler has failed with exit value " + resultHandler.getExitValue());
                    return;
                }
            }

            // Sleep for a while before doing another health check
            Thread.sleep(50L);
        }

    }



}
