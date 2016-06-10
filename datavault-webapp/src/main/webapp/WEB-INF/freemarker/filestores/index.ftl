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
                <button type="button" class="btn btn-danger btn-ok">Remove</button>
            </div>
        </div>
    </div>
</div>


<div class="modal fade" id="add-filestore" tabindex="-1" role="dialog" aria-labelledby="addFilestoreLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="addFileStoreLabel">Add filestore</h4>
            </div>
            <div class="modal-body">
                <p>Enter new filestore details here</p>
                <form id="add-filestore-form">
                    <div class="form-filestore">
                        <div class="form-group">
                            <label for="add-hostname">Hostname</label>
                            <input type="text" class="form-control" id="add-hostname" placeholder="Enter hostname"/>
                        </div>
                        <div class="form-group">
                            <label for="add-port">Port</label>
                            <input type="text" class="form-control" id="add-port" placeholder="Enter port"/>
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

    <h3>SFTP Storage</h3>

       <div class="table-responsive">
            <table class="table table-striped">

                <thead>
                    <tr class="tr">
                        <th>Hostname</th>
                        <th>Port</th>
                        <th>Path</th>
                        <th>Public Key</th>
                    </tr>
                </thead>

                <tbody>
                    <#if filestoresSFTP?has_content>
                        <#list filestoresSFTP as filestoreSFTP>
                        <tr class="tr">
                            <td>${filestoreSFTP.properties['hostname']}</td>
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
               <a class="btn btn-default" href="#" data-toggle="modal" data-target="#add-filestore">
                   <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add Filestore
               </a>
           </form>

        </div>




</div>

<script>
    // Bind properties to the filestore removal confirmation dialog
    $('#confirm-removal').on('show.bs.modal', function(e) {
        var data = $(e.relatedTarget).data();
        $('.remove-filestore', this).text(data.filestore);
        $('.btn-ok', this).data('filestore', data.filestore);
    });

    // Bind OK button for user removal confirmation dialog
    $('#confirm-removal').on('click', '.btn-ok', function(e) {
        var $modalDiv = $(e.delegateTarget);
        var user = $(this).data('filestore');
    
        $.ajax({
            url: '${springMacroRequestContext.getContextPath()}/filestores/' + filestore.??????,
            type: 'DELETE',
            success: function(result) {
                $modalDiv.modal('hide');
                location.reload(true);
            }
        });
    });

    // Bind properties to the add filestore dialog
    $('#add-filestore').on('show.bs.modal', function(e) {
        var data = $(e.relatedTarget).data();
        $('#add-hostname').val('');
        $('#add-port').val('');
    });



    var sftpFrm = $('#add-filestore-form');
    sftpFrm.submit(function (ev) {
        $.ajax({
            type: sftpFrm.attr('POST'),
            url: '${springMacroRequestContext.getContextPath()}/filestores'
            data: sftpFrm.serialize(),
            success: function (data) {
                alert('Public key is ' + data);
            }
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
