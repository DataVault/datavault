package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.DepositChunk_;

public class DepositChunkCustomDAOImpl extends BaseCustomDAOImpl implements
    DepositChunkCustomDAO {

    public DepositChunkCustomDAOImpl(EntityManager em) {
        super(em);
    }

    public List<DepositChunk> list(String sort) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DepositChunk> cr = cb.createQuery(DepositChunk.class).distinct(true);
        Root<DepositChunk> rt = cr.from(DepositChunk.class);
        // See if there is a valid sort option
        if(DepositChunk_.ID.equals(sort)) {
            cr.orderBy(cb.asc(rt.get(DepositChunk_.id)));
        } else {
            cr.orderBy(cb.asc(rt.get(DepositChunk_.chunkNum)));
        }

        List<DepositChunk> chunks = em.createQuery(cr).getResultList();
        return chunks;
    }
}
