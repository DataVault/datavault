package org.datavaultplatform.worker.logger;

import org.apache.log4j.DailyRollingFileAppender;

public class WorkerDailyRollingFileAppender extends DailyRollingFileAppender {
    
    @Override
    public void setFile(String file) {
        String workerName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        super.setFile(file + "-" + workerName + ".log");
    }
}
