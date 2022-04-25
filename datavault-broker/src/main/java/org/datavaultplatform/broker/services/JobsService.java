package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.dao.JobDAO;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
@Service
public class JobsService {

    private JobDAO jobDAO;
    
    public List<Job> getJobs() {
        return jobDAO.list();
    }
    
    public void addJob(Deposit deposit, Job job) {
        
        Date d = new Date();
        job.setTimestamp(d);
        
        job.setDeposit(deposit);
        
        jobDAO.save(job);
    }
    
    public void updateJob(Job job) {
        jobDAO.update(job);
    }
    
    public Job getJob(String jobID) {
        return jobDAO.findById(jobID);
    }
    
    public void setJobDAO(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

    public int count() { return jobDAO.count(); }
}

