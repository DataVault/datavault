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


public class WorkerManager {

    private String numberOfWorkers;

    public void setNumberOfWorkers(String numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

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

    private List<DefaultExecuteResultHandler> startWorkers() throws IOException, InterruptedException {
        List<DefaultExecuteResultHandler> resultHandlers = new ArrayList<>();
        int i = 0;
        while (i < Integer.parseInt(numberOfWorkers)) {
            resultHandlers.add(i, startWorker());
            i++;
        }

        return resultHandlers;
    }

    private DefaultExecuteResultHandler startWorker() throws IOException, InterruptedException {
        CommandLine cmdLine = new CommandLine("java");
        cmdLine.addArgument("-cp");
        //cmdLine.addArgument("datavault-worker-1.0-SNAPSHOT-jar-with-dependencies.jar");
        cmdLine.addArgument("datavault-worker-1.0-SNAPSHOT-jar:./*");
        cmdLine.addArgument("org.datavaultplatform.worker.WorkerInstance");

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        DefaultExecutor executor = new DefaultExecutor();
        
        // If the Manager gets killed then kill the Workers.
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

        executor.execute(cmdLine, resultHandler);

        return resultHandler;
    }

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
