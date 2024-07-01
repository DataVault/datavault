package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.DepositChunk_;

public class DepositChunkCustomDAOImpl extends BaseCustomDAOImpl implements
    DepositChunkCustomDAO {

    public DepositChunkCustomDAOImpl(EntityManager em) {
        super(em);
    }

    public List<DepositChunk> list(String sort) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DepositChunk> cq = cb.createQuery(DepositChunk.class).distinct(true);
        Root<DepositChunk> rt = cq.from(DepositChunk.class);
        // See if there is a valid sort option
        if(DepositChunk_.ID.equals(sort)) {
            cq.orderBy(cb.asc(rt.get(DepositChunk_.id)));
        } else {
            cq.orderBy(cb.asc(rt.get(DepositChunk_.chunkNum)));
        }

        List<DepositChunk> chunks = getResults(cq);
        return chunks;
    }
}
