package org.datavaultplatform.webapp.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.webapp.model.VaultReviewModel;

import static org.datavaultplatform.webapp.controllers.admin.AdminRolesControllerApi.DEOP_ID;
import static org.datavaultplatform.webapp.controllers.admin.AdminRolesControllerApi.OP_ID;

public class OpenApiSupport {

    public record ArchiveStoredUpdateRequest(String properties) {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class CreateVaultForm extends CreateVault {
        @Schema(description = "The action to perform on the form", requiredMode = Schema.RequiredMode.REQUIRED)
        private String action;
    }

    @Data
    public static class AddNewRoleAssignmentRequest {
        @Schema(name = "user", description = "The Id of the user")
        private String userId;
        @Schema(name = "role", description = "The Id of the role")
        private Long roleId;
    }

    @Data
    public static class UpdateExistingRoleAssignmentRequest {
        @Schema(name = "assignment", description = "The Id of the role assignment")
        private Long assignmentId;
        @Schema(name = "role", description = "The Id of the role")
        private Long roleId;
    }

    @Data
    public static class DeleteRoleAssignmentRequest {
        @Schema(name = "assignment", description = "The Id of the role assignment")
        private Long assignmentId;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class VaultReviewModelForm extends VaultReviewModel {
        private String action;
    }


    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class UserForm extends User {
        @Schema(description = "The action to perform on the form", requiredMode = Schema.RequiredMode.REQUIRED)
        private String action;
    }

    public record DeleteRoleRequest(long id) {
    }

    // A simple static class just for documentation
    @Data
    public static class SuperAdminRequest {
        @Schema(name = OP_ID, description = "The ID of the user to promote", examples = "user-123")
        private String userId;
    }

    // A simple static class just for documentation
    @Data
    public static class DeleteSuperAdminRequest {
        @Schema(name = DEOP_ID, description = "The ID of the user to delete", examples = "user-123")
        private String userId;
    }

    @Data
    public static class RoleSaveRequest {
        @Schema(description = "The unique ID of the role", examples = "101")
        long id;

        @Schema(description = "The display name of the role", examples = "Admin")
        String name;

        @Schema(description = "System type category", examples = "INTERNAL")
        String type;

        @Schema(description = "Current activation status", allowableValues = {"ACTIVE", "INACTIVE"})
        String status;

        @Schema(description = "Detailed purpose of the role", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String description;

        @Schema(description = "List of assigned permission keys")
        String[] permissions;
    }


    public record AddLocalFileStoreRequest(
            @Parameter(description = "The absolute file system path for the new local filestore")
            String path) {
    }

    record AddSFTPFilestoreRequest(String hostname, String port, String path) {
    }

    public record UpdateRoleAssignmentRequest(
            @Schema(name = "assignment")
            Long assignmentId,

            @Schema(name = "role")
            Long roleId
    ) {
    }

    public record RemoveRoleAssignmentRequest(@Schema(name = "assignment") Long assignmentId) {
    }

    public record DescriptionRequest(String description) {
    }

    public record NameRequest(String name) {
    }

    public record ArchiveStoreRequest(
            @Schema(description = "Store properties", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            String properties,

            @Schema(description = "Store label", requiredMode = Schema.RequiredMode.REQUIRED, examples = "MyLocalStore")
            String label,

            @Schema(description = "Storage type", requiredMode = Schema.RequiredMode.REQUIRED, examples = "LOCAL")
            String type,

            @Schema(description = "Enable retrieval", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            String retrieve
    ) {}

    public record UunRequest(String uun) {
    }
}
