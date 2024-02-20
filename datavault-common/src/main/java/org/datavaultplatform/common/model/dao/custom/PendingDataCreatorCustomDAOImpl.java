package org.datavaultplatform.common.model.dao.custom;

import jakarta.persistence.EntityManager;
import org.datavaultplatform.common.model.PendingDataCreator;

import java.util.List;

public class PendingDataCreatorCustomDAOImpl
    extends BaseCustomDAOImpl implements PendingDataCreatorCustomDAO {

    public PendingDataCreatorCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public void save(List<PendingDataCreator> pendingDataCreators) {
        for (PendingDataCreator pdc : pendingDataCreators) {
            em.persist(pdc);
        }
    }
}
