<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>
<#import "/spring.ftl" as spring />

    <div class="container container_layout">
        <div>
            <div class="row">
                <div class="col-sm-4">
                    <h1 class="layout_roles_title">Users & Roles</h1>
                </div>
            <div class="col-sm-8">
                <!-- todo buttons -->
            </div>
        </div>

        <div class="layout_misc__add_new">
            <a id="addNewRoleBtn" href="#">+Add New Role</a>
        </div>

        <#if errormessage?has_content>
            <div class="alert alert-danger">
                ${errormessage}
            </div>
        </#if>

        <#if roles?has_content>
            <table class="table_layout">
                <thead>
                    <tr class="tr">
                        <th class="table_layout__title">Role</th>
                        <th class="table_layout__count"> No. of Users</th>
                        <th class="table_layout__actions">Actions</th>
                    </tr>
                </thead>
                <tbody>
                <#list roles as role>
                    <tr class="tr">
                        <td>${role.getName()}</td>
                        <td>${role.getAssignedUserCount()}</td>
                        <td>
                            <a href="#" class="btn btn-link editRoleButton modal_layout_action_btn" role="button" value="${role.getId()}">
                                <i class="fa fa-pencil" style="font-size:24px;color white;padding:2px;border:solid 1px black;"></i> </span>
                            </a>
                            <a href="#" class="btn btn-link modal_layout_action_btn" data-toggle="modal" data-target="#delete-dialog" data-role-id="${role.id}" data-role-name="${role.name}" role="button">
                                <i class="fa fa-trash-o" style="font-size:24px;color:red;background:MistyRose;border:1px solid black;padding:2px"></i>
                            </a>
                        </td>
                    </tr>
                </#list>
                </tbody>
            </table>
        <#else>
            <h2>There are no roles</h2>
        </#if>

        <div id="modalnewedit" class="modal model-lg fade " role="dialog">
            <div class="modal-dialog  modal_layout_roles">
                <div class="modal-content ">
                    <div class="modal-header modal_layout_roles_modal-header">
                        <button type="button" class="close  modal_layout_roles_modal-header-close" data-dismiss="modal">
                            <span class="glyphicon glyphicon-remove-circle  modal_layout_roles_modal-header-close"></span>
                        </button>
                        <h3 id="modal_title" class="layout_roles_title">Add new role</h3>
                    </div>
                    <div class="modal-body modal_layout_roles_modal-body">
                        <div id="modalMsg" class="alert  alert-danger" role="alert">
                            all OK
                        </div>
                        <form id="create-role" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/admin/roles/save" method="post">
                            <div class="row">
                                <div class="col-xs-6">
                                    <input type="hidden" name="id" id="edit_roleid">
                                    <div class="form-group form-inline">
                                        <label class="modal_layout_roles__labels" for="edit_name">Name</label>
                                        <input class="modal_layout_roles__inputs form-control" type="text" name="name" id="edit_name">
                                    </div>
                                    <div class="form-group form-inline">
                                        <label class="modal_layout_roles__labels" for="edit_type">Type</label>
                                        <select class="layout_select_source form-control modal_layout_roles__inputs" placeholder="please select" name="type" id="edit_type">
                                            <option data-target="#drp-vault" value="VAULT">Vault role</option>
                                            <option data-target="#drp-school" value="SCHOOL">School role</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="col-xs-6 form-group form-inline">
                                    <label class="modal_layout_roles__labels" for="edit_description">Description</label>
                                    <textarea class="form-control modal_layout_roles__inputs__textarea" name="description" id="edit_description"></textarea>
                                </div>
                            </div>
                            <div class="row modal_layout_roles__permissions">
                                <p>Permissions</p>
                                <div class="layout_select_target" id="drp-vault">
                                    <div id="edit_vaultpermissions"> </div>
                                </div>
                            </div>
                            <input type="hidden" id="submitAction" name="action" value="submit"/>
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <div class="modal_layout_roles_modal-footer">
                                <button type="button" class="btn btn-basic" data-dismiss="modal"><span class="glyphicon glyphicon-remove-circle"></span> Close</button>
                                <button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-floppy-disk"></span> Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <div id="delete-dialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="delete-title" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <form class="form form-horizontal" role="form" action="${springMacroRequestContext.getContextPath()}/admin/roles/delete" method="post">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                <span class="glyphicon glyphicon-remove-circle  modal_layout_roles_modal-header-close"></span>
                            </button>
                            <h3 class="modal-title" id="delete-title">Delete Role</h3>
                        </div>
                        <div class="modal-body">
                            <label>Are you sure you want to delete role <span id="delete-role-name"></span>?</label>
                        </div>
                        <input type="hidden" id="delete-role-id" name="id"/>
                        <input type="hidden" id="submitAction" name="action" value="submit"/>
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <div class="modal_layout_roles_modal-footer">
                            <button type="button" class="btn btn-basic" data-dismiss="modal"><span class="glyphicon glyphicon-remove-circle"></span> Close</button>
                            <button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-floppy-disk"></span> Save</button>
                        </div>
                    </form>
                </div>
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
        });

        function getPermissions() {

            $.ajax({
                method: "GET",
                url: '${springMacroRequestContext.getContextPath()}/admin/roles/getvaultpermissions',
                success: function (data) {
                    vaultPermissions = data;

                },
                error: function (xhr, ajaxOptions, thrownError) {
                    $("#modalMsg").show();
                    $("#modalMsg").html("<strong>Error Fetching School Permissions: </strong> " + thrownError);
                }
            });

            $.ajax({
                method: "GET",
                url: '${springMacroRequestContext.getContextPath()}/admin/roles/getschoolpermissions',
                success: function (data) {
                    schoolPermissions = data;

                },
                error: function (xhr, ajaxOptions, thrownError) {
                    $("#modalMsg").show();
                    $("#modalMsg").html("<strong>Error Fetching School Permissions: </strong> " + thrownError);
                }
            });
        }

        function EditByRoleId(roleid) {

            $("#modalMsg").hide();
            getPermissions();

            $.ajax({
                method: "GET",
                url: '${springMacroRequestContext.getContextPath()}/admin/roles/' + roleid,
                success: function (data) {

                    $("model_msg").innerText = "";

                    $("#modalnewedit").modal("show");

                    $("#edit_roleid").val(data.role.id);
                    $("#edit_name").val(data.role.name);
                    $("#edit_description").val(data.role.description);
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
                        setPermissionsHtml(p, data.role.permissions.includes(p), "edit_vaultpermissions")
                    });

                },
                error: function (xhr, ajaxOptions, thrownError) {
                    $("#modalMsg").show();
                    $("#modalMsg").html("<strong>Error Fetching Role: </strong> " + thrownError);
                }
            });
        }

        $("#modalSave").click(function () {
            $("#modalMsg").hide();

            if (!$("#edit_name").val()) {
                $("#modalMsg").show();
                $("#modalMsg").html("Need role name to save");
                return;
            }

            if ($("[name='permisionmodel']").filter(':checked').length == 0) {
                $("#modalMsg").show();
                $("#modalMsg").html("Need at least One permission selected");
                return;
            }

            saveform();
        });

        function saveform() {
            var formdata = {
                id: $("#edit_roleid").val(),
                name: $("#edit_name").val(),
                description: $("#edit_description").val(),
                type: $("#edit_type").val(),
                permissions: []

            };

            $("[name='permisionmodel']").filter(':checked').each(function (i) {
                formdata.permissions.push(this.id);
            });
        }

        function setPermissionsHtml(permission, checked, target) {
            var outdiv = document.createElement("div");
            outdiv.classList.add("col-xs-6");
            outdiv.classList.add("form-group");
            outdiv.classList.add("form-inline");
            var indiv = document.createElement("div");
            indiv.classList.add("checkbox");
            var label = document.createElement("label");
            label.innerText = " " + permission.label;
            label.classList.add("model_layout_permission_label");
            label.setAttribute("for", permission.id);
            var input = document.createElement("input");
            input.setAttribute("name", "permissions");
            input.setAttribute("type", "checkbox");
            input.setAttribute("id", permission.id);
            input.setAttribute("value", permission.id);
            input.checked = checked;

            indiv.appendChild(input);
            indiv.appendChild(label);
            outdiv.appendChild(indiv);

            document.getElementById(target).appendChild(outdiv);
        }

        $("#addNewRoleBtn").click(function () {

            $("#modal_title").html("Add new role");

            $("#modalMsg").hide();
            getPermissions();

            $("#modalnewedit").modal("show");

            $("#edit_roleid").val(0);
            $("#edit_name").val("");
            $("#edit_description").val("");

            var type = "VAULT";
            $("#edit_type").val = type;
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