package org.datavault.worker.jobs;

import java.util.Map;
import org.datavault.common.job.Context;
import org.datavault.common.job.Job;

public class Withdraw extends Job {
    
    @Override
    public void performAction(Context context) {
        
        System.out.println("\tWithdraw job - performAction()");
        
        Map<String, String> properties = getProperties();
        String bagID = properties.get("bagId");
        String withdrawalPath = properties.get("withdrawalPath");
        
        System.out.println("\tbagID: " + bagID);
        System.out.println("\twithdrawalPath: " + withdrawalPath);
    }
}
