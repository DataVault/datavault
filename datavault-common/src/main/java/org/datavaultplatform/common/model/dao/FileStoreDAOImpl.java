package org.datavaultplatform.common.model.dao;

import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import org.datavaultplatform.common.model.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class FileStoreDAOImpl implements FileStoreDAO {

    private static final Logger logger = LoggerFactory.getLogger(FileStoreDAOImpl.class);

    private final SessionFactory sessionFactory;

    public FileStoreDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(FileStore fileStore) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(fileStore);
        tx.commit();
        session.close();
    }
 
    @Override
    public void update(FileStore fileStore) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(fileStore);
        tx.commit();
        session.close();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<FileStore> list() {        
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(FileStore.class);
        List<FileStore> fileStores = criteria.list();
        session.close();
        return fileStores;
    }
    
    @Override
    public FileStore findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(FileStore.class);
        criteria.add(Restrictions.eq("id",Id));
        FileStore fileStore = (FileStore)criteria.uniqueResult();
        session.close();
        return fileStore;
    }

    @Override
    public void deleteById(String Id) {
        logger.info("Deleting Filstore with id " + Id);

        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(FileStore.class);
        criteria.add(Restrictions.eq("id", Id));
        FileStore fileStore = (FileStore) criteria.uniqueResult();
        session.delete(fileStore);
        session.flush();
        session.close();
    }

}


