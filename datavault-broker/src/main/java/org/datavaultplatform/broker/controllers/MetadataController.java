package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import java.util.List;

import org.datavaultplatform.common.model.Dataset;
import org.datavaultplatform.broker.services.ExternalMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
//@CrossOrigin
public class MetadataController {
    
    private final ExternalMetadataService externalMetadataService;

    @Autowired
    public MetadataController(ExternalMetadataService externalMetadataService) {
        this.externalMetadataService = externalMetadataService;
    }

    @GetMapping(value = "/metadata/datasets")
    public List<Dataset> getDatasets( @RequestHeader(HEADER_USER_ID) String userId) {
        return externalMetadataService.getDatasets(userId);
    }
    
    @GetMapping("/metadata/datasets/{datasetId}")
    public Dataset getDataset(
            @RequestHeader(HEADER_USER_ID) String userId,
            @PathVariable String datasetId) {
        return externalMetadataService.getDataset(datasetId);
    }
}
