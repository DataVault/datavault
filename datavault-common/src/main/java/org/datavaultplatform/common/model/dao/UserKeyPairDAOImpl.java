package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.UserKeyPair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * User: Robin Taylor
 * Date: 29/10/2015
 * Time: 09:10
 */

public class UserKeyPairDAOImpl implements UserKeyPairDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(UserKeyPair userKeyPair) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(userKeyPair);
        tx.commit();
        session.close();
    }
}
