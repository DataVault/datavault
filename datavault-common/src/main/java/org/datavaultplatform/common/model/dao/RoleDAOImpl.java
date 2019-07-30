package org.datavaultplatform.common.model.dao;

import com.google.common.collect.Sets;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.util.RoleUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleDAOImpl implements RoleDAO {

    private SessionFactory sessionFactory;

    public RoleDAOImpl() {}

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void storeSpecialRoles() {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        ensureIsAdminExists(session);
        ensureDataOwnerExists(session);

        tx.commit();
        session.close();
    }

    private void ensureIsAdminExists(Session session) {
        Criteria allPermissionsCriteria = session.createCriteria(PermissionModel.class);
        List<PermissionModel> allPermissions = allPermissionsCriteria.list();

        Criteria isAdminCriteria = session.createCriteria(RoleModel.class);
        isAdminCriteria.add(Restrictions.eq("name", RoleUtils.IS_ADMIN_ROLE_NAME));
        RoleModel storedIsAdmin = (RoleModel) isAdminCriteria.uniqueResult();
        if (storedIsAdmin != null) {
            Set<Permission> storedIsAdminPerms = storedIsAdmin.getPermissions().stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            Set<Permission> expectedIsAdminPerms = Sets.newHashSet(Permission.values());
            if (!storedIsAdminPerms.equals(expectedIsAdminPerms)) {
                storedIsAdmin.setPermissions(allPermissions);
                session.update(storedIsAdmin);
            }
        } else {
            RoleModel isAdmin = new RoleModel();
            isAdmin.setType(RoleType.ADMIN);
            isAdmin.setName(RoleUtils.IS_ADMIN_ROLE_NAME);
            isAdmin.setDescription(RoleUtils.IS_ADMIN_ROLE_DESCRIPTION);
            isAdmin.setPermissions(allPermissions);
            session.persist(isAdmin);
        }
    }

    private void ensureDataOwnerExists(Session session) {
        Criteria vaultPermissionsCriteria = session.createCriteria(PermissionModel.class);
        vaultPermissionsCriteria.add(Restrictions.eq("type", PermissionModel.PermissionType.VAULT));
        List<PermissionModel> vaultPermissions = vaultPermissionsCriteria.list();

        Criteria dataOwnerCriteria = session.createCriteria(RoleModel.class);
        dataOwnerCriteria.add(Restrictions.eq("name", RoleUtils.DATA_OWNER_ROLE_NAME));
        RoleModel storedDataOwner = (RoleModel) dataOwnerCriteria.uniqueResult();
        if (storedDataOwner != null) {
            Set<Permission> storedDataOwnerPermissions = storedDataOwner.getPermissions().stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            Set<Permission> expectedDataOwnerPermissions = vaultPermissions.stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            if (!storedDataOwnerPermissions.equals(expectedDataOwnerPermissions)) {
                storedDataOwner.setPermissions(vaultPermissions);
                session.update(storedDataOwner);
            }
        } else {
            RoleModel dataOwner = new RoleModel();
            dataOwner.setType(RoleType.ADMIN);
            dataOwner.setName(RoleUtils.DATA_OWNER_ROLE_NAME);
            dataOwner.setDescription(RoleUtils.DATA_OWNER_ROLE_DESCRIPTION);
            dataOwner.setPermissions(vaultPermissions);
            session.persist(dataOwner);
        }
    }

    @Override
    public void store(RoleModel role) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(role);
        tx.commit();
        session.close();
    }

    @Override
    public RoleModel find(Long id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("id", id));
        RoleModel role = (RoleModel) criteria.uniqueResult();
        session.close();
        return role;
    }

    @Override
    public RoleModel getIsAdmin() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("name", RoleUtils.IS_ADMIN_ROLE_NAME));
        RoleModel role = (RoleModel) criteria.uniqueResult();
        session.close();
        return role;
    }

    @Override
    public RoleModel getDataOwner() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("name", RoleUtils.DATA_OWNER_ROLE_NAME));
        RoleModel role = (RoleModel) criteria.uniqueResult();
        session.close();
        return role;
    }

    @Override
    public Collection<RoleModel> findAll() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        List<RoleModel> roles = criteria.list();
        session.close();
        return roles;
    }

    @Override
    public Collection<RoleModel> findAll(RoleType roleType) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("role_type", roleType.name()));
        List<RoleModel> roles = criteria.list();
        session.close();
        return roles;
    }

    @Override
    public void update(RoleModel role) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(role);
        tx.commit();
        session.close();
    }

    @Override
    public void delete(Long id) {
        RoleModel role = find(id);
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.delete(role);
        tx.commit();
        session.close();
    }
}
