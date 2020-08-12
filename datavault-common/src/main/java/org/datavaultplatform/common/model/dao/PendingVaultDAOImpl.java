package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.PendingVault;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
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
}
