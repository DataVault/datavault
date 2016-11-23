<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="modal fade" id="confirm-removal" tabindex="-1" role="dialog" aria-labelledby="confirmRemovalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
                <h4 class="modal-title" id="confirmRemovalLabel">Confirm removal of group ownership</h4>
            </div>
            <div class="modal-body">
                <p>Are you sure you want to remove <b class="remove-user"></b> as an owner of the <b class="remove-group"></b> group?</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger btn-ok">Remove</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="add-group-owner" tabindex="-1" role="dialog" aria-labelledby="addGroupOwnerLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
                <h4 class="modal-title" id="addGroupOwnerLabel">Add group owner</h4>
            </div>
            <div class="modal-body">
                <p>Add a new Owner to the <b class="add-owner-group"></b> group</p>
                <form id="add-group-owner-form">
                    <div class="form-group">
                        <label class="control-label">User:</label>
                        <br>
                        <select id='add-group-owner-user' class='user-select' data-live-search='true'>
                            <option selected disabled data-hidden="true">Please choose a user</option>
                            <#list users as user>
                                <option value="${user.getID()?js_string}" data-tokens="${user.getID()?js_string}">${user.firstname?html} ${user.lastname?html}</option>
                            </#list>
                        </select>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button form="add-group-owner-form" type="submit" class="btn btn-primary btn-ok">Add Owner</button>
            </div>
        </div>
    </div>
</div>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Groups</b></li>
    </ol>

    <#if groups?has_content>
        <div class="table-responsive">
            <table class="table table-striped">

                <thead>
                    <tr class="tr">
                        <th>Active</th>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Vaults</th>
                        <th>Owners</th>
                    </tr>
                </thead>

                <tbody>
                    <#assign counter = 0 >
                    <#list groups as group>
                        <tr class="tr">
                            <td>
                                <input type="checkbox" class="toggleGroupEnabled" data-group="${group.ID}" <#if group.getEnabled()>checked</#if>>
                            </td>
                            <td>${group.ID?html}</td>
                            <td>${group.name?html}
                                <form>
                                     <a class="btn btn-xs btn-danger <#if ((vaultCounts[counter] != 0) || (group.getOwners()?size != 0))> disabled</#if>" href="${springMacroRequestContext.getContextPath()}/admin/groups/${group.ID}/delete">
                                        <i class="fa fa-trash-o" aria-hidden="true"></i> Delete Group
                                    </a>
                                </form>
                            </td>
                            <td><span class="badge">${vaultCounts[counter]}</span></td>
                            <td>
                                <ul class="list-group">
                                    <#list group.getOwners() as user>
                                        <li class="list-group-item"><i class="fa fa-user" aria-hidden="true"></i> ${user.firstname?html} ${user.lastname?html} (${user.getID()?html})
                                            <a class="btn btn-xs btn-danger pull-right" href="#" data-user="${user.getID()}" data-group="${group.ID}" data-toggle="modal" data-target="#confirm-removal">
                                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span> Remove
                                            </a>
                                        </li>
                                   </#list>
                                </ul>
                                <form>
                                     <a class="btn btn-default" href="#" data-group="${group.ID}" data-toggle="modal" data-target="#add-group-owner">
                                        <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add Group Owner
                                    </a>
                                </form>
                            </td>
                        </tr>
                        <#assign counter = counter + 1>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

    <form>
        <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/admin/groups/create">
            <i class="fa fa-users" aria-hidden="true"></i> Create new Group
        </a>
    </form>

</div>

<script>
    // Bind properties to the user removal confirmation dialog
    $('#confirm-removal').on('show.bs.modal', function(e) {
        var data = $(e.relatedTarget).data();
        $('.remove-user', this).text(data.user);
        $('.remove-group', this).text(data.group);
        $('.btn-ok', this).data('user', data.user);
        $('.btn-ok', this).data('group', data.group);
    });
    
    // Bind OK button for user removal confirmation dialog
    $('#confirm-removal').on('click', '.btn-ok', function(e) {
        var $modalDiv = $(e.delegateTarget);
        var user = $(this).data('user');
        var group = $(this).data('group');
        
        $.ajax({
            url: '${springMacroRequestContext.getContextPath()}/admin/groups/' + group + '/users/' + user,
            type: 'DELETE',
            success: function(result) {
                $modalDiv.modal('hide');
                location.reload(true);
            }
        });
    });

    // Bind properties to the add group owner dialog
    $('#add-group-owner').on('show.bs.modal', function(e) {
        var data = $(e.relatedTarget).data();
        $('.add-owner-group', this).text(data.group);
        $('#add-group-owner-form').data('group', data.group);
        $('.user-select').val(-1);
        $('.user-select').selectpicker('refresh');
    });
    $('#add-group-owner').on('shown.bs.modal', function(e) {
        $('#add-group-owner-user').focus();
    });
    
    // Bind form for the add group owner dialog
    $('#add-group-owner-form').on('submit', function(e) {
        var user = $('#add-group-owner-user').val();
        var group = $(this).data('group');
        
        if (!user) {
            alert('Please choose a user');
        } else {
            $.ajax({
                url: '${springMacroRequestContext.getContextPath()}/admin/groups/' + group + '/users/' + user,
                type: 'PUT',
                success: function(result) {
                    $('#add-group-owner').modal('hide');
                    location.reload(true);
                },
                error: function(xhr, ajaxOptions, thrownError) {
                    alert('Error: unable to add new owner');
                }
            });
        }
        
        e.preventDefault();
    });

    // Add Spring Security CSRF header to ajax requests
    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function(e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });
    
    // Handle enable/disable groups
    $('.toggleGroupEnabled').click(function() {

        var command;
        var group = $(this).data('group');
        
        if ($(this).prop("checked")) {
            command = 'enable';
        } else {
            command = 'disable';
        }

        $.ajax({
            url: '${springMacroRequestContext.getContextPath()}/admin/groups/' + group + '/' + command,
            type: 'PUT',
            error: function(xhr, ajaxOptions, thrownError) {
                alert('Error: unable to update group');
            }
        });
    });

    $('.user-select').selectpicker();
    $('.enabled-select').selectpicker();

</script>

</@layout.vaultLayout>
