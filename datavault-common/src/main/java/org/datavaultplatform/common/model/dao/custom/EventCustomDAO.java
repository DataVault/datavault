package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.Vault;

public interface EventCustomDAO extends BaseCustomDAO {

    List<Event> list(String sort);

    List<Event> findVaultEvents(Vault vault);

    
}
