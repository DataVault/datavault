package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import javax.persistence.EntityManager;
import org.datavaultplatform.common.model.DepositChunk;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class DepositChunkCustomDAOImpl extends BaseCustomDAOImpl implements
    DepositChunkCustomDAO {

    public DepositChunkCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @SuppressWarnings("unchecked")
    public List<DepositChunk> list(String sort) {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(DepositChunk.class);
        // See if there is a valid sort option
        if ("id".equals(sort)) {
            criteria.addOrder(Order.asc("id"));
        } else {
            criteria.addOrder(Order.asc("chunkNum"));
        }

        List<DepositChunk> chunks = criteria.list();
        return chunks;
    }
}
