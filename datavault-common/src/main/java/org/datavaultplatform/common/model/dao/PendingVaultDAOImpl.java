package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.Vault;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PendingVaultDAOImpl implements PendingVaultDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(PendingVaultDAOImpl.class);

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(PendingVault pendingVault) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(pendingVault);
        tx.commit();
        session.close();
    }

    @Override
    public PendingVault findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(PendingVault.class);
        criteria.add(Restrictions.eq("id", Id));
        PendingVault vault = (PendingVault)criteria.uniqueResult();
        session.close();
        return vault;
    }

    @Override
    public void update(PendingVault vault) {
        Session session = null;
        Transaction tx = null;
        try {
            session = this.sessionFactory.openSession();
            tx = session.beginTransaction();
            session.update(vault);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
                System.out.println("PendingVault.update - ROLLBACK");
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
