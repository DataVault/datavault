package org.datavaultplatform.common.model.dao.custom;

import java.util.List;

import org.datavaultplatform.common.model.Vault;
import org.springframework.data.jpa.repository.EntityGraph;


public interface VaultCustomDAO extends BaseCustomDAO {

    @EntityGraph(Vault.EG_VAULT)
    List<Vault> list(String userId, String sort, String order, String offset, String maxResult);

    @EntityGraph(Vault.EG_VAULT)
    List<Vault> search(String userId, String query, String sort, String order, String offset, String maxResult);

    int count(String userId);

    int getRetentionPolicyCount(int status);

    int getTotalNumberOfVaults(String userId);

    int getTotalNumberOfVaults(String userId, String query);

    List<Object[]> getAllProjectsSize();
}