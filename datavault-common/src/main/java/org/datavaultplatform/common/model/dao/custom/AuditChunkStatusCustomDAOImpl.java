package org.datavaultplatform.common.model.dao.custom;

import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.AuditChunkStatus_;
import org.datavaultplatform.common.model.DepositChunk;

public class AuditChunkStatusCustomDAOImpl extends BaseCustomDAOImpl implements
    AuditChunkStatusCustomDAO {

    public AuditChunkStatusCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<AuditChunkStatus> findByAudit(Audit audit){
        return findBy(AuditChunkStatus_.AUDIT, audit);
    }

    @Override
    public List<AuditChunkStatus> findByDepositChunk(DepositChunk depositChunk){
        return findBy(AuditChunkStatus_.DEPOSIT_CHUNK, depositChunk);
    }

    public List<AuditChunkStatus> findBy(String propertyName, Object value) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AuditChunkStatus> cr = cb.createQuery(AuditChunkStatus.class).distinct(true);
        Root<AuditChunkStatus> rt = cr.from(AuditChunkStatus.class);
        cr.orderBy(cb.asc(rt.get(AuditChunkStatus_.timestamp)));
        cr.where(cb.equal(rt.get(propertyName), value));
        List<AuditChunkStatus> auditChunks = getResults(cr);
        return auditChunks;
    }

    public List<AuditChunkStatus> findBy(HashMap<String, Object> properties) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AuditChunkStatus> cr = cb.createQuery(AuditChunkStatus.class).distinct(true);
        Root<AuditChunkStatus> rt = cr.from(AuditChunkStatus.class);
        cr.orderBy(cb.asc(rt.get(AuditChunkStatus_.timestamp)));

        Predicate[] predicates = properties.entrySet().stream().map(me ->
            cb.equal(rt.get(me.getKey()), me.getValue())
        ).toArray(Predicate[]::new);

        cr.where(predicates);
        List<AuditChunkStatus> auditChunks = getResults(cr);
        return auditChunks;
    }

    public AuditChunkStatus getLastChunkAuditTime(DepositChunk chunk) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AuditChunkStatus> cr = cb.createQuery(AuditChunkStatus.class).distinct(true);
        Root<AuditChunkStatus> rt = cr.from(AuditChunkStatus.class);
        cr.where(cb.equal(rt.get(AuditChunkStatus_.depositChunk), chunk));
        cr.orderBy(cb.desc(rt.get(AuditChunkStatus_.timestamp)));

        List<AuditChunkStatus> auditChunks = getResults(cr);

        if (auditChunks.isEmpty()) {
          return null;
        }
        return auditChunks.get(0);
    }
}