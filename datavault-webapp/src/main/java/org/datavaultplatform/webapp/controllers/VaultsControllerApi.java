package org.datavaultplatform.webapp.controllers;

import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.datavaultplatform.common.request.CreateVault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;

public interface VaultsControllerApi {
    @Operation(description = "updates the vault ownership",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Ownership transfer details",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = VaultTransferRequest.class))))
    ResponseEntity<Object> transferOwnership(
            @PathVariable String vaultId,
            @Valid VaultTransferRequest request);

    @Operation(description = "returns HTML page listing vaults")
    String getVaultsListing(ModelMap model, Principal principal);

    @Operation(description = "returns HTML page for specific vault")
    String getVaultByVaultId(ModelMap model, @PathVariable String vaultId, Principal principal);

    @Operation(description = "returns HTML page for showing all vaults associated with a user")
    String getUserVaults(ModelMap model,
                         @PathVariable String vaultId,
                         @PathVariable String userId,
                         Principal principal);

    @Operation(description = "returns HTML page for showing details of a specific pending vault")
    String getPendingVault(ModelMap model,
                           @PathVariable String vaultId,
                           Principal principal);

    @Operation(description = "returns HTML page for building a new vault")
    String buildVault(ModelMap model, Principal principal);

    @Operation(description = "returns HTML page for confirming a pending vault")
    String confirmPendingVault();

    // Process the completed 'create new vault' page
    @Operation(description = "Process the completed 'create new vault' page - based on action - return the appropriate HTML or redirect",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.CreateVaultForm.class))))
    String addVault(@ModelAttribute CreateVault vault, ModelMap model, @RequestParam String action, Principal principal);

    @Operation(description = "adds 'uun' as dataManager to specified vault then redirects",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Form data for adding data manager role to user",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.UunRequest.class))))
    RedirectView addDataManager(ModelMap model,
                                @PathVariable String vaultId,
                                @RequestParam("uun") String uun,
                                RedirectAttributes redirectAttrs);

    @Operation(description = "deletes 'uun' as dataManager from specified vault then redirects to vault page",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Form data for deleting data manager role from user",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.UunRequest.class))))
    RedirectView deleteDataManager(ModelMap model,
                                   @PathVariable String vaultId,
                                   @RequestParam("uun") String uun,
                                   RedirectAttributes redirectAttrs);

    @Operation(description = "updates the vault description then redirects to vault page",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Form data for updating vault description",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.DescriptionRequest.class))))
    String updateVaultDescription(ModelMap model,
                                  @PathVariable String vaultId,
                                  @Schema(hidden = true) @RequestParam("description") String description);

    @Operation(description = "updates the vault name then redirects to vault page",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Form data for updating vault name",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.NameRequest.class))))
    String updateVaultName(ModelMap model,
                           @PathVariable String vaultId,
                           @Schema(hidden = true) @RequestParam("name") String name);

    @Operation(description = "get autocomplete on a partial 'uun'")
    String autocompleteUUN(@PathVariable String term);

    @Operation(description = "returns true if the 'uun' is a valid UUN")
    String isUUN(@PathVariable String uun);

    class VaultTransferRequest {
        @Getter @Setter
        private Long role;
        @Getter
        private String user;
        @Getter @Setter
        private boolean assigningRole;
        @Getter @Setter
        private boolean orphaning;
        private String reason;
        
        @NotEmpty(message = "Please specify a transfer reason")
        public String getReason() {
            return reason;
        }
        
        @AssertTrue(message = "Please specify a user")
        public boolean isUserSelectionValid() {
            return orphaning || !Strings.isNullOrEmpty(user);
        }

        @AssertTrue(message = "Please specify a role")
        public boolean isRoleSelectionValid() {
            return !assigningRole || role != null;
        }

    }
}
