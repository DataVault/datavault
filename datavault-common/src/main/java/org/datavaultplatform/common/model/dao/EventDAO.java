package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.dao.custom.EventCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface EventDAO extends BaseDAO<Event>, EventCustomDAO {

  @Override
  @EntityGraph(Event.EG_EVENT)
  Optional<Event> findById(String id);

  @Override
  @EntityGraph(Event.EG_EVENT)
  List<Event> findAll();
}
