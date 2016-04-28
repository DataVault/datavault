package org.datavaultplatform.worker.logger;

import org.apache.log4j.DailyRollingFileAppender;
import org.datavaultplatform.worker.WorkerInstance;

public class WorkerDailyRollingFileAppender extends DailyRollingFileAppender {
    
    @Override
    public void setFile(String file) {
        super.setFile(file + "-" + WorkerInstance.getWorkerName() + ".log");
    }
}
