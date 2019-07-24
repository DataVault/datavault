package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;

import java.util.Collection;

public interface RoleDAO {

    void store(RoleModel role);

    RoleModel find(Long id);

    Collection<RoleModel> findAll();

    Collection<RoleModel> findAll(RoleType roleType);

    void update(RoleModel role);

    void delete(Long id);
}
