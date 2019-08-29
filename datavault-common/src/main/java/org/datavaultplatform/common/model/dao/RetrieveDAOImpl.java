package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.util.DaoUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

public class RetrieveDAOImpl implements RetrieveDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Retrieve retrieve) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(retrieve);
        tx.commit();
        session.close();
    }

    @Override
    public void update(Retrieve retrieve) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(retrieve);
        tx.commit();
        session.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Retrieve> list(String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createRetrieveCriteriaBuilder(userId, session, Permission.CAN_VIEW_RETRIEVES);
        if (criteriaBuilder.hasNoAccess()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("timestamp"));
        List<Retrieve> retrieves = criteria.list();
        session.close();
        return retrieves;
    }

    @Override
    public Retrieve findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Retrieve.class);
        criteria.add(Restrictions.eq("id", Id));
        Retrieve retrieve = (Retrieve) criteria.uniqueResult();
        session.close();
        return retrieve;
    }

    @Override
    public int count(String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createRetrieveCriteriaBuilder(userId, session, Permission.CAN_VIEW_RETRIEVES);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        return (int) (long) (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public int queueCount(String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createRetrieveCriteriaBuilder(userId, session, Permission.CAN_VIEW_QUEUES);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.add(Restrictions.eq("status", Retrieve.Status.NOT_STARTED));
        criteria.setProjection(Projections.rowCount());
        return (int) (long) (Long) criteria.uniqueResult();
    }

    @Override
    public int inProgressCount(String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createRetrieveCriteriaBuilder(userId, session, Permission.CAN_VIEW_IN_PROGRESS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.add(Restrictions.and(Restrictions.ne("status", Retrieve.Status.NOT_STARTED), Restrictions.ne("status", Retrieve.Status.COMPLETE)));
        criteria.setProjection(Projections.rowCount());
        return (int) (long) (Long) criteria.uniqueResult();
    }

    @Override
    public List<Retrieve> inProgress() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Retrieve.class);
        criteria.add(Restrictions.and(Restrictions.ne("status", Retrieve.Status.NOT_STARTED), Restrictions.ne("status", Retrieve.Status.COMPLETE)));
        criteria.addOrder(Order.asc("timestamp"));
        List<Retrieve> retrieves = criteria.list();
        session.close();
        return retrieves;
    }

    private SchoolPermissionCriteriaBuilder createRetrieveCriteriaBuilder(String userId, Session session, Permission permission) {
        return new SchoolPermissionCriteriaBuilder()
                .setSession(session)
                .setCriteriaType(Retrieve.class)
                .setCriteriaName("retrieve")
                .setTypeToSchoolAliasGenerator(criteria ->
                        criteria.createAlias("retrieve.deposit", "deposit")
                                .createAlias("deposit.vault", "vault")
                                .createAlias("vault.group", "group"))
                .setSchoolIds(DaoUtils.getPermittedSchoolIds(session, userId, permission));
    }
}
