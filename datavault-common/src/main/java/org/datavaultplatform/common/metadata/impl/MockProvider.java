package org.datavaultplatform.common.metadata.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datavaultplatform.common.model.Dataset;
import org.datavaultplatform.common.metadata.Provider;

// This mock metadata provider is for testing purposes only

public class MockProvider implements Provider {

    List<Dataset> datasets = new ArrayList<>();
    Map<String, String> projectIds = new HashMap<>();
    
    public MockProvider() {
        for (int i = 1; i < 6; i++) {
            Dataset d = new Dataset();
            d.setID("MOCK-DATASET-" + i);
            d.setName("Sample dataset " + i);
            d.setContent("Mock Metadata");
            d.setVisible(true);
            datasets.add(d);
            projectIds.put(d.getID(), "MOCK-PROJECTID-" + i);
        }
    }
    
    @Override
    public List<Dataset> getDatasetsForUser(String userID) {
        return datasets;
    }
    
    @Override
    public Dataset getDataset(String id) {
        for (Dataset d : datasets) {
            if (d.getID().equals(id)) {
                return d;
            }
        }
        return null;
    }

	@Override
	public Map<String, String> getPureProjectIds() {
		return projectIds;
	}

	@Override
	public String getPureProjectId(String datasetId) {
		return projectIds.get(datasetId);
	}
}