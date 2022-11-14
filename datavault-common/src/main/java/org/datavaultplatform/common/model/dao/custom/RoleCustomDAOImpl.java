package org.datavaultplatform.common.model.dao.custom;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.PermissionModel.PermissionType;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleAssignment_;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleModel_;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.util.RoleUtils;


@Slf4j
public class RoleCustomDAOImpl extends BaseCustomDAOImpl implements RoleCustomDAO {
    public RoleCustomDAOImpl(EntityManager em) {
        super(em);
    }
    @Override
    public void storeSpecialRoles() {
        ensureIsAdminExists();
        ensureDataOwnerExists();
        ensureDepositorExists();
        ensureNominatedDataManagerExists();
        ensureVaultCreatorExists();
    }

    private void ensureIsAdminExists() {
        List<PermissionModel> allPermissions = getAllPermissionModels();

        RoleModel storedIsAdmin = getRoleModelByName(RoleUtils.IS_ADMIN_ROLE_NAME);
        if (storedIsAdmin != null) {
            Set<Permission> storedIsAdminPerms = storedIsAdmin.getPermissions().stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            Set<Permission> expectedIsAdminPerms = Sets.newHashSet(Permission.values());
            if (!storedIsAdminPerms.equals(expectedIsAdminPerms)) {
                storedIsAdmin.setPermissions(allPermissions);
                em.merge(storedIsAdmin);
            }
        } else {
            RoleModel isAdmin = new RoleModel();
            isAdmin.setType(RoleType.ADMIN);
            isAdmin.setName(RoleUtils.IS_ADMIN_ROLE_NAME);
            isAdmin.setDescription(RoleUtils.IS_ADMIN_ROLE_DESCRIPTION);
            isAdmin.setStatus("0");
            isAdmin.setPermissions(allPermissions);
            em.persist(isAdmin);
        }
    }

    private void ensureDataOwnerExists() {
        List<PermissionModel> vaultPermissions = getPermissionModelsByType(PermissionType.VAULT);

        RoleModel storedDataOwner = getRoleModelByName(RoleUtils.DATA_OWNER_ROLE_NAME);
        if (storedDataOwner != null) {
            Set<Permission> storedDataOwnerPermissions = storedDataOwner.getPermissions().stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            Set<Permission> expectedDataOwnerPermissions = vaultPermissions.stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            if (!storedDataOwnerPermissions.equals(expectedDataOwnerPermissions)) {
                storedDataOwner.setPermissions(vaultPermissions);
                em.merge(storedDataOwner);
            }
        } else {
            RoleModel dataOwner = new RoleModel();
            dataOwner.setType(RoleType.ADMIN);
            dataOwner.setName(RoleUtils.DATA_OWNER_ROLE_NAME);
            dataOwner.setDescription(RoleUtils.DATA_OWNER_ROLE_DESCRIPTION);
            dataOwner.setPermissions(vaultPermissions);
            dataOwner.setStatus("1");
            em.persist(dataOwner);
        }
    }

    private void ensureVaultCreatorExists() {
        List<PermissionModel> vaultPermissions = getPermissionModelsByType(PermissionModel.PermissionType.VAULT);

        RoleModel storedVaultCreator = getRoleModelByName(RoleUtils.VAULT_CREATOR_ROLE_NAME);
        if (storedVaultCreator != null) {
            Set<Permission> storedDataOwnerPermissions = storedVaultCreator.getPermissions().stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            Set<Permission> expectedDataOwnerPermissions = vaultPermissions.stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
            if (!storedDataOwnerPermissions.equals(expectedDataOwnerPermissions)) {
                storedVaultCreator.setPermissions(vaultPermissions);
                em.merge(storedVaultCreator);
            }
        } else {
            RoleModel dataOwner = new RoleModel();
            dataOwner.setType(RoleType.ADMIN);
            dataOwner.setName(RoleUtils.VAULT_CREATOR_ROLE_NAME);
            dataOwner.setDescription(RoleUtils.VAULT_CREATOR_ROLE_DESCRIPTION);
            dataOwner.setPermissions(vaultPermissions);
            dataOwner.setStatus("1");
            em.persist(dataOwner);
        }
    }

    private void ensureNominatedDataManagerExists() {
        //Criteria vaultPermissionsCriteria = session.createCriteria(PermissionModel.class);
        //vaultPermissionsCriteria.add(Restrictions.eq("type", PermissionModel.PermissionType.VAULT));
        //List<PermissionModel> vaultPermissions = vaultPermissionsCriteria.list();

        RoleModel storedNDM = getRoleModelByName(RoleUtils.NOMINATED_DATA_MANAGER_ROLE_NAME);
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
            em.persist(ndm);
        }
    }

    private void ensureDepositorExists() {
        //Criteria vaultPermissionsCriteria = session.createCriteria(PermissionModel.class);
        //vaultPermissionsCriteria.add(Restrictions.eq("type", PermissionModel.PermissionType.VAULT));
        //List<PermissionModel> vaultPermissions = vaultPermissionsCriteria.list();

        RoleModel storedDep = getRoleModelByName(RoleUtils.DEPOSITOR_ROLE_NAME);
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
            em.persist(dep);
        }
    }
    private void populateAssignedUserCount(RoleModel role) {
        if(role != null) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class).distinct(true);
            Root<RoleAssignment> root = cq.from(RoleAssignment.class);
            cq.select(cb.count(root));
            cq.where(cb.equal(root.get(RoleAssignment_.role), role));
            int count = em.createQuery(cq).getSingleResult().intValue();
            role.setAssignedUserCount(count);
        }
    }

    @Override
    public RoleModel getIsAdmin() {
        RoleModel role = getRoleModelByName(RoleUtils.IS_ADMIN_ROLE_NAME);
        populateAssignedUserCount(role);
        return role;
    }

    @Override
    public RoleModel getDataOwner() {
        RoleModel role = getRoleModelByName(RoleUtils.DATA_OWNER_ROLE_NAME);
        populateAssignedUserCount(role);
        return role;
    }

    @Override
    public RoleModel getDepositor() {
        RoleModel role = getRoleModelByName(RoleUtils.DEPOSITOR_ROLE_NAME);
        populateAssignedUserCount(role);
        return role;
    }

    @Override
    public RoleModel getVaultCreator() {
        RoleModel role = getRoleModelByName(RoleUtils.VAULT_CREATOR_ROLE_NAME);
        populateAssignedUserCount(role);
        return role;
    }

    @Override
    public RoleModel getNominatedDataManager() {
        RoleModel role = getRoleModelByName(RoleUtils.NOMINATED_DATA_MANAGER_ROLE_NAME);
        populateAssignedUserCount(role);
        return role;
    }

    @Override
    public List<RoleModel> listAndPopulate() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<RoleModel> cq = cb.createQuery(RoleModel.class).distinct(true);
        Root<RoleModel> root = cq.from(RoleModel.class);
        cq.select(root);
        List<RoleModel> roles = getResults(cq);
        for (RoleModel role : roles) {
            populateAssignedUserCount(role);
        }
        return roles;
    }

    @Override
    public Collection<RoleModel> findAll(RoleType roleType) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<RoleModel> cq = cb.createQuery(RoleModel.class).distinct(true);
        Root<RoleModel> root = cq.from(RoleModel.class);
        cq.select(root);
        cq.where(cb.equal(root.get(RoleModel_.type), roleType));
        List<RoleModel> roles = getResults(cq);
        for (RoleModel role : roles) {
            populateAssignedUserCount(role);
        }
        return roles;
    }

    @Override
    public List<RoleModel> findAllEditableRoles() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<RoleModel> cq = cb.createQuery(RoleModel.class).distinct(true);
        Root<RoleModel> root = cq.from(RoleModel.class);
        cq.select(root);
        cq.where(cb.or(
            cb.equal(root.get(RoleModel_.type), RoleType.SCHOOL),
            cb.equal(root.get(RoleModel_.type), RoleType.VAULT)
        ));
        List<RoleModel> roles = getResults(cq);
        for (RoleModel role : roles) {
            populateAssignedUserCount(role);
        }
        return roles;
    }

    private RoleModel getRoleModelByName(String roleModelName){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<RoleModel> cq = cb.createQuery(RoleModel.class).distinct(true);
        Root<RoleModel> root = cq.from(RoleModel.class);
        cq.select(root);
        cq.where(cb.equal(root.get(RoleModel_.name), roleModelName));
        return getSingleResult(cq);
    }

    private List<PermissionModel> getPermissionModelsByType(PermissionModel.PermissionType type){
        return PermissionCustomDAOImpl.findPermissionModelsByType(em, type);
    }

    private List<PermissionModel> getAllPermissionModels(){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PermissionModel> cq = cb.createQuery(PermissionModel.class).distinct(true);
        Root<PermissionModel> root = cq.from(PermissionModel.class);
        cq.select(root);
        return getResults(cq);
    }
}
