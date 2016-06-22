<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="user">
<@layout.vaultLayout>

<div class="modal fade" id="confirm-removal" tabindex="-1" role="dialog" aria-labelledby="confirmRemovalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="confirmRemovalLabel">Confirm removal of filestore</h4>
            </div>
            <div class="modal-body">
                <p>Are you sure you want to remove <b class="remove-filestore"></b>?</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger btn-ok" id="remove">Remove</button>
            </div>
        </div>
    </div>
</div>


<div class="modal fade" id="add-filestoreSFTP" tabindex="-1" role="dialog" aria-labelledby="addFilestoreSFTPLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="addFileStoreSFTPLabel">Add filestore</h4>
            </div>
            <div class="modal-body">
                <p>Enter new filestore details here</p>
                <form id="add-filestoreSFTP-form">
                    <div class="form-filestore">
                        <div class="form-group">
                            <label for="hostname">Hostname</label>
                            <input type="text" class="form-control" id="hostname" name="hostname" placeholder="Enter hostname"/>
                        </div>
                        <div class="form-group">
                            <label for="port">Port</label>
                            <input type="text" class="form-control" id="port" name="port" placeholder="Enter port"/>
                        </div>
                        <div class="form-group">
                            <label for="path">Path</label>
                            <input type="text" class="form-control" id="path" name="path" placeholder="Enter path"/>
                        </div>

                        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-default">Submit</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="add-filestoreLocal" tabindex="-1" role="dialog" aria-labelledby="addFilestoreLocalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="addFileStoreLocalLabel">Add filestore</h4>
            </div>
            <div class="modal-body">
                <p>Enter new filestore details here</p>
                <form id="add-filestoreLocal-form">
                    <div class="form-filestore">
                        <div class="form-group">
                            <label for="path">Path</label>
                            <input type="text" class="form-control" id="path" name="path" value="${activeDir}" readonly />
                        </div>

                        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-default">Submit</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>



<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/filestores"><b>Storage Options</b></a></li>
    </ol>

    <h3>Local Storage</h3>

    <div class="table-responsive">
        <table class="table table-striped">

            <thead>
            <tr class="tr">
                <th>Hostname</th>
                <th>Path</th>
                <th>action</th>
            </tr>
            </thead>

            <tbody id="fileStoresLocal">
                <#if filestoresLocal?has_content>
                    <#list filestoresLocal as filestoreLocal>
                    <tr class="tr">
                        <td>localhost</td>
                        <td>${filestoreLocal.properties['rootPath']}</td>
                        <td>
                            <a class="btn btn-xs btn-danger pull-right" href="#" data-filestore="${filestoreLocal.ID}" data-toggle="modal" data-target="#confirm-removal">
                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span> Remove
                            </a>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>

        <form>
            <a class="btn btn-default" href="#" data-toggle="modal" data-target="#add-filestoreLocal">
                <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add Filestore
            </a>
        </form>

    </div>


    <h3>SFTP Storage</h3>

    <div class="table-responsive">
        <table class="table table-striped">

            <thead>
            <tr class="tr">
                <th>Hostname</th>
                <th>Port</th>
                <th>Path</th>
                <th>Public key</th>
                <th>action</th>
            </tr>
            </thead>

            <tbody id="fileStoresSFTP">
                <#if filestoresSFTP?has_content>
                    <#list filestoresSFTP as filestoreSFTP>
                    <tr class="tr">
                        <td>${filestoreSFTP.properties['host']}</td>
                        <td>${filestoreSFTP.properties['port']}</td>
                        <td>${filestoreSFTP.properties['rootPath']}</td>
                        <td>${filestoreSFTP.properties['publicKey']}</td>
                        <td>
                            <a class="btn btn-xs btn-danger pull-right" href="#" data-filestore="${filestoreSFTP.ID}" data-toggle="modal" data-target="#confirm-removal">
                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span> Remove
                            </a>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>

        <form>
            <a class="btn btn-default" href="#" data-toggle="modal" data-target="#add-filestoreSFTP">
                <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add Filestore
            </a>
        </form>

    </div>
</div>

<script>

    var localFrm = $('#add-filestoreLocal-form');
    localFrm.submit(function (ev) {
        $.ajax({
            method: "POST",
            url: '${springMacroRequestContext.getContextPath()}/filestores/local',
            data: localFrm.serialize(),
            success: function (data) {
                location.reload(true);
            }

            //error: function(xhr, ajaxOptions, thrownError) {
            //    alert('Error: unable to add new filestore');
            //}
        });

        ev.preventDefault();
    });


    var sftpFrm = $('#add-filestoreSFTP-form');
    sftpFrm.submit(function (ev) {
        $.ajax({
            method: "POST",
            url: '${springMacroRequestContext.getContextPath()}/filestores/sftp',
            data: sftpFrm.serialize(),
            success: function (data) {
                location.reload(true);
            }

            //error: function(xhr, ajaxOptions, thrownError) {
            //    alert('Error: unable to add new filestore');
            //}
        });

        ev.preventDefault();
    });


    // Bind properties to the user removal confirmation dialog
    $('#confirm-removal').on('show.bs.modal', function(e) {
        var data = $(e.relatedTarget).data();
        $('.remove-filestore', this).text(data.filestore);
        // todo: should this be filestore id?
        $('#remove', this).data('filestoreId', data.filestore);

    });

    $("button#remove").click(function(){
        var filestoreId = $(this).data('filestoreId');
        $.ajax({
            method: "DELETE",
            url: '${springMacroRequestContext.getContextPath()}/filestores/' + filestoreId,
            success: function (data) {
                location.reload(true);
            }

            //error: function(xhr, ajaxOptions, thrownError) {
            //    alert('Error: unable to add new filestore');
            //}
        });

        ev.preventDefault();
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
