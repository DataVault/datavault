package org.datavault.worker;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class WorkerManager {

    private String numberOfWorkers;

    public void setNumberOfWorkers(String numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }


    public static void main(String [] args) throws IOException, InterruptedException {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"config.xml"});

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
        cmdLine.addArgument("datavault-worker-1.0-SNAPSHOT-jar-with-dependencies.jar");
        cmdLine.addArgument("org.datavault.worker.WorkerInstance");

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(1);

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
