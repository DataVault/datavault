package org.datavaultplatform.common.util;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleType;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;

@Slf4j
public abstract class DaoUtils {

    public static String HINT_FETCH_GRAPH = EntityGraphType.FETCH.getKey();

    public static final String FULL_ACCESS_INDICATOR = "*";

    private DaoUtils() {}

    public static Set<String> getPermittedSchoolIds(Session session, String userId, Permission permission) {
        Criteria roleAssignmentsCriteria = session.createCriteria(RoleAssignment.class, "roleAssignment");
        roleAssignmentsCriteria.createAlias("roleAssignment.role", "role");
        roleAssignmentsCriteria.createAlias("role.permissions", "permissions");
        roleAssignmentsCriteria.add(Restrictions.eq("roleAssignment.userId", userId));
        roleAssignmentsCriteria.add(Restrictions.eq("permissions.id", permission.getId()));
        roleAssignmentsCriteria.add(Restrictions.or(
                Restrictions.isNotNull("roleAssignment.schoolId"),
                Restrictions.eq("role.type", RoleType.ADMIN)));
        List<RoleAssignment> roleAssignments = roleAssignmentsCriteria.list();
        return roleAssignments.stream()
                .map(roleAssignment -> roleAssignment.getSchoolId() == null ? FULL_ACCESS_INDICATOR : roleAssignment.getSchoolId())
                .collect(Collectors.toSet());
    }

    public static Set<String> getPermittedSchoolIds(EntityManager em, String userId, Permission permission) {
        String jql =
            "select roleAssignment from org.datavaultplatform.common.model.RoleAssignment roleAssignment " +
                "INNER JOIN roleAssignment.role as role " +
                "INNER JOIN role.permissions as permissions " +
                "WHERE roleAssignment.userId = :userId " +
                "AND   permissions.id        = :permissionId " +
                "AND   ( " +
                "  roleAssignment.schoolId IS NOT NULL " +
                "  OR " +
                "  role.type = :roleType" +
                ")";

        TypedQuery<RoleAssignment> query = em.createQuery(jql, RoleAssignment.class);
        query.setParameter("userId", userId);
        query.setParameter("permissionId", permission.getId());
        query.setParameter("roleType", RoleType.ADMIN);

        List<RoleAssignment> roleAssignments = query.getResultList();
        return roleAssignments.stream()
            .map(roleAssignment -> roleAssignment.getSchoolId() == null ? FULL_ACCESS_INDICATOR
                : roleAssignment.getSchoolId())
            .collect(Collectors.toSet());
    }

    public static <T> TypedQuery<T> addEntityGraph(EntityManager em, Class<T> clazz, TypedQuery<T> query) {
        String graphName = String.format("eg.%s.1",clazz.getSimpleName());
        EntityGraph<T> graph = (EntityGraph<T>) em.getEntityGraph(graphName);
        log.info(String.format("adding graph [%s] to [%s]", graph.getName(), clazz.getSimpleName()));
        return query.setHint(DaoUtils.HINT_FETCH_GRAPH, graph);
    }

}
