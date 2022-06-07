package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.dao.JobDAO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class JobsService {

    private final JobDAO jobDAO;

    public JobsService(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

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
        return jobDAO.findById(jobID).orElse(null);
    }

    public long count() { return jobDAO.count(); }
}

