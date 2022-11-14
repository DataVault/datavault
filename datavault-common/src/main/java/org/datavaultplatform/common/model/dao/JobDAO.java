package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.Job;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface JobDAO extends BaseDAO<Job> {

  @Override
  @EntityGraph(Job.EG_JOB)
  Optional<Job> findById(String id);

  @Override
  @EntityGraph(Job.EG_JOB)
  List<Job> findAll();
}
