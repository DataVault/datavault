package org.datavaultplatform.common.model.dao.custom;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.util.RoleUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


public class RoleCustomDAOImpl extends BaseCustomDAOImpl implements RoleCustomDAO {
    public RoleCustomDAOImpl(EntityManager em) {
        super(em);
    }
    @Override
    public void storeSpecialRoles() {
        Session session = this.getCurrentSession();
        ensureIsAdminExists(session);
        ensureDataOwnerExists(session);
        ensureDepositorExists(session);
        ensureNominatedDataManagerExists(session);
        ensureVaultCreatorExists(session);
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
            isAdmin.setStatus("0");
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
            dataOwner.setStatus("1");
            session.persist(dataOwner);
        }
    }

    private void ensureVaultCreatorExists(Session session) {
        Criteria vaultPermissionsCriteria = session.createCriteria(PermissionModel.class);
        vaultPermissionsCriteria.add(Restrictions.eq("type", PermissionModel.PermissionType.VAULT));
        List<PermissionModel> vaultPermissions = vaultPermissionsCriteria.list();

        Criteria dataOwnerCriteria = session.createCriteria(RoleModel.class);
        dataOwnerCriteria.add(Restrictions.eq("name", RoleUtils.VAULT_CREATOR_ROLE_NAME));
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
            dataOwner.setName(RoleUtils.VAULT_CREATOR_ROLE_NAME);
            dataOwner.setDescription(RoleUtils.VAULT_CREATOR_ROLE_DESCRIPTION);
            dataOwner.setPermissions(vaultPermissions);
            dataOwner.setStatus("1");
            session.persist(dataOwner);
        }
    }

    private void ensureNominatedDataManagerExists(Session session) {
        //Criteria vaultPermissionsCriteria = session.createCriteria(PermissionModel.class);
        //vaultPermissionsCriteria.add(Restrictions.eq("type", PermissionModel.PermissionType.VAULT));
        //List<PermissionModel> vaultPermissions = vaultPermissionsCriteria.list();

        Criteria ndmCriteria = session.createCriteria(RoleModel.class);
        ndmCriteria.add(Restrictions.eq("name", RoleUtils.NOMINATED_DATA_MANAGER_ROLE_NAME));
        RoleModel storedNDM = (RoleModel) ndmCriteria.uniqueResult();
        if (storedNDM != null) {
            /*Set<Permission> storedNDMPermissions = storedNDM.getPermissions().stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            Set<Permission> expectedNDMPermissions = vaultPermissions.stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            if (!storedNDMPermissions.equals(expectedNDMPermissions)) {
                storedNDM.setPermissions(vaultPermissions);
                session.update(storedNDM);
            }*/
        } else {
            RoleModel ndm = new RoleModel();
            ndm.setType(RoleType.VAULT);
            ndm.setName(RoleUtils.NOMINATED_DATA_MANAGER_ROLE_NAME);
            ndm.setDescription(RoleUtils.NOMINATED_DATA_MANAGER_ROLE_DESCRIPTION);
            /* todo: fix default NDM permissions */
            //ndm.setPermissions(vaultPermissions);
            ndm.setStatus("1");
            session.persist(ndm);
        }
    }

    private void ensureDepositorExists(Session session) {
        //Criteria vaultPermissionsCriteria = session.createCriteria(PermissionModel.class);
        //vaultPermissionsCriteria.add(Restrictions.eq("type", PermissionModel.PermissionType.VAULT));
        //List<PermissionModel> vaultPermissions = vaultPermissionsCriteria.list();

        Criteria depCriteria = session.createCriteria(RoleModel.class);
        depCriteria.add(Restrictions.eq("name", RoleUtils.DEPOSITOR_ROLE_NAME));
        RoleModel storedDep = (RoleModel) depCriteria.uniqueResult();
        if (storedDep != null) {
            /*Set<Permission> storedNDMPermissions = storedNDM.getPermissions().stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            Set<Permission> expectedNDMPermissions = vaultPermissions.stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            if (!storedNDMPermissions.equals(expectedNDMPermissions)) {
                storedNDM.setPermissions(vaultPermissions);
                session.update(storedNDM);
            }*/
        } else {
            RoleModel dep= new RoleModel();
            dep.setType(RoleType.VAULT);
            dep.setName(RoleUtils.DEPOSITOR_ROLE_NAME);
            dep.setDescription(RoleUtils.DEPOSITOR_ROLE_DESCRIPTION);
            /* todo: fix default NDM permissions */
            //ndm.setPermissions(vaultPermissions);
            dep.setStatus("2");
            session.persist(dep);
        }
    }
    private void populateAssignedUserCount(Session session, RoleModel role) {
        if (role != null) {
            Criteria assignedUserCountCriteria = session.createCriteria(RoleAssignment.class);
            assignedUserCountCriteria.add(Restrictions.eq("role", role));
            role.setAssignedUserCount(assignedUserCountCriteria.list().size());
        }
    }

    @Override
    public RoleModel getIsAdmin() {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("name", RoleUtils.IS_ADMIN_ROLE_NAME));
        RoleModel role = (RoleModel) criteria.uniqueResult();
        populateAssignedUserCount(session, role);
        return role;
    }

    @Override
    public RoleModel getDataOwner() {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("name", RoleUtils.DATA_OWNER_ROLE_NAME));
        RoleModel role = (RoleModel) criteria.uniqueResult();
        populateAssignedUserCount(session, role);
        return role;
    }

    @Override
    public RoleModel getDepositor() {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("name", RoleUtils.DEPOSITOR_ROLE_NAME));
        RoleModel role = (RoleModel) criteria.uniqueResult();
        populateAssignedUserCount(session, role);
        return role;
    }

    @Override
    public RoleModel getVaultCreator() {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("name", RoleUtils.VAULT_CREATOR_ROLE_NAME));
        RoleModel role = (RoleModel) criteria.uniqueResult();
        populateAssignedUserCount(session, role);
        return role;
    }

    @Override
    public RoleModel getNominatedDataManager() {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("name", RoleUtils.NOMINATED_DATA_MANAGER_ROLE_NAME));
        RoleModel role = (RoleModel) criteria.uniqueResult();
        populateAssignedUserCount(session, role);
        return role;
    }

    @Override
    public List<RoleModel> listAndPopulate() {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        List<RoleModel> roles = criteria.list();
        for (RoleModel role : roles) {
            populateAssignedUserCount(session, role);
        }
        return roles;
    }

    @Override
    public Collection<RoleModel> findAll(RoleType roleType) {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("type", roleType));
        List<RoleModel> roles = criteria.list();
        for (RoleModel role : roles) {
            populateAssignedUserCount(session, role);
        }
        return roles;
    }

    @Override
    public List<RoleModel> findAllEditableRoles() {
        Session session = getCurrentSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.or(
                Restrictions.eq("type", RoleType.SCHOOL),
                Restrictions.eq("type", RoleType.VAULT)
        ));
        List<RoleModel> roles = criteria.list();
        for (RoleModel role : roles) {
            populateAssignedUserCount(session, role);
        }
        return roles;
    }
}
