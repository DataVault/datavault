package org.datavaultplatform.worker.logger;

import org.apache.log4j.DailyRollingFileAppender;
import org.datavaultplatform.worker.WorkerInstance;

/**
 * @author ?
 *
 */
public class WorkerDailyRollingFileAppender extends DailyRollingFileAppender {
    
    /* (non-Javadoc)
     * @see org.apache.log4j.FileAppender#setFile(java.lang.String)
     */
    @Override
    public void setFile(String file) {
        super.setFile(file + "-" + WorkerInstance.getWorkerName() + ".log");
    }
}
