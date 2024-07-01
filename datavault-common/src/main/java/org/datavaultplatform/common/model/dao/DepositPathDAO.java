package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.DepositPath;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DepositPathDAO extends BaseDAO<DepositPath> {

}
