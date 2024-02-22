package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.DataCreator;


public interface DataCreatorCustomDAO extends BaseCustomDAO {
    void save(List<DataCreator> dataCreators);
}
