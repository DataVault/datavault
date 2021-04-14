<#-- @ftlvariable name="vault" type="org.datavaultplatform.common.response.VaultInfo" -->
<style>
    #add-new {
        margin-top: 2em;
    }

    #role-assignments {
        margin-top: 2em;
        padding: 0 2em 0 0;
    }

    #role-filter-toggle {
        cursor: pointer;
    }

    #role-filter-panel {
        position: absolute;
        padding: 1em;
        background-color: #ebebeb;
    }

    .action-column {
        width: 100px;
        text-align: center;
    }

    .action-column .btn {
        float: left;
        margin: 0 2px;
    }

    .role-definitions {
        margin-top: 2em;
        border-left: 1px solid;
        padding: 0 1em;
    }

    .role-definitions .role-definition-title {
        margin-top: 0;
    }

    .role-definitions .definition {
        margin: 0.5em 0;
    }

    .btn-delete {
        color: #b94a48;
        background-color: #f2dede;
    }

    .form-group {
        display: block;
        text-align: left;
    }

    .form-confirm {
        display: flex;
        margin-left: 0px !important;
        padding-left: 10px;
    }

    .form-label {
        display: block;
        text-align: left !important;
    }

    .form-input {
        min-width: 70%;
        text-align: left !important;
    }

    .checkbox {
        margin-left: 10px !important;
    }

    .modal-title {
        font-weight: 500;
        color: #000;
    }

    .control-label {
        font-weight: 400;
    }

    #role-update-user-name {
        padding: 7px 0 0 27px;
    }

</style>

<#import "/spring.ftl" as spring />
<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />
<#assign assignVaultRolesSecurityExpression = "hasPermission('${vault.ID}', 'vault', 'ASSIGN_VAULT_ROLES') or hasPermission('${vault.groupID}', 'GROUP', 'ASSIGN_SCHOOL_VAULT_ROLES')">
<#assign transferOwnershipSecurityExpression = "hasPermission('${vault.ID}', 'vault', 'CAN_TRANSFER_VAULT_OWNERSHIP') or hasPermission('${vault.groupID}', 'GROUP', 'TRANSFER_SCHOOL_VAULT_OWNERSHIP')">
<#assign showActionsColumnSecurityExpression = assignVaultRolesSecurityExpression + " or " + transferOwnershipSecurityExpression />

<@sec.authorize access=transferOwnershipSecurityExpression>
<div id="orphan-dialog" class="modal fade" tabindex="-1" role="dialog"
     aria-labelledby="orphan-user-title"
     aria-hidden="true">

    <div class="modal-dialog">
        <div class="modal-content">
            <form id="transfer-form" class="form form-horizontal" role="form"
                  action="<@spring.url relativeUrl="/vaults/${vault.ID}/data-owner/update" />"
                  method="post">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i
                                class="fa fa-times" aria-hidden="true"></i></button>
                    <h4 class="modal-title" id="orphan-user-title">Transfer Ownership</h4>
                </div>
                <div class="modal-body">

                    <div id="orphan-dialog-error" class="alert alert-danger hidden error" role="alert"></div>
                    <div id="transfer-inputs">

                        <div class="col-sm-10 form-group ui-widget">
                            <label for="new-owner-name" class="control-label form-label">New Data Owner</label>
                            <div class="form-input">
                                <input id="new-owner-name" type="text" class="form-control" name="user" value=""/>
                            </div>
                        </div>
                    </div>

                    <@sec.authorize access=assignVaultRolesSecurityExpression>
                    <div class="col-sm-10 form-group ui-widget">
                        <label for="transfer-role" class="control-label form-label">Vault role to assign to previous
                            Data Owner</label>
                        <div class="form-input">
                            <select id="transfer-role" name="role" class="form-control">
                                <#list roles as role>
                                    <option value="${role.id}">${role.name}</option>
                                </#list>
                            </select>
                        </div>
                    </div>

                    <div class="form-group ui-widget col-sm-10 form-confirm">
                        <div class="checkbox">
                            <#if dataOwner??>
                                <input class="form-check-input" id="confirm-checkbox" type="checkbox" name="assigningRole" checked="checked"/>
                            <#else>
                                <input class="form-check-input" id="confirm-checkbox" type="checkbox"
                                       name="assigningRole" disabled/>
                            </#if>
                            </div>
                            <label for="confirm-checkbox" class="control-label">Assign role to previous data
                                owner?</label>
                    </div>
                    </@sec.authorize>

                    <@sec.authorize access="hasRole('ROLE_IS_ADMIN') or hasPermission('${vault.groupID}', 'GROUP', 'CAN_ORPHAN_SCHOOL_VAULTS')">
                        <div class="form-group ui-widget col-sm-10 form-confirm">
                            <div class="checkbox">
                                <#if dataOwner??>
                                    <input class="form-check-input" id="orphan-checkbox" type="checkbox" name="orphaning"/>
                                <#else>
                                    <input class="form-check-input" id="orphan-checkbox" type="checkbox" name="orphaning" disabled/>
                                </#if>
                            </div>
                            <label for="orphan-checkbox" class="control-label">Temporarily orphan this vault?</label>
                        </div>
                    </@sec.authorize>

                    <div class="col-sm-10 form-group ui-widget control-form--checkbox">
                        <label for="transfer-reason" class="control-label form-label">Transfer reason</label>
                        <div class="form-input">
                            <textarea class="form-control" id="transfer-reason" name="reason"></textarea>
                        </div>
                    </div>
                </div>

                <input type="hidden" id="submitAction" name="action" value="submit"/>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default btn-cancel" data-dismiss="modal"><i class="fa fa-times" aria-hidden="true"></i> Cancel</button>
                    <button type="submit" class="btn btn-primary"><span class="fa fa-floppy-o"></span> Save</button>
                </div>
            </form>
        </div>
    </div>
</div>
</@sec.authorize>

<@sec.authorize access=assignVaultRolesSecurityExpression>
<div id="add-new-dialog" class="modal fade" tabindex="-1" role="dialog"
     aria-labelledby="add-new-user-title"
     aria-hidden="true">

    <div class="modal-dialog">
        <div class="modal-content">
            <form id="create-form" class="form form-horizontal" role="form"
                  action="<@spring.url relativeUrl="/security/roles/vault/${vault.ID}/user" />"
                  method="post">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i
                                class="fa fa-times" aria-hidden="true"></i></button>
                    <h4 class="modal-title" id="add-new-user-title">Add New User</h4>
                </div>
                <div class="modal-body">
                    <div id="add-new-dialog-error" class="alert alert-danger error hidden" role="alert"></div>
                    <div class="form-group ui-widget">
                        <label for="new-user-name" class="control-label col-sm-2">Name</label>
                        <div class="col-sm-10">
                            <input id="new-user-name" type="text" class="form-control" name="user" value=""/>
                        </div>
                    </div>
                    <div class="form-group ui-widget">
                        <label for="new-user-role" class="control-label col-sm-2">Role</label>
                        <div class="col-sm-10">
                            <select id="new-user-role" name="role" class="form-control" >
                                <option hidden value="" selected disabled>Please select</option>
                                <#list roles as role>
                                    <option value="${role.id}">${role.name}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                </div>
                <input type="hidden" id="submitAction" name="action" value="submit"/>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default btn-cancel" data-dismiss="modal"><i class="fa fa-times" aria-hidden="true"></i> Cancel</button>
                    <button type="submit" class="btn btn-primary"><span class="fa fa-floppy-o"></span> Save</button>
                </div>
            </form>
        </div>
    </div>

</div>

<div id="update-existing-dialog" class="modal fade" tabindex="-1" role="dialog"
     aria-labelledby="update-existing-title" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="update-form" class="form form-horizontal" role="form"
                  action="<@spring.url relativeUrl="/security/roles/vault/${vault.ID}/user/update" />"
                  method="post">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i
                                class="fa fa-times" aria-hidden="true"></i></button>
                    <h4 class="modal-title" id="update-existing-title">Edit User's Role</h4>
                </div>
                <div class="modal-body">
                    <div id="update-existing-dialog-error" class="alert alert-danger error hidden" role="alert"></div>
                    <div class="form-group ui-widget">
                        <label for="role-update-user-name" class="control-label col-sm-2">Name</label>
                        <label id="role-update-user-name"></label>
                    </div>
                    <div class="form-group ui-widget">
                        <label for="update-user-role" class="control-label col-sm-2">Role</label>
                        <div class="col-sm-10">
                            <select id="update-user-role" name="role" class="form-control">
                                <#list roles as role>
                                    <option value="${role.id}">${role.name}</option></#list>
                            </select>
                        </div>
                    </div>
                </div>
                <input type="hidden" id="role-update-assignment-id" name="assignment"/>
                <input type="hidden" id="submitAction" name="action" value="submit"/>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default btn-cancel" data-dismiss="modal"><i class="fa fa-times" aria-hidden="true"></i> Cancel</button>
                    <button type="submit" class="btn btn-primary"><span class="fa fa-floppy-o"></span> Save</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div id="delete-dialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="delete-title"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="delete-form" class="form form-horizontal" role="form"
                  action="<@spring.url relativeUrl="/security/roles/vault/${vault.ID}/user/delete" />"
                  method="post">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i
                                class="fa fa-times" aria-hidden="true"></i></button>
                    <h4 class="modal-title" id="delete-title">Delete User</h4>
                </div>
                <div id="delete-dialog-error" class="alert alert-danger hidden error" role="alert"></div>
                <div class="modal-body">
                    <p>Do you want to delete <label id="delete-role-user-name"></label> from this vault?</p>
                </div>
                <input type="hidden" id="delete-role-assignment-id" name="assignment"/>
                <input type="hidden" id="submitAction" name="action" value="submit"/>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <div class="modal-footer">
                    <button type="button" class="btn btn-default btn-cancel" data-dismiss="modal"><i class="fa fa-times" aria-hidden="true"></i> Cancel</button>
                    <button type="submit" class="btn btn-danger btn-delete"><i class="fa fa-trash"></i> Delete</button>
                </div>
            </form>
        </div>
    </div>
</div>


<div id="add-new">
    <a href="#" data-toggle="modal" data-target="#add-new-dialog">+ Add new user to vault</a>
</div>
</@sec.authorize>

<div class="col-md-8" id="role-assignments">
    <div class="table-responsive">
        <table class="table table-striped">
            <thead>
            <tr>
                <th>User</th>
                <th>
                    <span id="role-filter-toggle">Role <i class="filter-toggle-icon fa fa-caret-down"></i></span>
                    <div id="role-filter-panel" class="hidden">
                        <#list roles as role>
                            <div>
                                <input type="checkbox" id="role-filter-${role.id}" name="role-filter"
                                       value="${role.name}"/>
                                <label for="role-filter-${role.id}">${role.name}</label>
                            </div>
                        </#list>
                    </div>
                </th>
                <@sec.authorize access=showActionsColumnSecurityExpression>
                <th class="action-column">Actions</th>
                </@sec.authorize>
            </tr>
            </thead>
            <tbody>
            <#if dataOwner??>
                <tr>
                    <td>${dataOwner.userId}</td>
                    <td class="role-column">${dataOwner.role.name}</td>
                    <@sec.authorize access=showActionsColumnSecurityExpression>
                    <td class="action-column">
                        <@sec.authorize access=transferOwnershipSecurityExpression>
                        <a href="#" class="btn btn-default" data-toggle="modal"
                           data-target="#orphan-dialog"
                           data-user-name="${dataOwner.userId}"
                           title="Transfer ownership of this vault."><i
                                    class="glyphicon glyphicon-transfer"></i></a>
                        </@sec.authorize>
                    </td>
                    </@sec.authorize>
                </tr>
            <#else>
                <tr>
                    <td>Orphaned</td>
                    <td class="role-column">N/A</td>
                    <@sec.authorize access=showActionsColumnSecurityExpression>
                        <td class="action-column">
                            <@sec.authorize access=transferOwnershipSecurityExpression>
                                <a href="#" class="btn btn-default" data-toggle="modal"
                                   data-target="#orphan-dialog"
                                   data-user-name=""
                                   title="Transfer ownership of this vault."><i
                                            class="glyphicon glyphicon-transfer"></i></a>
                            </@sec.authorize>
                        </td>
                    </@sec.authorize>
                </tr>
            </#if>

            <#list roleAssignments as assignment>
                <tr>
                    <td>${assignment.userId}</td>
                    <td class="role-column">${assignment.role.name}</td>
                    <@sec.authorize access=showActionsColumnSecurityExpression>
                    <td class="action-column">
                        <@sec.authorize access=assignVaultRolesSecurityExpression>
                        <a href="#" class="btn btn-default" data-toggle="modal"
                           data-target="#update-existing-dialog" data-assignment-id="${assignment.id}"
                           data-user-name="${assignment.userId}"
                           data-user-role="${assignment.role.id}"
                           title="Edit role for ${assignment.userId}."><i class="fa fa-pencil"></i></a>
                        <a href="#" class="btn btn-default btn-delete" data-toggle="modal"
                           data-target="#delete-dialog" data-assignment-id="${assignment.id}"
                           data-user-name="${assignment.userId}"
                           title="Delete role for ${assignment.userId}."><i class="fa fa-trash"></i></a>
                        </@sec.authorize>
                    </td>
                    </@sec.authorize>
                </tr>
            </#list>
            </tbody>
        </table>
    </div>
</div>

<div id="role-definitions" class="col-md-4">
    <div class="role-definitions">
        <h3 class="role-definition-title">Role Definitions</h3>
        <#list roles as role>
            <div class="definition">
                <b>${role.name}:</b> ${role.description}
            </div>
        </#list>
    </div>
</div>

<script type="text/javascript">
    $('#role-filter-toggle').click(function () {
        $('.filter-toggle-icon').toggleClass('fa-caret-down').toggleClass('fa-caret-up');
        $('#role-filter-panel').toggleClass('hidden');
    });

    $('[name="role-filter"]').on('change', function () {
        var allValues = [];
        $('[name="role-filter"]:checked').each(function () {
            allValues.push($(this).val());
        });
        $('#role-assignments table tbody tr').filter(function () {
            $(this).toggle(allValues.length === 0 || allValues.indexOf($(this).find('.role-column').text()) >= 0);
        });
    });
    $('[data-target="#add-new-dialog"]').click(function () {
        $('#add-new-dialog form').trigger('reset');
        $('#add-new-dialog .error').addClass('hidden').text('');
    });

    $('[data-target="#orphan-dialog"]').click(function () {
        $('#orphan-dialog form').trigger('reset');
        $('#orphan-dialog .error').addClass('hidden').text('');
    });

    $('[data-target="#update-existing-dialog"]').click(function () {
        $('#update-existing-dialog form').trigger('reset');

        var assignmentId = $(this).data('assignment-id');
        var userName = $(this).data('user-name');
        var role = $(this).data('user-role');
        $('#role-update-assignment-id').val(assignmentId);
        $('#update-user-role').val(role);
        $('#role-update-user-name').text(userName);
        $('#update-error').addClass('hidden').text('');
    });

    $('[data-target="#delete-dialog"]').click(function () {
        $('#delete-dialog form').trigger('reset');
        var assignmentId = $(this).data('assignment-id');
        var userName = $(this).data('user-name');
        $('#delete-role-assignment-id').val(assignmentId);
        $('#delete-role-user-name').text(userName);
        $('#delete-error').addClass('hidden').text('');
    });

    var redirectUri = '<@spring.url "/vaults/${vault.ID}"/>';
    var forms = [
        '#create-form',
        '#update-form',
        '#delete-form',
        '#transfer-form',
    ];

    for (var formIndex in forms) {
        var formSelector = forms[formIndex];
        $(formSelector).submit(function (event) {
            event.preventDefault();
            var form = $(this);
            var formData = form.serialize();
            var url = form.attr('action');
            form.find('.error').addClass('hidden');

            $.ajax({
                method: 'POST',
                url: url,
                data: formData,
                success: function (data) {
                    if (ErrorHandler.isForceLogoutResponse(data)) {
                        return ErrorHandler.handleForceLogoutResponse();
                    }
                    window.location.href = redirectUri;
                },
                error: function (xhr) {
                    ErrorHandler.handleAjaxError('#' + form.find('.error').attr('id'), xhr);
                }
            });
        });
    }

    $(document).ready(function () {
        var transferInputs = $('#transfer-inputs input');

        $('#orphan-checkbox').on('change', function (e) {
            var on = $(this).prop('checked');
            transferInputs.prop('disabled', on);
        });
    });

    $("#new-owner-name").autocomplete({
        autoFocus: true,
        appendTo: "#orphan-dialog",
        minLength: 2,
        source: function (request, response) {
            var term = request.term;
            $.ajax({
                url: '<@spring.url "/vaults/autocompleteuun/"/>' + term,
                type: 'GET',
                dataType: "json",
                success: function (data) {
                    response(data);
                },
                error: function(xhr) {
                    ErrorHandler.handleAjaxError('#orphan-dialog-error', xhr);
                }
            });
        },
        select: function (event, ui) {
            var attributes = ui.item.value.split(" - ");
            this.value = attributes[0];
            return false;
        }
    });

    $("#new-user-name").autocomplete({
        autoFocus: true,
        appendTo: "#add-new-dialog",
        minLength: 2,
        source: function (request, response) {
            var term = request.term;
            $.ajax({
                url: '<@spring.url "/vaults/autocompleteuun/"/>' + term,
                type: 'GET',
                dataType: "json",
                success: function (data) {
                    response(data);
                },
                error: function(xhr) {
                    ErrorHandler.handleAjaxError('#add-new-dialog-error', xhr);
                }
            });
        },
        select: function (event, ui) {
            var attributes = ui.item.value.split(" - ");
            this.value = attributes[0];
            return false;
        }
    });
</script>



