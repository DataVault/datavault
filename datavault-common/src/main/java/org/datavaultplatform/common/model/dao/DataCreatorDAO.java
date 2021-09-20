package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.DataCreator;

import java.util.List;

public interface DataCreatorDAO {
    void save(List<DataCreator> dataCreators);

    DataCreator findById(String Id);

    void update(DataCreator dataCreator);

    void delete(String id);
}
