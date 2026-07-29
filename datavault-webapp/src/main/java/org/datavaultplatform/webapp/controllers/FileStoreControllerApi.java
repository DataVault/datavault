package org.datavaultplatform.webapp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

public interface FileStoreControllerApi {

    // Return the 'Storage Options' page
    @Operation(description = "returns HTML for listing filestores")
    String listFilestores(ModelMap model) throws Exception;

    @Operation(summary = "Add local filestore",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.AddLocalFileStoreRequest.class))))
    void addLocalFilestore(@RequestParam("path") String path) throws Exception;

    // Process the 'add SFTP FileStore' Ajax request
    @Operation(summary = "Add SFTP Filestore",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.AddSFTPFilestoreRequest.class))))
    void addSFTPFilestore(@RequestParam("hostname") String hostname, @RequestParam("port") String port, @RequestParam("path") String path, ModelMap model) throws Exception;

    @Operation(description = "Deletes the specified filestore")
    // Process the 'delete filestore' Ajax request
    void deleteFileStore(ModelMap model, @PathVariable String fileStoreId);
}
