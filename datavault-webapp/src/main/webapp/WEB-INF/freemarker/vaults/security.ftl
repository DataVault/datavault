<#-- @ftlvariable name="vault" type="org.datavaultplatform.common.response.VaultInfo" -->
<style>
    #add-new {
        margin: 2em 0;
    }

    #role-assignments {
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


</style>

<#import "/spring.ftl" as spring />
<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />


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
                    <div id="create-error" class="alert alert-danger hidden" role="alert"></div>


                    <div class="col-sm-10 form-group ui-widget">
                        <label for="new-user-name" class="control-label form-label">New Data Owner:</label>
                        <div class="form-input" >
                            <input id="new-user-name" type="text" class="form-control" name="user" value=""/>
                        </div>
                    </div>


                    <div class="col-sm-10 form-group ui-widget">
                        <label for="new-user-role" class="control-label form-label">Vault role to assign to previous Data Owner:</label>
                        <div class="form-input">
                            <select id="new-user-role" name="role" class="form-control">
                                <#list roles as role>
                                    <option value="${role.id}">${role.name}</option>
                                </#list>
                            </select>
                        </div>
                    </div>

                    <div class="form-group ui-widget col-sm-10 form-confirm">
                        <div class="checkbox">
                            <input class="form-check-input" id="confirm-checkbox" type="checkbox" name="confirmed" />
                        </div>
                        <label for="confirm-checkbox" class="control-label">Don't assign self new role?</label>
                    </div>
                </div>
                <input type="hidden" id="submitAction" name="action" value="submit"/>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-ok">Save</button>
                </div>
            </form>
        </div>
    </div>
</div>

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
                    <h4 class="modal-title" id="add-new-user-title">Add new user</h4>
                </div>
                <div class="modal-body">
                    <div id="create-error" class="alert alert-danger hidden" role="alert"></div>
                    <div class="form-group ui-widget">
                        <label for="new-user-name" class="control-label col-sm-2">Name:</label>
                        <div class="col-sm-10">
                            <input id="new-user-name" type="text" class="form-control" name="user" value=""/>
                        </div>
                    </div>
                    <div class="form-group ui-widget">
                        <label for="new-user-role" class="control-label col-sm-2">Role:</label>
                        <div class="col-sm-10">
                            <select id="new-user-role" name="role" class="form-control">
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
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-ok">Save</button>
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
                    <h4 class="modal-title" id="update-existing-title">Edit user's role</h4>
                </div>
                <div class="modal-body">
                    <div id="update-error" class="alert alert-danger hidden" role="alert"></div>
                    <div class="form-group ui-widget">
                        <label for="role-update-user-name" class="control-label col-sm-2">Name:</label>
                        <div class="col-sm-10">
                            <input id="role-update-user-name" type="text" class="form-control"
                                   disabled="disabled"/>
                        </div>
                    </div>
                    <div class="form-group ui-widget">
                        <label for="update-user-role" class="control-label col-sm-2">Role:</label>
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
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-ok">Save</button>
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
                    <h4 class="modal-title" id="delete-title">Delete user</h4>
                </div>
                <div id="delete-error" class="alert alert-danger hidden" role="alert"></div>
                <div class="modal-body">
                    <label>Do you want to delete <span id="delete-role-user-name"></span> from this vault?</label>
                </div>
                <input type="hidden" id="delete-role-assignment-id" name="assignment"/>
                <input type="hidden" id="submitAction" name="action" value="submit"/>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-ok">Delete</button>
                </div>
            </form>
        </div>
    </div>
</div>


<div id="add-new">
    <a href="#" data-toggle="modal" data-target="#add-new-dialog">+ Add new user to vault</a>
</div>

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
                <th class="action-column">Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>${vault.userName}</td>
                <td class="role-column">Data Owner</td>
                <td class="action-column">
                    <a href="#" class="btn btn-default" data-toggle="modal"
                       data-target="#orphan-dialog"
                       data-user-name="${vault.userName}"
                       title="Transfer ownership of this vault."><i
                                class="fa fa-users"></i></a>
                </td>
            </tr>
            <#list roleAssignments as assignment>
                <tr>
                    <td>${assignment.user.firstname} ${assignment.user.lastname}</td>
                    <td class="role-column">${assignment.role.name}</td>
                    <td class="action-column">
                        <a href="#" class="btn btn-default" data-toggle="modal"
                           data-target="#update-existing-dialog" data-assignment-id="${assignment.id}"
                           data-user-name="${assignment.user.firstname} ${assignment.user.lastname}"
                           title="Edit role assignment for user ${assignment.user.firstname} ${assignment.user.lastname}."><i
                                    class="fa fa-pencil"></i></a>
                        <a href="#" class="btn btn-default btn-delete" data-toggle="modal"
                           data-target="#delete-dialog" data-assignment-id="${assignment.id}"
                           data-user-name="${assignment.user.firstname} ${assignment.user.lastname}"
                           title="Remove role assignment for user ${assignment.user.firstname} ${assignment.user.lastname}."><i
                                    class="fa fa-trash"></i></a>
                    </td>
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
            $(this).toggle(allValues.length === 0 || allValues.includes($(this).find('.role-column').text()));
        });
    });

    $('[data-target="#add-new-dialog"]').click(function () {
        $('#create-error').addClass('hidden').text('');
    });

    $('[data-target="#update-existing-dialog"]').click(function () {
        var assignmentId = $(this).data('assignment-id');
        var userName = $(this).data('user-name');
        $('#role-update-assignment-id').val(assignmentId);
        $('#role-update-user-name').val(userName);
        $('#update-error').addClass('hidden').text('');
    });

    $('[data-target="#delete-dialog"]').click(function () {
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

    for (var formSelector of forms) {
        $(formSelector).submit(function (event) {
            event.preventDefault();
            var form = $(this);
            var formData = form.serialize();
            var url = form.attr('action');

            $.ajax({
                method: 'POST',
                url: url,
                data: formData,
                success: function () {
                    window.location.href = redirectUri;
                },
                error: function (xhr) {
                    var error = form.children('.error').removeClass('hidden');
                    var text = xhr.status === 422 ? xhr.responseText : 'An error occurred. Please contact your system administrator.';

                    error.text(text);
                }
            });
        });
    }

    $("#new-user-name").autocomplete({
        autoFocus: true,
        appendTo: "#add-new-dialog",
        minLength: 2,
        source: function (request, response) {
            var term = request.term;
            $.ajax({
                url: '<@spring.url "/admin/vaults/autocompleteuun/"/>' + term,
                type: 'GET',
                dataType: "json",
                success: function (data) {
                    response(data);
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



