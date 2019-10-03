package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionDAOImpl implements PermissionDAO {

    private SessionFactory sessionFactory;

    public PermissionDAOImpl() {}

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void synchronisePermissions() {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        // First, remove any permissions that are no longer found in the Permission enum from all roles...
        String commaSeparatedPermissionIds = Arrays.stream(Permission.values())
                .map(Permission::getId)
                .collect(Collectors.joining(","));
        String deleteUnknownRolePermissionsSql = "DELETE FROM Role_permissions WHERE NOT FIND_IN_SET(permission_id, '"
                + commaSeparatedPermissionIds
                + "');";
        session.createSQLQuery(deleteUnknownRolePermissionsSql).executeUpdate();

        // ...then remove those permissions themselves...
        String deleteUnknownPermissionsSql = "DELETE FROM Permissions WHERE NOT FIND_IN_SET(id, '"
                + commaSeparatedPermissionIds
                + "');";
        session.createSQLQuery(deleteUnknownPermissionsSql).executeUpdate();

        // ...then store default entries for anything found in the enum but not the database
        Criteria permissionEnumEntries = session.createCriteria(PermissionModel.class);
        Set<String> permissionIds = Arrays.stream(Permission.values())
                .map(Permission::getId)
                .collect(Collectors.toSet());
        permissionEnumEntries.add(Restrictions.in("id", permissionIds));
        Set<Permission> persistedPermissions = ((List<PermissionModel>) permissionEnumEntries.list()).stream()
                .map(PermissionModel::getPermission)
                .collect(Collectors.toSet());
        for (Permission p : Permission.values()) {
            if (!persistedPermissions.contains(p)) {
                session.persist(PermissionModel.createDefault(p));
            }
        }

        tx.commit();
        session.close();
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

    @Override
    public List<PermissionModel> findByType(PermissionModel.PermissionType type) {
        Session session = sessionFactory.openSession();
        Criteria criteria = session.createCriteria(PermissionModel.class);
        criteria.add(Restrictions.eq("type", type));
        List<PermissionModel> permissions = criteria.list();
        session.close();
        return permissions;
    }
}
