package org.datavaultplatform.metadata.controllers;

import java.util.List;

import org.datavaultplatform.common.metadata.Dataset;
import org.datavaultplatform.metadata.services.DatasetsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatasetsController {
    
    private DatasetsService datasetsService;
    
    public void setDatasetsService(DatasetsService datasetsService) {
        this.datasetsService = datasetsService;
    }
    
    @RequestMapping(value = "/datasets", method = RequestMethod.GET)
    public List<Dataset> getDatasets() {
        return datasetsService.getDatasets();
    }
}
