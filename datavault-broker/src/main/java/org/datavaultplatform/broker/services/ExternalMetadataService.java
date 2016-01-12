package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.metadata.*;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/* Mock interface for an external metadata service such as a CRIS system */

public class ExternalMetadataService {

    List<Dataset> datasets = new ArrayList<>();
    
    public List<Dataset> getDatasets() { return datasets; }
    public Dataset getDataset(String id) { Dataset d = new Dataset(); d.setID(id); return d; }
}
