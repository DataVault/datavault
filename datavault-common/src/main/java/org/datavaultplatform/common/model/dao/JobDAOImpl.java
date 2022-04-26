package org.datavaultplatform.common.model.dao;

import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import org.datavaultplatform.common.model.Job;
import org.springframework.stereotype.Repository;

@Repository
public class JobDAOImpl implements JobDAO {

    private final SessionFactory sessionFactory;

    public JobDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Job job) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(job);
        tx.commit();
        session.close();
    }
    
    @Override
    public void update(Job job) {
        Session session = null;
        Transaction tx = null;
        try {
            session = this.sessionFactory.openSession();
            tx = session.beginTransaction();
            session.update(job);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
                System.out.println("Job.update - ROLLBACK");
            }
            e.printStackTrace();
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<Job> list() {        
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Job.class);
        List<Job> jobs = criteria.list();
        session.close();
        return jobs;
    }
    
    @Override
    public Job findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Job.class);
        criteria.add(Restrictions.eq("id",Id));
        Job job = (Job)criteria.uniqueResult();
        session.close();
        return job;
    }

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int)(long)(Long)session.createCriteria(Job.class).setProjection(Projections.rowCount()).uniqueResult();
    }
}
