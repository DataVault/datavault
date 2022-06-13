package org.datavaultplatform.common.model.dao.custom;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.datavaultplatform.common.model.ArchiveStore;

public class ArchiveStoreCustomDAOImpl extends BaseCustomDAOImpl implements
    ArchiveStoreCustomDAO {

    public ArchiveStoreCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public ArchiveStore findForRetrieval() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ArchiveStore> cr = cb.createQuery(ArchiveStore.class).distinct(true);
        Root<ArchiveStore> rt = cr.from(ArchiveStore.class);
        cr.where(cb.equal(rt.get("retrieveEnabled"), true));
        try {
            ArchiveStore store = em.createQuery(cr).getSingleResult();
            return store;
        } catch (NoResultException ex){
            return null;
        }
    }
}
