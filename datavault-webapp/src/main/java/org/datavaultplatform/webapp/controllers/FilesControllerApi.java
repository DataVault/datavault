package org.datavaultplatform.webapp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Data;
import org.datavaultplatform.webapp.model.FancytreeNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FilesControllerApi {
    List<FancytreeNode> getNodes(String parent, boolean directoryOnly) throws Exception;

    @Operation(description = "Get the files listing as JSON")
    List<FancytreeNode> getFilesListing(
            @Parameter(description = "Fancytree mode parameter (e.g., 'init', 'lazy')", example = "init")
            @RequestParam(value = "mode", required = false) String mode,

            @Parameter(description = "The parent path to list files from", example = "/path/to/directory")
            @RequestParam(value = "parent", required = false) String parent) throws Exception;

    @Operation(description = "Get the directory listing as JSON")
    List<FancytreeNode> getDirListing(
            @Parameter(description = "Fancytree mode parameter (e.g., 'init', 'lazy')", example = "init")
            @RequestParam(value = "mode", required = false) String mode,

            @Parameter(description = "The parent path to list directories from", example = "/path/to/directory")
            @RequestParam(value = "parent", required = false) String parent) throws Exception;

    @Operation(description = "Get the filesize as text",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "gets filesize of 'filepath'",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    description = "english description of size",
                                    examples = {"5 GB"}))))
    String getFilesize(@Parameter(description = "The full path to the file", example = "/uploads/documents/report.pdf")
                       @RequestParam("filepath") String filepath);

    @Operation(description = "Checks Deposit Size returns JSON")
    CheckDepositSizeResponse checkDepositSize(
            @Parameter(description = "An array of file paths to check the deposit size for", example = "/path/to/file1.txt")
            @RequestParam(value = "filepath[]") String[] filePaths);

    @Operation(summary = "Upload a file chunk")
    ResponseEntity<Void> fileUpload(
            @ModelAttribute FileUploadRequest metadata,
            @RequestPart("file") MultipartFile file) throws Exception;


    record CheckDepositSizeResponse(Boolean success, String max) {
    }

    @Data
    class FileUploadRequest {
        @Schema(description = "The current chunk number", examples = "1")
        private String flowChunkNumber;

        @Schema(description = "Total number of chunks", examples = "10")
        private String flowTotalChunks;

        @Schema(description = "Size of each chunk in bytes")
        private String flowChunkSize;

        @Schema(description = "Total file size in bytes")
        private String flowTotalSize;

        @Schema(description = "Unique identifier for the file")
        private String flowIdentifier;

        @Schema(description = "The name of the file")
        private String flowFilename;

        @Schema(description = "The relative path (Base64 encoded)")
        private String flowRelativePath;

        @Schema(description = "The unique handle for this specific upload session")
        private String fileUploadHandle;

        // Standard Getters/Setters or Lombok @Data
    }
}
