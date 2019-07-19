package org.datavaultplatform.common.model;

public enum RoleType {

    SCHOOL(true),
    VAULT(true),
    ADMIN(false);

    private final boolean customCreatable;

    RoleType(boolean customCreatable) {
        this.customCreatable = customCreatable;
    }

    public boolean isCustomCreatable() {
        return customCreatable;
    }
}
