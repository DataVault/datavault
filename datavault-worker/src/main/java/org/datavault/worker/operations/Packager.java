package org.datavault.worker.operations;

import java.io.File;
import gov.loc.repository.bagit.*;

public class Packager {
    
    // Create a bag from an existing directory.
    public static boolean createBag(File dir) throws Exception {

        BagFactory bagFactory = new BagFactory();
        PreBag preBag = bagFactory.createPreBag(dir);
        Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, false);
        
        boolean result = false;
        try {
            result = bag.verifyValid().isSuccess();
        } finally {
            bag.close();
        }
                
        return result;
    }
}
