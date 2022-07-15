package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.Vault;
import org.springframework.data.jpa.repository.EntityGraph;

public interface EventCustomDAO extends BaseCustomDAO {

    @EntityGraph(Event.EG_EVENT)
    List<Event> list(String sort);

    @EntityGraph(Event.EG_EVENT)
    List<Event> findVaultEvents(Vault vault);

    
}
