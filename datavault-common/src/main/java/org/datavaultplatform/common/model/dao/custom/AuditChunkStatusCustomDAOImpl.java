package org.datavaultplatform.common.model.dao.custom;

import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.DepositChunk;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class AuditChunkStatusCustomDAOImpl extends BaseCustomDAOImpl implements
    AuditChunkStatusCustomDAO {

    public AuditChunkStatusCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<AuditChunkStatus> findByAudit(Audit audit){
        return findBy("audit", audit);
    }

    @Override
    public List<AuditChunkStatus> findByDepositChunk(DepositChunk depositChunk){
        return findBy("depositChunk", depositChunk);
    }

    public List<AuditChunkStatus> findBy(String propertyName, Object value){
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(AuditChunkStatus.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("timestamp"));
        criteria.add(Restrictions.eq(propertyName, value));
        List<AuditChunkStatus> auditChunks = criteria.list();
        return auditChunks;
    }

    public List<AuditChunkStatus> findBy(HashMap<String, Object> properties){
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(AuditChunkStatus.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("timestamp"));
        for(String propertyName : properties.keySet()) {
            Object value = properties.get(propertyName);
            criteria.add(Restrictions.eq(propertyName, value));
        }
        List<AuditChunkStatus> auditChunks = criteria.list();
        return auditChunks;
    }

    public AuditChunkStatus getLastChunkAuditTime(DepositChunk chunk){
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(AuditChunkStatus.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("depositChunk", chunk));
        criteria.addOrder(Order.desc("timestamp"));

        List<AuditChunkStatus> auditChunks = criteria.list();

        if(auditChunks.size() <= 0){
            return null;
        }
        return auditChunks.get(0);
    }
}