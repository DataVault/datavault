package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.DataManager;
import org.datavaultplatform.common.model.dao.DataManagerDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DataManagersService {

    private final DataManagerDAO dataManagerDAO;

    @Autowired
    public DataManagersService(DataManagerDAO dataManagerDAO) {
        this.dataManagerDAO = dataManagerDAO;
    }

    public List<DataManager> getDataManagers() {
        return dataManagerDAO.list();
    }
    
    public void addDataManager(DataManager dataManager) {
        dataManagerDAO.save(dataManager);
    }
    
    public DataManager getDataManager(String dataManagerID) {
        return dataManagerDAO.findById(dataManagerID).orElse(null);
    }

    public void deleteDataManager(String dataManagerID) {
        dataManagerDAO.deleteById(dataManagerID);
    }
}

