package org.datavaultplatform.common.util;

public class RoleUtils {

    public static final String IS_ADMIN_ROLE_NAME = "IS Admin";
    public static final String IS_ADMIN_ROLE_DESCRIPTION = "An admin of the whole system, with full permissions over the system.";
    public static final String DATA_OWNER_ROLE_NAME = "Data Owner";
    public static final String DATA_OWNER_ROLE_DESCRIPTION = "An admin of a specific vault, with full permissions over that vault.";

    private RoleUtils() {}

    public static boolean isReservedRoleName(String roleName) {
        return IS_ADMIN_ROLE_NAME.equalsIgnoreCase(roleName) || DATA_OWNER_ROLE_NAME.equalsIgnoreCase(roleName);
    }
}
