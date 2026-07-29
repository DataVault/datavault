package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.datavaultplatform.webapp.controllers.OpenApiSupport;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

public interface AdminArchiveStoreControllerApi {

    // Return the 'Archive Stores' page
    @Operation(description = "returns HTML for listing archive stores")
    String listArchivestores(ModelMap model);

    // Process the 'add local ArchiveStore' Ajax request
    @Operation(summary = "Add local Archive Store",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.ArchiveStoreRequest.class))))
    void addLocalArchiveStore(@RequestParam(value = "properties", required = false) String properties,
                              @RequestParam(value = "label") String label,
                              @RequestParam(value = "type") String type,
                              @RequestParam(value = "retrieve", required = false) String retrieve) throws Exception;

    // Process the 'delete archivestore' Ajax request
    void deleteArchiveStore(ModelMap model, @PathVariable String archiveStoreId);

    // Mark this archive store as being the preferred one for retrieval
    void enableRetrieve(ModelMap model, @PathVariable String archiveStoreId);

    // Mark this archivestore as no longer being preferred for retrieval
    void disableRetrieve(ModelMap model, @PathVariable String archiveStoreId);

    // Process the 'update properties archivestore' Ajax request
    @Operation(summary = "Update store properties",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.ArchiveStoredUpdateRequest.class))))
    void updateArchiveStore(ModelMap model,
                            @PathVariable String archiveStoreId,
                            @RequestParam("properties") String properties) throws Exception;

}
