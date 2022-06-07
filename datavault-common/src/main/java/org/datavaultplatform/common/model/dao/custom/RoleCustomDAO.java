package org.datavaultplatform.common.model.dao.custom;

import java.util.Collection;
import java.util.List;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;

public interface RoleCustomDAO {

    void storeSpecialRoles();

    void store(RoleModel role);

    RoleModel find(Long id);

    RoleModel getIsAdmin();

    RoleModel getDataOwner();

    RoleModel getDepositor();

    RoleModel getVaultCreator();

    RoleModel getNominatedDataManager();

    Collection<RoleModel> findAll();

    Collection<RoleModel> findAll(RoleType roleType);

    List<RoleModel> findAllEditableRoles();

    void update(RoleModel role);

    void delete(Long id);
}
