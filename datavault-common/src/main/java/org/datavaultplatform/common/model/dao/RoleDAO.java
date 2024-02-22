package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.dao.custom.RoleCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @Override
    @EntityGraph(RoleModel.EG_ROLE_MODEL)
    Optional<RoleModel> findById(Long id);

    @Override
    @EntityGraph(RoleModel.EG_ROLE_MODEL)
    List<RoleModel> findAll();

}
