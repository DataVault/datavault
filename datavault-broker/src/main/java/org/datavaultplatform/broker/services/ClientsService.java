package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Client;
import org.datavaultplatform.common.model.dao.ClientDAO;

import java.util.List;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;
@Service
public class ClientsService {

    private ClientDAO clientDAO;
    
    public List<Client> getClients() {
        return clientDAO.list();
    }

    public void updateClient(Client client) {
        clientDAO.update(client);
    }
    
    public Client getClient(String clientID) {
        return clientDAO.findById(clientID);
    }

    public Client getClientByApiKey(String key) {
        return clientDAO.findByApiKey(key);
    }
    
    public void setClientDAO(ClientDAO clientDAO) {
        this.clientDAO = clientDAO;
    }

    public int count() { return clientDAO.count(); }

    public void addClient(Client client) {
        clientDAO.save(client);
    }
}

