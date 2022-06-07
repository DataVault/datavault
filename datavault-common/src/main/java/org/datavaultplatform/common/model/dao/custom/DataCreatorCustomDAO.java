package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.DataCreator;

public interface DataCreatorCustomDAO {
    void save(List<DataCreator> dataCreators);

    DataCreator findById(String Id);

    void update(DataCreator dataCreator);

    void delete(String id);
}
