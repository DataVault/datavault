package org.datavaultplatform.common.model.dao.custom;

import org.datavaultplatform.common.model.Client;


public interface ClientCustomDAO extends BaseCustomDAO {

    Client findByApiKey(String Apikey);
}
