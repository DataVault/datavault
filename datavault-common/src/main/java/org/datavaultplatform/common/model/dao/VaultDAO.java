package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.custom.VaultCustomDAO;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface VaultDAO extends BaseDAO<Vault>, VaultCustomDAO {

  @Override
  default List<Vault> list() {
    return findAll(Sort.by(Order.asc("creationTime")));
  }

  default void saveOrUpdateVault(Vault vault){
    save(vault);
  }
}