package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import jakarta.persistence.EntityManager;
import org.datavaultplatform.common.model.DataCreator;

public class DataCreatorCustomDAOImpl
    extends BaseCustomDAOImpl implements DataCreatorCustomDAO {

    public DataCreatorCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public void save(List<DataCreator> dataCreators) {
        for (DataCreator pdc : dataCreators) {
            em.persist(pdc);
        }
    }
}
