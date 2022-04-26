package org.datavaultplatform.common.model.dao;

import java.util.List;

import org.datavaultplatform.common.model.Client;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * User: Robin Taylor
 * Date: 10/02/2016
 * Time: 11:01
 */

@Repository
public class ClientDAOImpl implements ClientDAO {

    private static final Logger logger = LoggerFactory.getLogger(ClientDAOImpl.class);

    private final SessionFactory sessionFactory;

    public ClientDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public void save(Client client) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(client);
        tx.commit();
        session.close();
    }

    @Override
    public void update(Client client) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(client);
        tx.commit();
        session.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Client> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Client.class);
        List<Client> clients = criteria.list();
        session.close();
        return clients;
    }

    @Override
    public Client findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Client.class);
        criteria.add(Restrictions.eq("id",Id));
        Client client = (Client)criteria.uniqueResult();
        session.close();
        return client;
    }

    @Override
    public Client findByApiKey(String apiKey) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Client.class);
        criteria.add(Restrictions.eq("apiKey", apiKey));
        Client client = (Client)criteria.uniqueResult();
        session.close();
        return client;
    }

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int)(long)(Long)session.createCriteria(Client.class).setProjection(Projections.rowCount()).uniqueResult();
    }
}
