package org.datavaultplatform.common.model.dao.custom;

import java.util.Collection;
import java.util.List;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;

public interface RoleCustomDAO extends BaseCustomDAO {

    void storeSpecialRoles();

    RoleModel getIsAdmin();

    RoleModel getDataOwner();

    RoleModel getDepositor();

    RoleModel getVaultCreator();

    RoleModel getNominatedDataManager();

    Collection<RoleModel> findAll(RoleType roleType);

    List<RoleModel> findAllEditableRoles();

    List<RoleModel> listAndPopulate();
}

