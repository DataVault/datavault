package org.datavaultplatform.common.model.dao.custom;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.PermissionModel_;


public class PermissionCustomDAOImpl extends BaseCustomDAOImpl implements PermissionCustomDAO {

    public PermissionCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public void synchronisePermissions() {

        // First, remove any permissions that are no longer found in the Permission enum from all roles...
        String commaSeparatedPermissionIds = Arrays.stream(Permission.values())
                .map(Permission::getId)
                .collect(Collectors.joining(","));
        String deleteUnknownRolePermissionsSql = "DELETE FROM Role_permissions WHERE NOT FIND_IN_SET(permission_id, '"
                + commaSeparatedPermissionIds
                + "');";
        em.createNativeQuery(deleteUnknownRolePermissionsSql).executeUpdate();

        // ...then remove those permissions themselves...
        String deleteUnknownPermissionsSql = "DELETE FROM Permissions WHERE NOT FIND_IN_SET(id, '"
                + commaSeparatedPermissionIds
                + "');";
        em.createNativeQuery(deleteUnknownPermissionsSql).executeUpdate();

        // ...then store default entries for anything found in the enum but not the database
        Set<String> permissionIds = Arrays.stream(Permission.values())
            .map(Permission::getId)
            .collect(Collectors.toSet());

        Set<Permission> persistedPermissions = findPermissiondWhereIdsIn(permissionIds).stream()
                .map(PermissionModel::getPermission)
                .collect(Collectors.toSet());
        for (Permission p : Permission.values()) {
            if (!persistedPermissions.contains(p)) {
                em.persist(PermissionModel.createDefault(p));
            }
        }
    }

    @Override
    public PermissionModel find(Permission permission) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PermissionModel> cq = cb.createQuery(PermissionModel.class).distinct(true);
        Root<PermissionModel> root = cq.from(PermissionModel.class);
        cq.select(root);
        cq.where(cb.equal(root.get(PermissionModel_.ID), permission.name()));
        return getSingleResult(cq);
    }

    @Override
    public List<PermissionModel> findByType(PermissionModel.PermissionType type) {
        return findPermissionModelsByType(em, type);
    }

    /*
     * This code is also used by RoleCustomDAOImpl
     */
    protected static List<PermissionModel> findPermissionModelsByType(EntityManager em, PermissionModel.PermissionType type) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PermissionModel> cq = cb.createQuery(PermissionModel.class).distinct(true);
        Root<PermissionModel> root = cq.from(PermissionModel.class);
        cq.select(root);
        cq.where(cb.equal(root.get(PermissionModel_.TYPE), type));
        return getResults(em, cq);
    }

    private List<PermissionModel> findPermissiondWhereIdsIn(Collection<String> ids) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PermissionModel> cq = cb.createQuery(PermissionModel.class).distinct(true);
        Root<PermissionModel> root = cq.from(PermissionModel.class);
        cq.select(root).where(root.get(PermissionModel_.ID).in(ids));
        return getResults(cq);
    }
}
