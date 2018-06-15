package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.DataManager;
import org.datavaultplatform.common.model.dao.DataManagerDAO;

import java.util.List;

public class DataManagersService {

    private DataManagerDAO dataManagerDAO;
    
    public List<DataManager> getDataManagers() {
        return dataManagerDAO.list();
    }
    
    public void addDataManager(DataManager dataManager) {
        dataManagerDAO.save(dataManager);
    }
    
    public DataManager getDataManager(String dataManagerID) {
        return dataManagerDAO.findById(dataManagerID);
    }

    public void deleteDataManager(String dataManagerID) {
        dataManagerDAO.deleteById(dataManagerID);
    }
    
    public void setDataManagerDAO(DataManagerDAO dataManagerDAO) {
        this.dataManagerDAO = dataManagerDAO;
    }
}

