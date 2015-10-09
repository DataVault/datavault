package org.datavaultplatform.common.model.dao;

import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;

import org.datavaultplatform.common.model.Vault;

public class VaultDAOImpl implements VaultDAO {

    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
     
    @Override
    public void save(Vault vault) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(vault);
        tx.commit();
        session.close();
    }
 
    @Override
    public void update(Vault vault) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(vault);
        tx.commit();
        session.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Vault> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Vault.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("creationTime"));
        List<Vault> vaults = criteria.list();
        session.close();
        return vaults;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Vault> list(String sort) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Vault.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        // See if there is a valid sort option
        if ("id".equals(sort)) {
            criteria.addOrder(Order.asc("id"));
        } else if ("name".equals(sort)) {
            criteria.addOrder(Order.asc("name"));
        } else if ("description".equals(sort)) {
            criteria.addOrder(Order.asc("description"));
        } else if ("vaultSize".equals(sort)) {
            criteria.addOrder(Order.asc("vaultSize"));
        } else if ("user".equals(sort)) {
            criteria.addOrder(Order.asc("user"));
        } else if ("policy".equals(sort)) {
            criteria.addOrder(Order.asc("policy"));
        } else {
            criteria.addOrder(Order.asc("creationTime"));
        }

        List<Vault> vaults = criteria.list();
        session.close();
        return vaults;
    }

    @Override
    public Vault findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Vault.class);
        criteria.add(Restrictions.eq("id", Id));
        Vault vault = (Vault)criteria.uniqueResult();
        session.close();
        return vault;
    }

    @Override
    public List<Vault> search(String query, String sort) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Vault.class);
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%"), Restrictions.ilike("name", "%" + query + "%"), Restrictions.ilike("description", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        // See if there is a valid sort option
        if ("id".equals(sort)) {
            criteria.addOrder(Order.asc("id"));
        } else if ("name".equals(sort)) {
            criteria.addOrder(Order.asc("name"));
        } else if ("description".equals(sort)) {
            criteria.addOrder(Order.asc("description"));
        } else if ("vaultSize".equals(sort)) {
            criteria.addOrder(Order.asc("vaultSize"));
        } else if ("user".equals(sort)) {
            criteria.addOrder(Order.asc("user"));
        } else if ("policy".equals(sort)) {
            criteria.addOrder(Order.asc("policy"));
        } else {
            criteria.addOrder(Order.asc("creationTime"));
        }

        List<Vault> vaults = criteria.list();
        session.close();
        return vaults;
    }

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int)(long)(Long)session.createCriteria(Vault.class).setProjection(Projections.rowCount()).uniqueResult();
    }
}
