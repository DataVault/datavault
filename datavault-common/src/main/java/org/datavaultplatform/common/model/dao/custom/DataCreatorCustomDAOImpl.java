package org.datavaultplatform.common.model.dao.custom;

import org.datavaultplatform.common.model.DataCreator;
import org.datavaultplatform.common.model.dao.DataCreatorDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class DataCreatorCustomDAOImpl implements DataCreatorDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataCreatorCustomDAOImpl.class);

    private final SessionFactory sessionFactory;

    public DataCreatorCustomDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public void save(List<DataCreator> dataCreators) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        for (DataCreator pdc : dataCreators) {
            session.persist(pdc);
        }
        tx.commit();
        session.close();
    }

    @Override
    public DataCreator findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(DataCreator.class);
        criteria.add(Restrictions.eq("id", Id));
        DataCreator creator = (DataCreator) criteria.uniqueResult();
        session.close();
        return creator;
    }

    @Override
    public void update(DataCreator dataCreator) {
        Session session = null;
        Transaction tx = null;
        try {
            session = this.sessionFactory.openSession();
            tx = session.beginTransaction();
            session.update(dataCreator);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
                System.out.println("DataCreator.update - ROLLBACK");
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void delete(String id) {
        DataCreator creator = findById(id);
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Transaction tx = session.beginTransaction();
            session.delete(creator);
            tx.commit();
        } finally {
            if (session != null) session.close();
        }
    }
}
