package org.datavault.worker.jobs;

import java.util.Map;
import org.datavault.common.job.Context;
import org.datavault.common.job.Job;

public class Retrieve extends Job {
    
    @Override
    public void performAction(Context context) {
        
        System.out.println("\tRetrieve job - performAction()");
        
        Map<String, String> properties = getProperties();
        String bagID = properties.get("bagId");
        String filePath = properties.get("filePath");
        
        System.out.println("\tbagID: " + bagID);
        System.out.println("\tfilePath: " + filePath);
    }
}