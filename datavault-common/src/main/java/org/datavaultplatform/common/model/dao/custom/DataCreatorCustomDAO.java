package org.datavaultplatform.common.model.dao.custom;

import org.datavaultplatform.common.model.DataCreator;

import java.util.List;


public interface DataCreatorCustomDAO extends BaseCustomDAO {
    void save(List<DataCreator> dataCreators);
}
