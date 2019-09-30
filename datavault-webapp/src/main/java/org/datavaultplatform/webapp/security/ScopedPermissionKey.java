package org.datavaultplatform.webapp.security;

import com.google.common.base.Objects;

import java.io.Serializable;

public class ScopedPermissionKey {
    final Serializable id;
    final Class type;

    ScopedPermissionKey(Serializable id, Class type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScopedPermissionKey scopeKey = (ScopedPermissionKey) o;
        return Objects.equal(id, scopeKey.id) &&
                Objects.equal(type, scopeKey.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type);
    }
}
