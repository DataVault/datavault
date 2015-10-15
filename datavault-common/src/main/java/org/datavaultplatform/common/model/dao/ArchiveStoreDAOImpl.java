package org.datavaultplatform.common.model.dao;

import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import org.datavaultplatform.common.model.ArchiveStore;

public class ArchiveStoreDAOImpl implements ArchiveStoreDAO {

    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
     
    @Override
    public void save(ArchiveStore archiveStore) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(archiveStore);
        tx.commit();
        session.close();
    }
 
    @Override
    public void update(ArchiveStore archiveStore) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(archiveStore);
        tx.commit();
        session.close();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<ArchiveStore> list() {        
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(ArchiveStore.class);
        List<ArchiveStore> archiveStores = criteria.list();
        session.close();
        return archiveStores;
    }
    
    @Override
    public ArchiveStore findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(ArchiveStore.class);
        criteria.add(Restrictions.eq("id",Id));
        ArchiveStore archiveStore = (ArchiveStore)criteria.uniqueResult();
        session.close();
        return archiveStore;
    }
}
