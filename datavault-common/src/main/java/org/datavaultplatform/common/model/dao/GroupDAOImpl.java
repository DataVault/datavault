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

public class GroupDAOImpl implements GroupDAO {

    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
     
    @Override
    public void save(Group group) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(group);
        tx.commit();
        session.close();
    }

    @Override
    public void update(Group group) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(group);
        tx.commit();
        session.close();
    }

    @Override
    public void delete(Group group) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.delete(group);
        tx.commit();
        session.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Group> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Group.class);
        criteria.addOrder(Order.asc("name"));
        List<Group> groups = criteria.list();
        session.close();
        return groups;
    }

    @Override
    public List<Group> list(String userId) {
        Session session = this.sessionFactory.openSession();
        List<Group> groups = new ArrayList<>();
        Optional<Criteria> criteria = groupCriteriaForUser(userId, session, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
        if (criteria.isPresent()) {
            groups = criteria.get().addOrder(Order.asc("name")).list();
        }
        session.close();
        return groups;
    }

    @Override
    public Group findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Group.class);
        criteria.add(Restrictions.eq("id", Id));
        Group group = (Group)criteria.uniqueResult();
        session.close();
        return group;
    }

    @Override
    public int count(String userId) {
        Session session = this.sessionFactory.openSession();
        Optional<Criteria> criteria = groupCriteriaForUser(userId, session, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
        return criteria.map(value -> (int) (long) (Long) value.setProjection(Projections.rowCount()).uniqueResult()).orElse(0);
    }

    private Optional<Criteria> groupCriteriaForUser(String userId, Session session, Permission permission) {
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
        Set<String> groupIds = roleAssignments.stream()
                .map(roleAssignment -> roleAssignment.getSchool() == null ? "*" : roleAssignment.getSchool().getID())
                .collect(Collectors.toSet());

        if (groupIds.isEmpty()) {
            return Optional.empty();
        }

        Criteria retrieveCriteria = session.createCriteria(Group.class, "group");
        if (!groupIds.contains("*")) {
            Set<String> permittedSchoolIds = groupIds.stream()
                    .filter(schoolId -> !"*".equals(schoolId))
                    .collect(Collectors.toSet());
            retrieveCriteria.add(Restrictions.in("group.id", permittedSchoolIds));
        }
        return Optional.of(retrieveCriteria);
    }
}
