package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.List;

public class PermissionDAOImpl implements PermissionDAO {

    private SessionFactory sessionFactory;

    public PermissionDAOImpl() {}

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public PermissionModel find(Permission permission) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(PermissionModel.class);
        criteria.add(Restrictions.eq("id", permission.name()));
        PermissionModel permissionModel = (PermissionModel) criteria.uniqueResult();
        session.close();
        return permissionModel;
    }

    @Override
    public Collection<PermissionModel> findAll() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(PermissionModel.class);
        List<PermissionModel> permissions = criteria.list();
        session.close();
        return permissions;
    }
}
