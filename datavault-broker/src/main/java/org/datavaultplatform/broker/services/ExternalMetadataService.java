package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.metadata.*;

import java.util.List;
import java.util.ArrayList;

/* Mock interface for an external metadata service such as a CRIS system */

public class ExternalMetadataService {

    List<Dataset> datasets = new ArrayList<>();
    
    public ExternalMetadataService() {
        generateMockData();
    }
    
    private void generateMockData() {
        for (int i = 0; i < 10; i++) {
            Dataset d = new Dataset();
            d.setID("DATASET" + i);
            d.setName("Sample dataset " + i);
            datasets.add(d);
        }
    }
    
    public List<Dataset> getDatasets() { return datasets; }
    
    public Dataset getDataset(String id) {
        for (Dataset d : datasets) {
            if (d.getID().equals(id)) {
                return d;
            }
        }
        return null;
    }
}
