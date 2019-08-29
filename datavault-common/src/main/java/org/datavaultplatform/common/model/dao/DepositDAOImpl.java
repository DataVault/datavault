package org.datavaultplatform.common.model.dao;

import java.util.ArrayList;
import java.util.List;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.util.DaoUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import org.datavaultplatform.common.model.Deposit;

public class DepositDAOImpl implements DepositDAO {

    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
     
    @Override
    public void save(Deposit deposit) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(deposit);
        tx.commit();
        session.close();
    }
    
    @Override
    public void update(Deposit deposit) {
        Session session = null;
        Transaction tx = null;
        try {
            session = this.sessionFactory.openSession();
            tx = session.beginTransaction();
            session.update(deposit);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
                System.out.println("Deposit.update - ROLLBACK");
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<Deposit> list(String sort, String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_MANAGE_DEPOSITS);
        if (criteriaBuilder.hasNoAccess()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaBuilder.build();
        // See if there is a valid sort option
        if ("id".equals(sort)) {
            criteria.addOrder(Order.asc("id"));
        } else if ("note".equals(sort)) {
            criteria.addOrder(Order.asc("note"));
        } else if ("status".equals(sort)) {
            criteria.addOrder(Order.asc("status"));
        } else if ("filePath".equals(sort)) {
            criteria.addOrder(Order.asc("filePath"));
        } else if ("depositSize".equals(sort)) {
            criteria.addOrder(Order.asc("depositSize"));
        } else {
            criteria.addOrder(Order.asc("creationTime"));
        }

        List<Deposit> deposits = criteria.list();
        session.close();
        return deposits;
    }
    
    @Override
    public Deposit findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        criteria.add(Restrictions.eq("id",Id));
        Deposit deposit = (Deposit)criteria.uniqueResult();
        session.close();
        return deposit;
    }

    @Override
    public int count(String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_MANAGE_DEPOSITS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        return (int) (long) (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public int queueCount(String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_VIEW_QUEUES);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.add(Restrictions.eq("status", Deposit.Status.NOT_STARTED));
        criteria.setProjection(Projections.rowCount());
        return (int) (long) (Long) criteria.uniqueResult();
    }

    @Override
    public int inProgressCount(String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_VIEW_IN_PROGRESS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.add(Restrictions.and(Restrictions.ne("status", Deposit.Status.NOT_STARTED), Restrictions.ne("status", Deposit.Status.COMPLETE)));
        criteria.setProjection(Projections.rowCount());
        return (int) (long) (Long) criteria.uniqueResult();
    }

    @Override
    public List<Deposit> inProgress() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        criteria.add(Restrictions.and(Restrictions.ne("status", Deposit.Status.NOT_STARTED), Restrictions.ne("status", Deposit.Status.COMPLETE)));
        List<Deposit> deposits = criteria.list();
        session.close();
        return deposits;
    }

    @Override
    public List<Deposit> completed() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        criteria.add(Restrictions.eq("status", Deposit.Status.COMPLETE));
        List<Deposit> deposits = criteria.list();
        session.close();
        return deposits;
    }

    @Override
    public List<Deposit> search(String query, String sort, String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_MANAGE_DEPOSITS);
        if (criteriaBuilder.hasNoAccess()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%"), Restrictions.ilike("note", "%" + query + "%"), Restrictions.ilike("filePath", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        // See if there is a valid sort option
        if ("id".equals(sort)) {
            criteria.addOrder(Order.asc("id"));
        } else if ("note".equals(sort)) {
            criteria.addOrder(Order.asc("note"));
        } else if ("status".equals(sort)) {
            criteria.addOrder(Order.asc("status"));
        } else if ("filePath".equals(sort)) {
            criteria.addOrder(Order.asc("filePath"));
        } else if ("depositSize".equals(sort)) {
            criteria.addOrder(Order.asc("depositSize"));
        } else {
            criteria.addOrder(Order.asc("creationTime"));
        }

        List<Deposit> deposits = criteria.list();
        session.close();
        return deposits;
    }

    @Override
    public Long size(String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_VIEW_VAULTS_SIZE);
        if (criteriaBuilder.hasNoAccess()) {
            return 0L;
        }
        Criteria criteria = criteriaBuilder.build();
        return (Long) criteria.setProjection(Projections.sum("depositSize")).uniqueResult();
    }

    private SchoolPermissionCriteriaBuilder createDepositCriteriaBuilder(String userId, Session session, Permission permission) {
        return new SchoolPermissionCriteriaBuilder()
                .setCriteriaType(Deposit.class)
                .setCriteriaName("deposit")
                .setSession(session)
                .setTypeToSchoolAliasGenerator(criteria ->
                        criteria.createAlias("deposit.vault", "vault")
                                .createAlias("vault.group", "group"))
                .setSchoolIds(DaoUtils.getPermittedSchoolIds(session, userId, permission));
    }
}
