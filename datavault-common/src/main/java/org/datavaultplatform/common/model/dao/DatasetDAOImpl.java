package org.datavaultplatform.common.model.dao;

import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import org.datavaultplatform.common.model.Dataset;
import org.springframework.stereotype.Repository;

@Repository
public class DatasetDAOImpl implements DatasetDAO {

    private final SessionFactory sessionFactory;

    public DatasetDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Dataset dataset) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(dataset);
        tx.commit();
        session.close();
    }
    
    @Override
    public void update(Dataset dataset) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(dataset);
        tx.commit();
        session.close();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<Dataset> list() {        
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Dataset.class);
        criteria.addOrder(Order.asc("sort"));
        List<Dataset> policies = criteria.list();
        session.close();
        return policies;
    }
    
    @Override
    public Dataset findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Dataset.class);
        criteria.add(Restrictions.eq("id",Id));
        Dataset dataset = (Dataset)criteria.uniqueResult();
        session.close();
        return dataset;
    }
}
