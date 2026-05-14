package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.webapp.controllers.OpenApiSupport;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

public interface AdminUsersControllerApi {
    @Operation(description = "returns the users listing HTML page")
    String getUsersListing(ModelMap model,
                           @RequestParam(value = "query", required = false) String query) throws Exception;

    // Return an empty 'create new user' page
    @Operation(description = "returns the create user HTML page")
    String createUserPage(ModelMap model) throws Exception;

    // Process the completed 'create new user' page
    @Operation(description = "processes the create user HTML page",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.UserForm.class))),
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Redirect: if user does not exist - redirects to /admin/users after creating user",
                            headers = @Header(name = "Location", schema = @Schema(type = "string"))
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "if user already exists just show HTML page to create user",
                            content = @Content(mediaType = MediaType.TEXT_HTML_VALUE, schema = @Schema(type = "string")))})
    String addUser(@ModelAttribute User user, ModelMap model, @RequestParam String action) throws Exception;

    // Return an 'edit user' page
    @Operation(description = "returns the edit user HTML page")
    String editUser(ModelMap model, @PathVariable String userId) throws Exception;

    // Process the completed 'edit user' page
    @Operation(description = "processes the edit user HTML page then shows vaults",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.UserForm.class))),
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Redirect: redirects to / if the action is 'cancel'",
                            headers = @Header(name = "Location", schema = @Schema(type = "string"))
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "An HTML page showing vaults",
                            content = @Content(mediaType = MediaType.TEXT_HTML_VALUE, schema = @Schema(type = "string")))})
    String editUser(@ModelAttribute User user, ModelMap model, @PathVariable String userId, @RequestParam String action) throws Exception;
}
