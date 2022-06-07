package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.dao.custom.RoleCustomDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RoleDAO extends AbstractDAO<RoleModel,Long>, RoleCustomDAO {

    /* list() is not vanilla */
    @Override
    default List<RoleModel> list() {
        return listAndPopulate();
    }

}
