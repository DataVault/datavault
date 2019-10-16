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
            padding: 0 2em 0 0;
        }
        #isadmin-button {
            float: left;
            margin-left: 2px;
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
        .modal-dialog textarea {
            max-width: 75%;
        }
        .modal-dialog select.form-control {
            width: 193px;
        }
        .modal-dialog .control-label {
            margin-top: 7px;
            font-weight: 400;
        }
        #modalnewedit .col-xs-9 {
            width: 75%;
        }
        #modalnewedit .row {
            margin-right: 0;
            padding-right: 0;
        }
        #permissions {
            margin-top: 20px;
        }
        #permissions .permission {
            padding-left: 0;
        }
        #permissions .permission-label {
            padding-left: 10px;
        }
    </style>

    <div id="modalnewedit" class="modal fade " role="dialog">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <form id="create-role" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/admin/roles/save" method="post">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
                        <h4 id="modal_title" class="modal-title">Add new role</h4>
                    </div>
                    <div class="modal-body">
                        <div id="create-error" class="alert alert-danger hidden"></div>
                        <div class="row">
                            <div class="col-xs-6">
                                <input type="hidden" name="id" id="edit_roleid">
                                <div class="form-group form-inline row">
                                    <label class="control-label col-xs-3" for="edit_name">Name</label>
                                    <input class="form-control col-xs-9" type="text" name="name" id="edit_name">
                                </div>
                                <div class="form-group form-inline row">
                                    <label class="control-label col-xs-3" for="edit_type">Type</label>
                                    <select class="form-control col-xs-9" name="type" id="edit_type">
                                        <option data-target="#drp-vault" value="VAULT">Vault role</option>
                                        <option data-target="#drp-school" value="SCHOOL">School role</option>
                                    </select>
                                </div>
                                <div class="form-group form-inline row">
                                    <label class="control-label col-xs-3" for="edit_status">Level</label>
                                    <input class="form-control col-xs-9" name="status" id="edit_status">
                                </div>
                            </div>
                            <div class="col-xs-6">
                                <div class="form-group form-inline row">
                                    <label class="control-label col-xs-3" for="edit_description">Description</label>
                                    <textarea class="form-control col-xs-9" name="description" id="edit_description"></textarea>
                                </div>
                            </div>
                        </div>
                        <div class="row col-xs-12" id="permissions">
                            <label>Permissions</label>
                            <div id="drp-vault">
                                <div id="edit_vaultpermissions"> </div>
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

    <div id="delete-dialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="delete-title" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form id="delete-form" class="form form-horizontal" role="form" action="${springMacroRequestContext.getContextPath()}/admin/roles/delete" method="post">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
                        <h4 id="modal_title" class="modal-title">Delete role</h4>
                    </div>
                    <div class="modal-body">
                        <div id="delete-error" class="alert alert-danger hidden"></div>
                        <div class="modal-body">
                            <p>Do you want to delete <label id="delete-role-name"></label> as a role in the system?</p>
                            <p>Note: You will not be able to delete a role before reassigning all its users to another existing role.</p>
                        </div>
                    </div>
                    <input type="hidden" id="delete-role-id" name="id"/>
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
            <li class="active"><b>Roles</b></li>
        </ol>

        <h1>Users & Roles</h1>

        <div id="add-new">
            <a id="addNewRoleBtn" href="#">+Add New Role</a>
        </div>

        <div id="role-assignments" class="col-md-6">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr>
                        <th>Role</th>
                        <th>No. of Users</th>
                        <th>Level</th>
                        <th class="action-column">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <#if isSuperAdmin>
                        <tr>
                            <td>${superAdminRole.getName()}</td>
                            <td>${superAdminRole.getAssignedUserCount()}</td>
                            <td>${superAdminRole.getStatus()}</td>
                            <td class="action-column">
                                <a id="isadmin-button" class="btn btn-default" href="${springMacroRequestContext.getContextPath()}/admin/roles/isadmin" title="Manage ${superAdminRole.name} users.">
                                    <i class="fa fa-users"></i>
                                </a>
                            </td>
                        </tr>
                    </#if>
                    <#list readOnlyRoles as role>
                        <tr>
                            <td>${role.getName()}</td>
                            <td>${role.getAssignedUserCount()}</td>
                            <td>${role.getStatus()}</td>
                            <td class="action-column">
                                <a href="#" class="btn btn-default" disabled="disabled" role="button" title="Cannot edit the ${role.name} role.">
                                    <i class="fa fa-pencil"></i>
                                </a>
                                <a href="#" class="btn btn-default btn-delete" disabled="disabled" role="button" title="Cannot delete the ${role.name} role.">
                                    <i class="fa fa-trash"></i>
                                </a>
                            </td>
                        </tr>
                    </#list>
                    <#list roles as role>
                        <tr>
                            <td>${role.getName()}</td>
                            <td>${role.getAssignedUserCount()}</td>
                            <td>${role.getStatus()}</td>
                            <td class="action-column">
                                <a href="#" class="btn btn-default editRoleButton" role="button" value="${role.getId()}" title="Edit the ${role.name} role.">
                                    <i class="fa fa-pencil"></i>
                                </a>
                                <a href="#" class="btn btn-default btn-delete" data-toggle="modal" data-target="#delete-dialog" data-role-id="${role.id}" data-role-name="${role.name}" role="button" title="Delete the ${role.name} role.">
                                    <i class="fa fa-trash-o"></i>
                                </a>
                            </td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script>

        var vaultPermissions = {};
        var schoolPermissions = {};

        $(".editRoleButton").click(function () {
            var roleid = this.getAttribute("value");
            $("#modal_title").html("Edit role ");
            EditByRoleId(roleid);
        });

        $('[data-target="#delete-dialog"]').click(function() {
            var roleId = $(this).data('role-id');
            var roleName = $(this).data('role-name');
            $('#delete-role-id').val(roleId);
            $('#delete-role-name').text(roleName);
            $('#delete-error').addClass('hidden').text('');
        });

        $('#create-role').submit(function(event) {
            event.preventDefault();
            var formData = $(this).serialize();
            $.ajax({
                method: 'POST',
                url: '${springMacroRequestContext.getContextPath()}/admin/roles/save',
                data: formData,
                success: function(data) {
                    if (ErrorHandler.isForceLogoutResponse(data)) {
                        return ErrorHandler.handleForceLogoutResponse();
                    }
                    window.location.href = '${springMacroRequestContext.getContextPath()}/admin/roles';
                },
                error: function(xhr) {
                    ErrorHandler.handleAjaxError('#create-error', xhr);
                }
            });
        });

        $('#delete-form').submit(function(event) {
            event.preventDefault();
            var formData = $(this).serialize();
            $.ajax({
                method: 'POST',
                url: '${springMacroRequestContext.getContextPath()}/admin/roles/delete',
                data: formData,
                success: function(data) {
                    if (ErrorHandler.isForceLogoutResponse(data)) {
                        return ErrorHandler.handleForceLogoutResponse();
                    }
                    window.location.href = '${springMacroRequestContext.getContextPath()}/admin/roles';
                },
                error: function(xhr) {
                    ErrorHandler.handleAjaxError('#delete-error', xhr);
                }
            });
        });

        function getPermissions() {

            $.ajax({
                method: "GET",
                url: '${springMacroRequestContext.getContextPath()}/admin/roles/getvaultpermissions',
                success: function (data) {
                    if (ErrorHandler.isForceLogoutResponse(data)) {
                        return ErrorHandler.handleForceLogoutResponse();
                    }
                    vaultPermissions = data;
                },
                error: function (xhr) {
                    ErrorHandler.handleAjaxError('#create-error', xhr);
                }
            });

            $.ajax({
                method: "GET",
                url: '${springMacroRequestContext.getContextPath()}/admin/roles/getschoolpermissions',
                success: function (data) {
                    if (ErrorHandler.isForceLogoutResponse(data)) {
                        return ErrorHandler.handleForceLogoutResponse();
                    }
                    schoolPermissions = data;
                },
                error: function (xhr) {
                    ErrorHandler.handleAjaxError('#create-error', xhr);
                }
            });
        }

        function EditByRoleId(roleid) {

            getPermissions();

            $.ajax({
                method: "GET",
                url: '${springMacroRequestContext.getContextPath()}/admin/roles/' + roleid,
                success: function (data) {

                    if (ErrorHandler.isForceLogoutResponse(data)) {
                        return ErrorHandler.handleForceLogoutResponse();
                    }

                    $('#create-error').addClass('hidden').text('');
                    $("#modalnewedit").modal("show");

                    $("#edit_roleid").val(data.role.id);
                    $("#edit_name").val(data.role.name);
                    $("#edit_description").val(data.role.description);
                    $("#edit_status").val(data.role.status);
                    $("#edit_type").val(data.role.type);

                    var permissions = data.role.permissions;
                    permissions = permissions.concat(data.unsetPermissions);
                    permissions.sort(function(p1, p2) {
                        if (p1.label < p2.label) {
                            return -1;
                        }
                        if (p1.label > p2.label) {
                            return 1;
                        }
                        return 0;
                    });

                    $("#edit_vaultpermissions").empty();
                    permissions.forEach(function(p) {
                        setPermissionsHtml(p, data.role.permissions.indexOf(p) >= 0, "edit_vaultpermissions")
                    });

                },
                error: function (xhr) {
                    ErrorHandler.handleAjaxError('#create-error', xhr);
                }
            });
        }

        function setPermissionsHtml(permission, checked, target) {
            var outdiv = document.createElement("div");
            outdiv.classList.add("col-xs-6");
            outdiv.classList.add("form-group");
            outdiv.classList.add("form-inline");
            outdiv.classList.add("permission");
            var indiv = document.createElement("div");
            indiv.classList.add("checkbox");
            var label = document.createElement("label");
            label.innerText = " " + permission.label;
            label.classList.add("permission-label");
            label.setAttribute("for", permission.id);
            var input = document.createElement("input");
            input.setAttribute("name", "permissions");
            input.setAttribute("type", "checkbox");
            input.setAttribute("id", permission.id);
            input.setAttribute("value", permission.id);
            input.classList.add("permission-checkbox");
            input.checked = checked;

            indiv.appendChild(input);
            indiv.appendChild(label);
            outdiv.appendChild(indiv);

            document.getElementById(target).appendChild(outdiv);
        }

        $("#addNewRoleBtn").click(function () {

            $("#modal_title").html("Add new role");

            $('#create-error').addClass('hidden').text('');
            getPermissions();

            $("#modalnewedit").modal("show");

            $("#edit_roleid").val(0);
            $("#edit_name").val("");
            $("#edit_description").val("");
            $("#edit_status").val("");

            var type = "VAULT";
            $("#edit_type").val(type);
            changePermissions(type);

        });

        $("#edit_type").change(function () {

            var type = this.value;
            changePermissions(type);
        });

        function changePermissions(type) {
            $("#edit_vaultpermissions").empty();

            var permissions = type === "SCHOOL" ? schoolPermissions : vaultPermissions;
            permissions.forEach(function (p) {
                setPermissionsHtml(p, false, "edit_vaultpermissions");
            });

        }

        $(function () {
            getPermissions();
        });

        // Add Spring Security CSRF header to ajax requests
        $(function () {
            var token = $("meta[name='_csrf']").attr("content");
            var header = $("meta[name='_csrf_header']").attr("content");
            $(document).ajaxSend(function (e, xhr, options) {
                xhr.setRequestHeader(header, token);
            });
        });
    </script>
</@layout.vaultLayout>