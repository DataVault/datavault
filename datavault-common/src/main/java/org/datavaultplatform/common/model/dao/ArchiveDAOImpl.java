package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Archive;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import org.springframework.stereotype.Repository;


@Repository
public class ArchiveDAOImpl implements ArchiveDAO {

    private final SessionFactory sessionFactory;

    public ArchiveDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Archive archive) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(archive);
        tx.commit();
        session.close();
    }

    @Override
    public void update(Archive archive) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(archive);
        tx.commit();
        session.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Archive> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Archive.class);
        List<Archive> archives = criteria.list();
        session.close();
        return archives;
    }

    @Override
    public Archive findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Archive.class);
        criteria.add(Restrictions.eq("id",Id));
        Archive archive = (Archive)criteria.uniqueResult();
        session.close();
        return archive;
    }

}
