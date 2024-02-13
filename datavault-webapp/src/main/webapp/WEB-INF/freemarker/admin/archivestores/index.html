<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="modal fade" id="confirm-removal" tabindex="-1" role="dialog" aria-labelledby="confirmRemovalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="confirmRemovalLabel">Confirm removal of archivestore</h4>
            </div>
            <div class="modal-body">
                <p>Are you sure you want to remove <b class="remove-archivestore"></b>?</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger btn-ok" id="remove">Remove</button>
            </div>
        </div>
    </div>
</div>


<div class="modal fade" id="add-archivestoreLocal" tabindex="-1" role="dialog" aria-labelledby="addArchivestoreLocalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="addArchiveStoreLocalLabel">Add archivestore</h4>
            </div>
            <div class="modal-body">
                <form id="add-archivestoreLocal-form">
                    <div class="form-archivestore">
                        <div class="form-group">
                            <label for="label">Label</label>
                            <input type="text" class="form-control" id="label" name="label"/>
                        </div>
                        <div class="form-group">
                            <label for="type">Type</label>
                            <select class="form-control" id="type" name="type">
                                <option value="AmazonGlacier">Amazon Glacier</option>
                                <option value="DropboxFileSystem">Dropbox File System</option>
                                <option value="LocalFileSystem">Local File System</option>
                                <option value="MultiLocalFileSystem">Multi Local File System</option>
                                <option value="OracleObjectStorageClassic">Oracle Object Storage Classic</option>
                                <option value="S3Cloud">S3 Cloud</option>
                                <option value="SFTPFileSystem">SFTP File System</option>
                                <option value="TrivolyStorageManager">Trivoly Storage Manager</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <input type="checkbox" id="retrieve" name="retrieve"/> Enable Retrieve
                        </div>
                        <div class="form-group">
                            <label for="path">Path</label>
                            <textarea class="form-control" id="properties" name="properties" rows="3"></textarea>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button form="add-archivestoreLocal-form" type="submit" class="btn btn-primary">Submit</button>
            </div>
        </div>
    </div>
</div>



<div class="container">

    <ol class="breadcrumb">
        <li class="active"><b>Archive Storage Options</b></li>
    </ol>

    <h3>Storage</h3>

    <div class="table-responsive storage-table">
        <#if archivestores?has_content>
        <table class="table table-striped">

            <thead>
            <tr class="tr">
                <th>Label</th>
                <th>Class</th>
                <th>Properties</th>
                <th>Retrieve Enabled</th>
                <th></th>
            </tr>
            </thead>

            <tbody id="fileStoresLocal">
                    <#list archivestores as archivestore>
                    <tr class="tr">
                        <td>${archivestore.label}</td>
                        <td>${archivestore.storageClass}</td>
                        <td>
                            <textarea id="${archivestore.ID}-properties" class="form-control" name="${archivestore.ID}-properties" rows="3"><#list archivestore.properties?keys as prop>${prop}=${archivestore.properties[prop]}
                                </#list>
                            </textarea>
                            <a class="update-properties-btn btn btn-default btn-sm pull-right" href="#" data-archivestore="${archivestore.ID}">Update</a>
                        </td>
                        <td>
                            <input type="checkbox" class="toggleRetrieveEnabled" data-archivestore1="${archivestore.ID}" ${archivestore.retrieveEnabled?then("checked", "")}>
                        </td>
                        <td>
                            <a class="btn btn-xs btn-danger pull-right" href="#" data-archivestore="${archivestore.ID}" data-toggle="modal" data-target="#confirm-removal">
                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span> Remove
                            </a>
                        </td>
                    </tr>
                    </#list>
            </tbody>
        </table>

        <#else>
            <h4>There are currently no Local Archivestores configured</h4>
        </#if>

        <form>
            <a class="btn btn-default" href="#" data-toggle="modal" data-target="#add-archivestoreLocal">
                <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add Archivestore
            </a>
        </form>

    </div>


    <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/">Continue</a>
    </div>

<script>

    var localFrm = $('#add-archivestoreLocal-form');
    localFrm.submit(function (ev) {
        $.ajax({
            method: "POST",
            url: '${springMacroRequestContext.getContextPath()}/admin/archivestores/local',
            data: localFrm.serialize(),
            success: function (data) {
                location.reload(true);
            },
            error: function(xhr, ajaxOptions, thrownError) {
                alert("Error - unable to add new archivestore:\n" + thrownError);
            }
        });

        ev.preventDefault();
    });

    // Handle properties update
    $('.update-properties-btn').click(function() {
        var archivestore = $(this).data('archivestore');

        var properties = $("#"+archivestore+"-properties").val();

        $.ajax({
            url: '${springMacroRequestContext.getContextPath()}/admin/archivestores/' + archivestore + '/update/properties',
            type: 'POST',
            data: { properties : properties },
            error: function(xhr, ajaxOptions, thrownError) {
                alert('Error: unable to update archivestore');
            }
        });
    });

    // Bind properties to the user removal confirmation dialog
    $('#confirm-removal').on('show.bs.modal', function(e) {
        var data = $(e.relatedTarget).data();
        $('.remove-archivestore', this).text(data.archivestore);
        // todo: should this be archivestore id?
        $('#remove', this).data('archivestoreId', data.archivestore);

    });

    $("button#remove").click(function(){
        var archivestoreId = $(this).data('archivestoreId');
        $.ajax({
            method: "DELETE",
            url: '${springMacroRequestContext.getContextPath()}/admin/archivestores/' + archivestoreId,
            success: function (data) {
                location.reload(true);
            },
            error: function(xhr, ajaxOptions, thrownError) {
                alert('Error: unable to delete archivestore');
            }
        });

        ev.preventDefault();
    });

    // Handle enable/disable retrieval
    $('.toggleRetrieveEnabled').click(function() {
        var command;
        var archivestore1 = $(this).data('archivestore1');

        if ($(this).prop("checked")) {
            command = 'enable';
        } else {
            command = 'disable';
        }

        $.ajax({
            url: '${springMacroRequestContext.getContextPath()}/admin/archivestores/' + archivestore1 + '/' + command,
            type: 'POST',
            error: function(xhr, ajaxOptions, thrownError) {
                alert('Error: unable to update archivestore');
            }
        });
    });


    // Add Spring Security CSRF header to ajax requests
    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function(e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });


</script>

</@layout.vaultLayout>
