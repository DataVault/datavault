package org.datavaultplatform.common.metadata.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datavaultplatform.common.model.Dataset;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.datavaultplatform.common.metadata.Provider;

// This mock metadata provider is for testing purposes only

public class TestPureProvider implements Provider {

    List<Dataset> datasets = new ArrayList<>();
    
    public TestPureProvider() {
    }
    
    
    private Map<String, String> processPersonFlatFile() throws IOException {
    	Map<String, String> retVal = new HashMap<String, String>();
    	File personFile = new File("/tmp/person.flat");
    	LineIterator personIt = null;
		try {
			 personIt = FileUtils.lineIterator(personFile, "UTF-8");
			while ( personIt.hasNext()) {
	    	    String line =  personIt.nextLine();
	    	    // do something with line
	    	    String[] splitLine = line.split("\t");
	    	    if (splitLine.length == 2) {
	    	    	retVal.put(splitLine[1], splitLine[0]);
	    	    }
			}
		} catch (IOException e) {
			throw e;
		} finally {
			 personIt.close();
    	}
		
		return retVal;
    }
    
    private Map<String, Map<String, List<String>>> processDatasetDisplayFlatFile() throws IOException {
    	Map<String, Map<String, List<String>>> retVal = new HashMap<String, Map<String, List<String>>>();
    	File dsDisplayFile = new File("/tmp/datasetDisplay.flat");
    	LineIterator displayIt = null;
		try {
			displayIt = FileUtils.lineIterator(dsDisplayFile, "UTF-8");
			while (displayIt.hasNext()) {
	    	    String line = displayIt.nextLine();
	    	    // do something with line
	    	    String[] splitLine = line.split("\t");
	    	    if (splitLine.length == 4) {
	    	    	List<String> info = new ArrayList<String>();
	    	    	info.add(splitLine[2]);
	    	    	info.add(splitLine[3]);
	    	    	//see if we have seen a ds for this user already if so use that
	    	    	Map<String, List<String>> dsMap = retVal.get(splitLine[0]);
	    	    	if (dsMap == null) {
	    	    		// if not make a new hash
	    	    		dsMap = new HashMap<String, List<String>>();
	    	    	}
	    	    	dsMap.put(splitLine[1], info);
	    	    	retVal.put(splitLine[0], dsMap);
	    	    }
			}
		} catch (IOException e) {
			throw e;
		} finally {
			displayIt.close();
    	}
		
		return retVal;
    }
    
    private Map<String, String> processDatasetFullFlatFile() throws IOException {
    	Map<String, String> retVal = new HashMap<String, String>();
        File dsFullFile  = new File("/tmp/datasetFull.flat");
        LineIterator fullIt = null;
		try {
			fullIt = FileUtils.lineIterator(dsFullFile, "UTF-8");
			while (fullIt.hasNext()) {
	    	    String line = fullIt.nextLine();
	    	    // do something with line
	    	    String[] splitLine = line.split("\t");
	    	    if (splitLine.length == 2) {
	    	    	retVal.put(splitLine[0], splitLine[1]);
	    	    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fullIt.close();
    	}
		
		return retVal;
    }
    
    @Override
    public List<Dataset> getDatasetsForUser(String userID) {
    	List<Dataset> retVal = new ArrayList<Dataset>();
    	System.out.println("Getting Datasets from the test pure flat file for " +userID);
    	System.out.println("Employee ID is hardcoded to 123363 for now we need to add the LDAP service to get the real employee ID");
    	// userID is the UUN
    	// get the employee id from LDAP service
    	
    	
        Map<String, String> personHash;
		try {
			// key employee id - value person uuid
			personHash = this.processPersonFlatFile();
			 // key person uuid - dataset uuod - value list of ds uuid, title, workflow
	        Map<String, Map<String, List<String>>> displayHash = this.processDatasetDisplayFlatFile();
	        // key ds uuid - value the pure record metadata as xml 
	        Map<String, String> fullHash = this.processDatasetFullFlatFile();
	    	
	    	// get the person uuid for the employee id
	    	String personUUID = personHash.get("123363");
	    	if (personUUID != null) {
		    	// get the datasets for this users
		    	Map<String, List<String>> dataSetsMap = displayHash.get(personUUID);
		    	// foreach dataset
		    	for (String dsUUID: dataSetsMap.keySet()) {
		    	// get the full meta data to make the ds content
		    		List<String> info = dataSetsMap.get(dsUUID);
		    		if (info.size() == 2) {
			    		Dataset ds = new Dataset();
			    		ds.setID(dsUUID);
			    		ds.setName(info.get(0) + "(" + info.get(1) + ")");
			    		String xml = fullHash.get(dsUUID);
			    		ds.setContent(xml);
			    		retVal.add(ds);
		    		}
		    	}
	    	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        return retVal;
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
}
