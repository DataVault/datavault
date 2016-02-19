package org.datavaultplatform.broker.controllers;

import java.util.List;

import org.datavaultplatform.common.model.Dataset;
import org.datavaultplatform.broker.services.ExternalMetadataService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetadataController {
    
    private ExternalMetadataService externalMetadataService;

    public void setExternalMetadataService(ExternalMetadataService externalMetadataService) {
        this.externalMetadataService = externalMetadataService;
    }
    
    @RequestMapping(value = "/metadata/datasets", method = RequestMethod.GET)
    public List<Dataset> getDatasets(@RequestHeader(value = "X-UserID", required = true) String userID) {
        return externalMetadataService.getDatasets(userID);
    }
    
    @RequestMapping(value = "/metadata/datasets/{datasetid}", method = RequestMethod.GET)
    public Dataset getDataset(@RequestHeader(value = "X-UserID", required = true) String userID,
                              @PathVariable("datasetid") String datasetID) {
        return externalMetadataService.getDataset(datasetID);
    }
}
