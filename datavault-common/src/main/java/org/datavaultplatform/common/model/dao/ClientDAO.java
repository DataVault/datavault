package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Client;
import java.util.List;

/**
 * User: Robin Taylor
 * Date: 10/02/2016
 * Time: 10:59
 */

public interface ClientDAO {

    public void save(Client client);

    public void update(Client client);

    public List<Client> list();

    public Client findById(String Id);

    public Client findByApiKey(String Apikey);

    public int count();
}
