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
        SchoolPermissionCriteriaBuilder criteriaBuilder = createGroupCriteriaBuilder(userId, session, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
        if (criteriaBuilder.hasNoAccess()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaBuilder.build();
        List<Group> groups = criteria.addOrder(Order.asc("name")).list();
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
        SchoolPermissionCriteriaBuilder criteriaBuilder = createGroupCriteriaBuilder(userId, session, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        return (int) (long) (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }

    private SchoolPermissionCriteriaBuilder createGroupCriteriaBuilder(String userId, Session session, Permission permission) {
        return new SchoolPermissionCriteriaBuilder()
                .setCriteriaType(Group.class)
                .setCriteriaName("group")
                .setSession(session)
                .setSchoolIds(DaoUtils.getPermittedSchoolIds(session, userId, permission));
    }
}
