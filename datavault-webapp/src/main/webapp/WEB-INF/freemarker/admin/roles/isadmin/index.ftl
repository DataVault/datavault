<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />

    <div class="container container_layout">
    <div>
        <div class="row">
            <div class="col-sm-4">
                <h1 class="layout_roles_title">IS Admin Management</h1>
            </div>
            <div class="col-sm-8">
                <!-- todo buttons (or not?) -->
            </div>
        </div>

        <div class="row">
            <div class="col-sm-4">
                <a class="layout_misc__subpage_link" href="${springMacroRequestContext.getContextPath()}/admin/roles">Back
                    to role management</a>
            </div>
        </div>

        <div class="layout_misc__add_new">
            <a id="addNewAdminBtn" href="#" data-toggle="modal" data-target="#add-new-dialog">+Add New User</a>
        </div>

        <table class="table_layout">
            <thead>
            <tr class="tr">
                <th class="table_layout__title">Name</th>
                <th class="table_layout__actions">Actions</th>
            </tr>
            </thead>
            <tbody>
            <#list users as user>
                <tr class="tr">
                    <td>${user.getFirstname()} ${user.getLastname()}</td>
                    <td>
                        <#if users?size gt 1>
                            <a href="#" class="btn btn-link modal_layout_action_btn" data-toggle="modal"
                               data-target="#delete-dialog" data-role-name="${role.name}" data-user-id="${user.getID()}"
                               data-user-name="${user.getFirstname()} ${user.getLastname()}" role="button"
                               title="Remove user ${user.getFirstname()} ${user.getLastname()} from the ${role.name} role.">
                                <i class="fa fa-trash-o"
                                   style="font-size:24px;color:red;background:MistyRose;border:1px solid black;padding:2px"></i>
                            </a>
                        <#else>
                            <a href="#" class="btn btn-link modal_layout_action_btn" disabled="disabled" role="button"
                               title="This user is the last IS Admin and cannot be removed.">
                                <i class="fa fa-trash-o"
                                   style="font-size:24px;color:red;background:MistyRose;border:1px solid black;padding:2px"></i>
                            </a>
                        </#if>
                    </td>
                </tr>
            </#list>
            </tbody>
        </table>

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
                                <label for="user-name" class="control-label col-sm-2">Name:</label>
                                <div class="col-sm-10">
                                    <input id="user-name" type="text" class="form-control" name="user" value=""/>
                                </div>
                            </div>
                        </div>
                        <#-- When Autocomplete comes around, hide this and set value from autocomplete events -->
                        <div class="form-group ui-widget">
                            <label for="op-user-id" class="control-label col-sm-2">For testing (will be hidden
                                later):</label>
                            <div class="col-sm-10">
                                <input type="text" id="op-user-id" class="form-control" name="op-id" value=""/>
                            </div>
                        </div>
                        <#-- --- -->
                        <input type="hidden" id="submitAction" name="action" value="submit"/>
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                            <button type="submit" class="btn btn-primary btn-ok">Add</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div id="delete-dialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="delete-title"
             aria-hidden="true">
            <div class="modal-dialog modal_layout_roles">
                <div class="modal-content">
                    <div class="modal-header modal_layout_roles_modal-header">
                        <button type="button" class="close  modal_layout_roles_modal-header-close" data-dismiss="modal">
                            <span class="glyphicon glyphicon-remove-circle  modal_layout_roles_modal-header-close"></span>
                        </button>
                        <h3 id="modal_title" class="layout_roles_title">Remove user from IS Admin role</h3>
                    </div>
                    <div class="modal-body modal_layout_roles_modal-body">
                        <form id="delete-form" class="form form-horizontal" role="form"
                              action="${springMacroRequestContext.getContextPath()}/admin/roles/deop" method="post">
                            <div id="delete-error" class="alert alert-danger hidden"></div>
                            <div class="modal-body">
                                <p>Do you want to remove <label id="delete-user-name"></label> from the <label
                                            id="delete-role-name"></label> role?</p>
                            </div>
                            <input type="hidden" id="delete-isadmin-user-id" name="deop-id"/>
                            <input type="hidden" id="submitAction" name="action" value="submit"/>
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <div class="modal_layout_roles_modal-footer">
                                <button type="button" class="btn btn-basic" data-dismiss="modal"><span
                                            class="glyphicon glyphicon-remove-circle"></span> Cancel
                                </button>
                                <button type="submit" class="btn btn-primary"><i class="fa fa-trash"></i> Remove
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
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
                success: function () {
                    window.location.href = '${springMacroRequestContext.getContextPath()}/admin/roles/isadmin';
                },
                error: function (xhr) {
                    $('#delete-error').removeClass('hidden').text(xhr.responseText);
                }
            });
        });

        $('[data-target="#add-new-dialog"]').click(function () {
            $('#create-error').addClass('hidden').text('');
        });

        $('#create-form').submit(function (event) {
            event.preventDefault();
            var formData = $(this).serialize();
            $.ajax({
                method: 'POST',
                url: '${springMacroRequestContext.getContextPath()}/admin/roles/op/',
                data: formData,
                success: function () {
                    window.location.href = '${springMacroRequestContext.getContextPath()}/admin/roles/isadmin';
                },
                error: function (xhr) {
                    var $error = $('#create-error').removeClass('hidden').text(xhr.responseText);
                }
            });
        });


        $("#user-name").autocomplete({
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
                    }
                });
            },
            select: function (event, ui) {
                var attributes = ui.item.value.split(" - ");
                this.value = attributes[0];
                console.log("Autocomplete selected:");
                console.log(event);
                console.log(ui);
                console.warn("When this actually works, then set the user id hidden field by selection data");
                return false;
            }
        });
    </script>

</@layout.vaultLayout>