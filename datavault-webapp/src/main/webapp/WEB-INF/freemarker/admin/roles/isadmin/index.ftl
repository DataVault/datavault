<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />

    <style>
        #add-new {
            margin: 2em 0;
        }
        #role-assignments {
            padding: 0;
        }
        .action-column {
             width: 100px;
             text-align: center;
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
        .modal-dialog .btn > .fa {
            padding: 0 2px;
        }
        .control-label {
            font-weight: 400;
        }
    </style>

    <div id="add-new-dialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="add-new-user-title"
         aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form id="create-form" class="form form-horizontal" role="form"
                      action="${springMacroRequestContext.getContextPath()}/admin/role/op" method="post">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i
                                    class="fa fa-times" aria-hidden="true"></i></button>
                        <h4 class="modal-title" id="add-new-user-title">Add new user</h4>
                    </div>
                    <div class="modal-body">
                        <div id="create-error" class="alert alert-danger hidden" role="alert"></div>
                        <div class="form-group ui-widget">
                            <label for="add-superadmin-uun" class="control-label col-sm-3">UUN or Name</label>
                            <div class="col-sm-9">
                                <input id="add-superadmin-uun" type="text" class="form-control" name="op-id" value=""/>
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

    <div id="delete-dialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="delete-title"
         aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form id="delete-form" class="form form-horizontal" role="form"
                      action="${springMacroRequestContext.getContextPath()}/admin/roles/deop" method="post">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i
                                    class="fa fa-times" aria-hidden="true"></i></button>
                        <h4 class="modal-title" id="add-new-user-title">Delete User From IS Admin Role</h4>
                    </div>
                    <div class="modal-body">
                        <div id="delete-error" class="alert alert-danger hidden"></div>
                        <p>Do you want to delete <label id="delete-user-name"></label> from the <label id="delete-role-name"></label> role?</p>
                    </div>
                    <input type="hidden" id="delete-isadmin-user-id" name="deop-id"/>
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

    <div class="container">
        <ol class="breadcrumb">
            <li><a href="${springMacroRequestContext.getContextPath()}/admin"><b>Administration</b></a></li>
            <li><a href="${springMacroRequestContext.getContextPath()}/admin/roles"><b>Roles</b></a></li>
            <li class="active"><b>IS Admin Management</b></li>
        </ol>
        <h1>IS Admin Management</h1>
        <div id="add-new">
            <a href="#" data-toggle="modal" data-target="#add-new-dialog">+Add New User</a>
        </div>
        <div class="col-md-4" id="role-assignments">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th class="action-column">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list users as user>
                            <tr>
                                <td>${user.getFirstname()} ${user.getLastname()}</td>
                                <td class="action-column">
                                    <#if users?size gt 1>
                                        <a href="#" class="btn btn-default btn-delete" data-toggle="modal"
                                           data-target="#delete-dialog" data-role-name="${role.name}" data-user-id="${user.getID()}"
                                           data-user-name="${user.getFirstname()} ${user.getLastname()}" role="button"
                                           title="Delete role for ${user.getFirstname()} ${user.getLastname()}.">
                                            <i class="fa fa-trash"></i>
                                        </a>
                                    <#else>
                                        <a href="#" class="btn btn-default btn-delete" disabled="disabled" role="button"
                                           title="This user is the last ${role.name} and cannot be removed.">
                                            <i class="fa fa-trash"></i>
                                        </a>
                                    </#if>
                                </td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script>
        $('[data-target="#delete-dialog"]').click(function () {
            var roleName = $(this).data('role-name');
            var userId = $(this).data('user-id');
            var userName = $(this).data('user-name');
            $('#delete-isadmin-user-id').val(userId);
            $('#delete-role-name').text(roleName);
            $('#delete-user-name').text(userName);
            $('#delete-error').addClass('hidden').text('');
        });

        $('#delete-form').submit(function (event) {
            event.preventDefault();
            var formData = $(this).serialize();
            $.ajax({
                method: 'POST',
                url: '${springMacroRequestContext.getContextPath()}/admin/roles/deop',
                data: formData,
                success: function (data) {
                    if (ErrorHandler.isForceLogoutResponse(data)) {
                        return ErrorHandler.handleForceLogoutResponse();
                    }
                    window.location.href = '${springMacroRequestContext.getContextPath()}/admin/roles/isadmin';
                },
                error: function (xhr) {
                    ErrorHandler.handleAjaxError('#delete-error', xhr);
                }
            });
        });

        $('[data-target="#add-new-dialog"]').click(function () {
            $('#create-error').addClass('hidden').text('');
            $('#add-superadmin-uun').val('');
        });

        $('#create-form').submit(function (event) {
            event.preventDefault();
            var formData = $(this).serialize();
            $.ajax({
                method: 'POST',
                url: '${springMacroRequestContext.getContextPath()}/admin/roles/op/',
                data: formData,
                success: function (data) {
                    if (ErrorHandler.isForceLogoutResponse(data)) {
                        return ErrorHandler.handleForceLogoutResponse();
                    }
                    window.location.href = '${springMacroRequestContext.getContextPath()}/admin/roles/isadmin';
                },
                error: function (xhr) {
                    ErrorHandler.handleAjaxError('#create-error', xhr);
                }
            });
        });

        $("#add-superadmin-uun").autocomplete({
            autoFocus: true,
            appendTo: "#add-new-dialog",
            minLength: 2,
            source: function (request, response) {
                var term = request.term;
                $.ajax({
                    url: "${springMacroRequestContext.getContextPath()}/vaults/autocompleteuun/" + term,
                    type: 'GET',
                    dataType: "json",
                    success: function (data) {
                        response(data);
                    },
                    error: function(xhr) {
                        ErrorHandler.handleAjaxError('#create-error', xhr);
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

</@layout.vaultLayout>