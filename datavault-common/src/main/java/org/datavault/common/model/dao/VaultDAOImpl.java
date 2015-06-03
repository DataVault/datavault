package org.datavault.common.model.dao;

import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

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
        List<Vault> vaultList = session.createQuery("from Vaults").list();
        session.close();
        return vaultList;
    }
    
}
