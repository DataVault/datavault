package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AuditChunkStatusDAOImpl implements AuditChunkStatusDAO {

    private final SessionFactory sessionFactory;

    public AuditChunkStatusDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(AuditChunkStatus auditChunkStatus) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(auditChunkStatus);
        tx.commit();
        session.close();
    }

    @Override
    public void update(AuditChunkStatus auditChunkStatus) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(auditChunkStatus);
        tx.commit();
        session.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AuditChunkStatus> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(AuditChunkStatus.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("timestamp"));
        List<AuditChunkStatus> AuditChunkStatuss = criteria.list();
        session.close();
        return AuditChunkStatuss;
    }

    @Override
    public AuditChunkStatus findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(AuditChunkStatus.class);
        criteria.add(Restrictions.eq("id", Id));
        AuditChunkStatus AuditChunkStatus = (AuditChunkStatus) criteria.uniqueResult();
        session.close();
        return AuditChunkStatus;
    }

    @Override
    public List<AuditChunkStatus> findByAudit(Audit audit){
        return findBy("audit", audit);
    }

    @Override
    public List<AuditChunkStatus> findByDeposit(Deposit deposit){
        return findBy("depositChunk_deposit", deposit);
    }

    @Override
    public List<AuditChunkStatus> findByDepositChunk(String depositChunkId){
        return findBy("depositChunk_id", depositChunkId);
    }

    public List<AuditChunkStatus> findBy(String propertyName, Object value){
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(AuditChunkStatus.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("timestamp"));
        criteria.add(Restrictions.eq(propertyName, value));
        List<AuditChunkStatus> auditChunks = criteria.list();
        session.close();
        return auditChunks;
    }

    public List<AuditChunkStatus> findBy(HashMap<String, Object> properties){
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(AuditChunkStatus.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("timestamp"));
        for(String propertyName : properties.keySet()) {
            Object value = properties.get(propertyName);
            criteria.add(Restrictions.eq(propertyName, value));
        }
        List<AuditChunkStatus> auditChunks = criteria.list();
        session.close();
        return auditChunks;
    }

    public AuditChunkStatus getLastChunkAuditTime(DepositChunk chunk){
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(AuditChunkStatus.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("depositChunk", chunk));
        criteria.addOrder(Order.desc("timestamp"));

        List<AuditChunkStatus> auditChunks = criteria.list();
        session.close();

        if(auditChunks.size() <= 0){
            return null;
        }
        return auditChunks.get(0);
    }

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int) (long) (Long) session.createCriteria(AuditChunkStatus.class).setProjection(Projections.rowCount()).uniqueResult();
    }
}