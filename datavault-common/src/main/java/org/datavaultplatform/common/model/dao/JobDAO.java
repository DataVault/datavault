package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Job;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface JobDAO extends BaseDAO<Job> {
}
