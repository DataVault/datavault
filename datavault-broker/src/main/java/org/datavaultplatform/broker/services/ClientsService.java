package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Client;
import org.datavaultplatform.common.model.dao.ClientDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClientsService {

    private final ClientDAO clientDAO;

    @Autowired
    public ClientsService(ClientDAO clientDAO) {
        this.clientDAO = clientDAO;
    }

    public List<Client> getClients() {
        return clientDAO.list();
    }

    public void updateClient(Client client) {
        clientDAO.update(client);
    }
    
    public Client getClient(String clientID) {
        return clientDAO.findById(clientID).orElse(null);
    }

    public Client getClientByApiKey(String key) {
        return clientDAO.findByApiKey(key);
    }

    public long count() { return clientDAO.count(); }

    public void addClient(Client client) {
        clientDAO.save(client);
    }
}

