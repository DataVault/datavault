package org.datavaultplatform.broker.controllers.admin;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.datavaultplatform.broker.services.RetentionPoliciesService;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@CrossOrigin
@Tag(name="admin-retention-policies-controller", description = "Administrator RetentionPolicies functions")
public class AdminRetentionPoliciesController {

    private final Logger logger = LoggerFactory.getLogger(AdminRetentionPoliciesController.class);

    private final RetentionPoliciesService retentionPoliciesService;

    public AdminRetentionPoliciesController(RetentionPoliciesService retentionPoliciesService) {
        this.retentionPoliciesService = retentionPoliciesService;
    }

    @Operation(
            summary = "Create a new Retention Policy",
            description = "Creates a new Retention Policy in the DataVault system."
    )
    @PostMapping(value = "/admin/retentionpolicies", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateRetentionPolicy> addRetentionPolicy(@RequestHeader(HEADER_USER_ID) String userId,
                                                                    @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                                                    @RequestBody CreateRetentionPolicy createRetentionPolicy) {

        logger.info("Adding new RetentionPolicy with name = " + createRetentionPolicy.getName());

        RetentionPolicy retentionPolicy = retentionPoliciesService.buildRetentionPolicy(createRetentionPolicy);

        try{
            retentionPoliciesService.addRetentionPolicy(retentionPolicy);
        }catch(Exception ex){
            logger.error("Couldn't add retention policy: ", ex);
            return new ResponseEntity<>(createRetentionPolicy, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //TODO - when returning HttpStatus.CREATED we should add Location header with URL of created resource
        return new ResponseEntity<>(createRetentionPolicy, HttpStatus.CREATED);
    }


    @Operation(
            summary = "Get a Retention Policy",
            description = "Retrieves a specific Retention Policy by its ID."    )
    @GetMapping(value = "/admin/retentionpolicies/{policyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateRetentionPolicy> getRetentionPolicy(@RequestHeader(HEADER_USER_ID) String userId,
                                                                    @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                                                    @PathVariable String policyId) {

        logger.info("Getting RetentionPolicy with id = " + policyId) ;

        // Build a transfer object from the real one. We do this to avoid problems with dates, although I am sure there
        // is a better way to do it.

        RetentionPolicy rp = retentionPoliciesService.getPolicy(policyId);
        CreateRetentionPolicy crp = retentionPoliciesService.buildCreateRetentionPolicy(rp);

        return new ResponseEntity<>(crp, HttpStatus.OK);
    }

    @Operation(
            summary = "Edit a Retention Policy",
            description = "Updates an existing Retention Policy."
    )
    @PutMapping(value = "/admin/retentionpolicies", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateRetentionPolicy> editRetentionPolicy(@RequestHeader(HEADER_USER_ID) String userId,
                                                                     @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                                                     @RequestBody CreateRetentionPolicy crp) {

        RetentionPolicy rp = retentionPoliciesService.buildRetentionPolicy(crp);
        retentionPoliciesService.updateRetentionPolicy(rp);

        return new ResponseEntity<>(crp, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete a Retention Policy",
            description = "Deletes a Retention Policy by its ID."
    )
    @DeleteMapping("/admin/retentionpolicies/delete/{policyId}")
    public void deleteRetentionPolicy(@RequestHeader(HEADER_USER_ID) String userId,
                                      @PathVariable String policyId) {

        retentionPoliciesService.delete(policyId);
    }
}
