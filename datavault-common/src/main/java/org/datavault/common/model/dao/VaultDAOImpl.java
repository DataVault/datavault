package org.datavault.common.model.dao;

import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import org.datavault.common.model.Vault;

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
 
    @SuppressWarnings("unchecked")
    @Override
    public List<Vault> list() {        
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Vault.class);
        List<Vault> vaults = criteria.list();
        session.close();
        return vaults;
    }
    
    @Override
    public Vault findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Vault.class);
        criteria.add(Restrictions.eq("id",Id));
        Vault vault = (Vault)criteria.uniqueResult();
        session.close();
        return vault;
    }
}
