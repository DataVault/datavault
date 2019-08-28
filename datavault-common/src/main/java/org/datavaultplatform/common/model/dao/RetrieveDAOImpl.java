package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<Retrieve> retrieves = new ArrayList<>();
        Optional<Criteria> criteria = retrieveCriteriaForUser(userId, session, Permission.CAN_VIEW_RETRIEVES);
        if (criteria.isPresent()) {
            criteria.get().setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            criteria.get().addOrder(Order.asc("timestamp"));
            retrieves = criteria.get().list();
        }
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
        Optional<Criteria> criteria = retrieveCriteriaForUser(userId, session, Permission.CAN_VIEW_RETRIEVES);
        return criteria.map(value -> (int) (long) (Long) value.setProjection(Projections.rowCount()).uniqueResult()).orElse(0);
    }

    @Override
    public int queueCount(String userId) {
        Session session = this.sessionFactory.openSession();
        Optional<Criteria> criteria = retrieveCriteriaForUser(userId, session, Permission.CAN_VIEW_QUEUES);
        if (criteria.isPresent()) {
            criteria.get().add(Restrictions.eq("status", Retrieve.Status.NOT_STARTED));
            criteria.get().setProjection(Projections.rowCount());
            return (int) (long) (Long) criteria.get().uniqueResult();
        }
        return 0;
    }

    @Override
    public int inProgressCount(String userId) {
        Session session = this.sessionFactory.openSession();
        Optional<Criteria> criteria = retrieveCriteriaForUser(userId, session, Permission.CAN_VIEW_IN_PROGRESS);
        if (criteria.isPresent()) {
            criteria.get().add(Restrictions.and(Restrictions.ne("status", Retrieve.Status.NOT_STARTED), Restrictions.ne("status", Retrieve.Status.COMPLETE)));
            criteria.get().setProjection(Projections.rowCount());
            return (int) (long) (Long) criteria.get().uniqueResult();
        }
        return 0;
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

    private Optional<Criteria> retrieveCriteriaForUser(String userId, Session session, Permission permission) {
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

        Criteria retrieveCriteria = session.createCriteria(Retrieve.class, "retrieve");
        if (!schoolIds.contains("*")) {
            retrieveCriteria.createAlias("retrieve.deposit", "deposit");
            retrieveCriteria.createAlias("deposit.vault", "vault");
            retrieveCriteria.createAlias("vault.group", "group");
            Set<String> permittedSchoolIds = schoolIds.stream()
                    .filter(schoolId -> !"*".equals(schoolId))
                    .collect(Collectors.toSet());
            retrieveCriteria.add(Restrictions.in("group.id", permittedSchoolIds));
        }
        return Optional.of(retrieveCriteria);
    }
}