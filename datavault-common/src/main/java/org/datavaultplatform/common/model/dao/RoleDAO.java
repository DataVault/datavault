package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;

import java.util.Collection;
import java.util.List;

public interface RoleDAO {

    void storeSpecialRoles();

    void store(RoleModel role);

    RoleModel find(Long id);

    RoleModel getIsAdmin();

    RoleModel getDataOwner();

    Collection<RoleModel> findAll();

    Collection<RoleModel> findAll(RoleType roleType);

    List<RoleModel> findAllEditableRoles();

    void update(RoleModel role);

    void delete(Long id);
}
