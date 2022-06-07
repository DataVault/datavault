package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.dao.custom.EventCustomDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface EventDAO extends BaseDAO<Event>, EventCustomDAO {
}
