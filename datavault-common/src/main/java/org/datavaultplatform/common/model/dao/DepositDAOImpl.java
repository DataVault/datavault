package org.datavaultplatform.common.model.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleType;
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
        Optional<Criteria> criteriaOptional = depositCriteriaForUser(userId, session, Permission.CAN_MANAGE_DEPOSITS);
        if (!criteriaOptional.isPresent()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaOptional.get();
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
        Optional<Criteria> criteria = depositCriteriaForUser(userId, session, Permission.CAN_MANAGE_DEPOSITS);
        return criteria.map(value -> (int) (long) (Long) value.setProjection(Projections.rowCount()).uniqueResult()).orElse(0);
    }

    @Override
    public int queueCount(String userId) {
        Session session = this.sessionFactory.openSession();
        Optional<Criteria> criteria = depositCriteriaForUser(userId, session, Permission.CAN_VIEW_QUEUES);
        if (criteria.isPresent()) {
            criteria.get().add(Restrictions.eq("status", Deposit.Status.NOT_STARTED));
            criteria.get().setProjection(Projections.rowCount());
            return (int) (long) (Long) criteria.get().uniqueResult();
        }
        return 0;
    }

    @Override
    public int inProgressCount(String userId) {
        Session session = this.sessionFactory.openSession();
        Optional<Criteria> criteria = depositCriteriaForUser(userId, session, Permission.CAN_VIEW_IN_PROGRESS);
        if (criteria.isPresent()) {
            criteria.get().add(Restrictions.and(Restrictions.ne("status", Deposit.Status.NOT_STARTED), Restrictions.ne("status", Deposit.Status.COMPLETE)));
            criteria.get().setProjection(Projections.rowCount());
            return (int) (long) (Long) criteria.get().uniqueResult();
        }
        return 0;
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
        Optional<Criteria> criteriaOptional = depositCriteriaForUser(userId, session, Permission.CAN_MANAGE_DEPOSITS);
        if (!criteriaOptional.isPresent()) {
            return new ArrayList<>();
        }

        Criteria criteria = criteriaOptional.get();
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
        Optional<Criteria> depositCriteria = depositCriteriaForUser(userId, session, Permission.CAN_VIEW_VAULTS_SIZE);
        if (depositCriteria.isPresent()) {
            return (Long) depositCriteria.get().setProjection(Projections.sum("depositSize")).uniqueResult();
        }
        return 0L;
    }

    private Optional<Criteria> depositCriteriaForUser(String userId, Session session, Permission permission) {
        Criteria roleAssignmentsCriteria = session.createCriteria(RoleAssignment.class, "roleAssignment");
        roleAssignmentsCriteria.createAlias("roleAssignment.user", "user");
        roleAssignmentsCriteria.createAlias("roleAssignment.role", "role");
        roleAssignmentsCriteria.createAlias("role.permissions", "permissions");
        roleAssignmentsCriteria.add(Restrictions.eq("user.id", userId));
        roleAssignmentsCriteria.add(Restrictions.eq("permissions.id", permission.getId()));
        roleAssignmentsCriteria.add(Restrictions.or(
                Restrictions.isNotNull("roleAssignment.school"),
                Restrictions.eq("role.type", RoleType.ADMIN)));
        List<RoleAssignment> roleAssignments = roleAssignmentsCriteria.list();
        Set<String> schoolIds = roleAssignments.stream()
                .map(roleAssignment -> roleAssignment.getSchool() == null ? "*" : roleAssignment.getSchool().getID())
                .collect(Collectors.toSet());

        if (schoolIds.isEmpty()) {
            return Optional.empty();
        }

        Criteria depositCriteria = session.createCriteria(Deposit.class, "deposit");
        if (!schoolIds.contains("*")) {
            depositCriteria.createAlias("deposit.vault", "vault");
            depositCriteria.createAlias("vault.group", "group");
            Set<String> permittedSchoolIds = schoolIds.stream()
                    .filter(schoolId -> !"*".equals(schoolId))
                    .collect(Collectors.toSet());
            depositCriteria.add(Restrictions.in("group.id", permittedSchoolIds));
        }
        return Optional.of(depositCriteria);
    }
}
