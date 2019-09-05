package org.datavaultplatform.common.util;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleType;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DaoUtils {

    public static final String FULL_ACCESS_INDICATOR = "*";

    private DaoUtils() {}

    public static Set<String> getPermittedSchoolIds(Session session, String userId, Permission permission) {
        Criteria roleAssignmentsCriteria = session.createCriteria(RoleAssignment.class, "roleAssignment");
        roleAssignmentsCriteria.createAlias("roleAssignment.role", "role");
        roleAssignmentsCriteria.createAlias("role.permissions", "permissions");
        roleAssignmentsCriteria.add(Restrictions.eq("roleAssignment.userId", userId));
        roleAssignmentsCriteria.add(Restrictions.eq("permissions.id", permission.getId()));
        roleAssignmentsCriteria.add(Restrictions.or(
                Restrictions.isNotNull("roleAssignment.school"),
                Restrictions.eq("role.type", RoleType.ADMIN)));
        List<RoleAssignment> roleAssignments = roleAssignmentsCriteria.list();
        return roleAssignments.stream()
                .map(roleAssignment -> roleAssignment.getSchool() == null ? FULL_ACCESS_INDICATOR : roleAssignment.getSchool().getID())
                .collect(Collectors.toSet());
    }
}
