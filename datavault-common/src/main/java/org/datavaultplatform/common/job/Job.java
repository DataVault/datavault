package org.datavaultplatform.common.job;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

// A generic job container

@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {

    String jobClass;
    Map<String, String> properties;

    public Job() {};
    public Job(String jobClass, Map<String, String> properties) {
        this.jobClass = jobClass;
        this.properties = properties;
    }

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    public void performAction(Context context) {}
}