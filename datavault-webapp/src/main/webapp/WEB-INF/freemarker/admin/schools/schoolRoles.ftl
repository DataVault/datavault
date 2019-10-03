<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

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
        .role-definitions {
            border-left: 1px solid;
            padding: 0 1em;
        }
        .role-definitions .role-definition-title {
            margin-top: 0;
            font-weight: 500;
            color: #000;
        }
        .role-definitions .definition {
            margin: 0.5em 0;
        }
        .btn-delete {
            color: #b94a48;
            background-color: #f2dede;
        }
        .btn-cancel {
            background-color: #ededed;
        }
        .modal-title {
            font-weight: 500;
            color: #000;
        }
        .control-label {
            font-weight: 400;
        }
        .modal-dialog .btn > .fa {
            padding: 0 2px;
        }
        #role-update-user-name {
            padding: 7px 0 0 27px;
        }
    </style>

    <#if canManageSchoolRoleAssignments>

    <div id="add-new-dialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="add-new-user-title" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form id="create-form" class="form form-horizontal" role="form" action="${springMacroRequestContext.getContextPath()}/admin/schools/${school.getID()}/user" method="post">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
                        <h4 class="modal-title" id="add-new-user-title">Add New User</h4>
                    </div>
                    <div class="modal-body">
                        <div id="create-error" class="alert alert-danger hidden" role="alert"></div>
                        <div class="form-group ui-widget">
                            <label for="new-user-name" class="control-label col-sm-2">Name</label>
                            <div class="col-sm-10">
                                <input id="new-user-name" type="text" class="form-control" name="user" value=""/>
                            </div>
                        </div>
                        <div class="form-group ui-widget">
                            <label for="new-user-role" class="control-label col-sm-2">Role</label>
                            <div class="col-sm-10">
                                <select id="new-user-role" name="role" class="form-control">
                                    <option value="">Please select</option>
                                    <#list roles as role><option value="${role.id}">${role.name}</option></#list>
                                </select>
                            </div>
                        </div>
                    </div>
                    <input type="hidden" id="submitAction" name="action" value="submit"/>
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default btn-cancel" data-dismiss="modal"><i class="fa fa-times" aria-hidden="true"></i> Cancel</button>
                        <button type="submit" class="btn btn-primary btn-ok"><i class="fa fa-floppy-o" aria-hidden="true"></i> Save</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div id="update-existing-dialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="update-existing-title" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form id="update-form" class="form form-horizontal" role="form" action="${springMacroRequestContext.getContextPath()}/admin/schools/${school.getID()}/user/update" method="post">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
                        <h4 class="modal-title" id="update-existing-title">Edit User's Role</h4>
                    </div>
                    <div class="modal-body">
                        <div id="update-error" class="alert alert-danger hidden" role="alert"></div>
                        <div class="form-group ui-widget">
                            <label for="role-update-user-name" class="control-label col-sm-2">Name</label>
                            <label id="role-update-user-name"></label>
                        </div>
                        <div class="form-group ui-widget">
                            <label for="update-user-role" class="control-label col-sm-2">Role</label>
                            <div class="col-sm-10">
                                <select id="update-user-role" name="role" class="form-control">
                                    <#list roles as role><option value="${role.id}">${role.name}</option></#list>
                                </select>
                            </div>
                        </div>
                    </div>
                    <input type="hidden" id="role-update-assignment-id" name="assignment"/>
                    <input type="hidden" id="submitAction" name="action" value="submit"/>
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default btn-cancel" data-dismiss="modal"><i class="fa fa-times" aria-hidden="true"></i>Cancel</button>
                        <button type="submit" class="btn btn-primary btn-ok"><i class="fa fa-floppy-o" aria-hidden="true"></i>Save</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div id="delete-dialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="delete-title" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form id="delete-form" class="form form-horizontal" role="form" action="${springMacroRequestContext.getContextPath()}/admin/schools/${school.getID()}/user/delete" method="post">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
                        <h4 class="modal-title" id="delete-title">Delete User</h4>
                    </div>
                    <div id="delete-error" class="alert alert-danger hidden" role="alert"></div>
                    <div class="modal-body">
                        <span>Do you want to delete <label id="delete-role-user-name"></label> from this school?</span>
                    </div>
                    <input type="hidden" id="delete-role-assignment-id" name="assignment"/>
                    <input type="hidden" id="submitAction" name="action" value="submit"/>
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default btn-cancel" data-dismiss="modal"><i class="fa fa-times" aria-hidden="true"></i>Cancel</button>
                        <button type="submit" class="btn btn-danger btn-delete"><i class="fa fa-trash" aria-hidden="true"></i>Delete</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    </#if>

    <div class="container">

        <ol class="breadcrumb">
            <li><a href="${springMacroRequestContext.getContextPath()}/admin"><b>Administration</b></a></li>
            <li><a href="${springMacroRequestContext.getContextPath()}/admin/schools"><b>Schools</b></a></li>
            <li class="active"><b>${school.name}</b></li>
        </ol>

        <h1 id="role-assignments-title">${school.name}</h1>

        <div id="add-new">
            <#if canManageSchoolRoleAssignments>
            <a href="#" data-toggle="modal" data-target="#add-new-dialog">+ Add new user to school</a>
            </#if>
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
                                            <input type="checkbox" id="role-filter-${role.id}" name="role-filter" value="${role.name}" />
                                            <label for="role-filter-${role.id}">${role.name}</label>
                                        </div>
                                    </#list>
                                </div>
                            </th>
                            <#if canManageSchoolRoleAssignments>
                                <th class="action-column">Actions</th>
                            </#if>
                        </tr>
                    </thead>
                    <tbody>
                        <#list roleAssignments as assignment>
                            <tr>
                                <td>${assignment.userId}</td>
                                <td class="role-column">${assignment.role.name}</td>
                                <#if canManageSchoolRoleAssignments>
                                    <td class="action-column">
                                        <a href="#" class="btn btn-default" data-toggle="modal" data-target="#update-existing-dialog" data-assignment-id="${assignment.id}" data-user-name="${assignment.userId}" data-user-role="${assignment.role.id}" title="Edit role for ${assignment.userId}."><i class="fa fa-pencil"></i></a>
                                        <a href="#" class="btn btn-default btn-delete" data-toggle="modal" data-target="#delete-dialog" data-assignment-id="${assignment.id}" data-user-name="${assignment.userId}" title="Delete role for ${assignment.userId}."><i class="fa fa-trash"></i></a>
                                    </td>
                                </#if>
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

    </div>

    <script>
        $('#role-filter-toggle').click(function() {
            $('.filter-toggle-icon').toggleClass('fa-caret-down').toggleClass('fa-caret-up');
            $('#role-filter-panel').toggleClass('hidden');
        });
        $('[name="role-filter"]').on('change', function() {
            var allValues = [];
            $('[name="role-filter"]:checked').each(function() {
                allValues.push($(this).val());
            });
            $('#role-assignments table tbody tr').filter(function() {
                $(this).toggle(allValues.length === 0 || allValues.indexOf($(this).find('.role-column').text()) >= 0);
            });
        });

        <#if canManageSchoolRoleAssignments>

        $('[data-target="#add-new-dialog"]').click(function() {
            $('#create-error').addClass('hidden').text('');
            $('#new-user-name').val('');
            $('#new-user-role').val('');
        });
        $('[data-target="#update-existing-dialog"]').click(function() {
            var assignmentId = $(this).data('assignment-id');
            var userName = $(this).data('user-name');
            var role = $(this).data('user-role');
            $('#role-update-assignment-id').val(assignmentId);
            $('#update-user-role').val(role);
            $('#role-update-user-name').text(userName);
            $('#update-error').addClass('hidden').text('');
        });
        $('[data-target="#delete-dialog"]').click(function() {
            var assignmentId = $(this).data('assignment-id');
            var userName = $(this).data('user-name');
            $('#delete-role-assignment-id').val(assignmentId);
            $('#delete-role-user-name').text(userName);
            $('#delete-error').addClass('hidden').text('');
        });

        $('#create-form').submit(function(event) {
            event.preventDefault();
            var formData = $(this).serialize();
            $.ajax({
                method: 'POST',
                url: '${springMacroRequestContext.getContextPath()}/admin/schools/${school.getID()}/user',
                data: formData,
                success: function(data) {
                    if (ErrorHandler.isForceLogoutResponse(data)) {
                        return ErrorHandler.handleForceLogoutResponse();
                    }
                    window.location.href = '${springMacroRequestContext.getContextPath()}/admin/schools/${school.getID()}';
                },
                error: function(xhr) {
                    ErrorHandler.handleAjaxError('#create-error', xhr);
                }
            });
        });

        $('#update-form').submit(function(event) {
            event.preventDefault();
            var formData = $(this).serialize();
            $.ajax({
                method: 'POST',
                url: '${springMacroRequestContext.getContextPath()}/admin/schools/${school.getID()}/user/update',
                data: formData,
                success: function(data) {
                    if (ErrorHandler.isForceLogoutResponse(data)) {
                        return ErrorHandler.handleForceLogoutResponse();
                    }
                    window.location.href = '${springMacroRequestContext.getContextPath()}/admin/schools/${school.getID()}';
                },
                error: function(xhr) {
                    ErrorHandler.handleAjaxError('#update-error', xhr);
                }
            });
        });

        $('#delete-form').submit(function(event) {
            event.preventDefault();
            var formData = $(this).serialize();
            $.ajax({
                method: 'POST',
                url: '${springMacroRequestContext.getContextPath()}/admin/schools/${school.getID()}/user/delete',
                data: formData,
                success: function(data) {
                    if (ErrorHandler.isForceLogoutResponse(data)) {
                        return ErrorHandler.handleForceLogoutResponse();
                    }
                    window.location.href = '${springMacroRequestContext.getContextPath()}/admin/schools/${school.getID()}';
                },
                error: function(xhr) {
                    ErrorHandler.handleAjaxError('#delete-error', xhr);
                }
            });
        });

        $("#new-user-name").autocomplete({
            autoFocus: true,
            appendTo: "#add-new-dialog",
            minLength: 2,
            source: function(request, response) {
                var term = request.term;
                $.ajax({
                    url: "${springMacroRequestContext.getContextPath()}/vaults/autocompleteuun/"+term,
                    type: 'GET',
                    dataType: "json",
                    success: function( data ) {
                        response( data );
                    },
                    error: function(xhr) {
                        ErrorHandler.handleAjaxError('#create-error', xhr);
                    }
                });
            },
            select: function(event, ui) {
                var attributes = ui.item.value.split(" - ");
                this.value = attributes[0];
                return false;
            }
        });

        </#if>
    </script>

</@layout.vaultLayout>