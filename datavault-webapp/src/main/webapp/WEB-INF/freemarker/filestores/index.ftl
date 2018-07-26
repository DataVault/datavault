<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="user">
<@layout.vaultLayout>

<style>
#copy-ssh-public-key .modal-body {
    padding: 0;
    overflow: hidden;
    height: 600px;
}
.filestore-sftp-key {
    width: 100%;
}
</style>

<div class="modal fade" id="confirm-removal" tabindex="-1" role="dialog" aria-labelledby="confirmRemovalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
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
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
                <h4 class="modal-title" id="addFileStoreSFTPLabel">Add filestore</h4>
            </div>
            <div class="modal-body">
                <p>Overwrite the default values as appropriate</p>
                <form id="add-filestoreSFTP-form">
                    <div class="form-filestore">
                        <div class="form-group">
                            <label for="hostname">Hostname</label>
                            <input type="text" class="form-control" id="hostname" name="hostname" value="${sftpHost}"/>
                        </div>
                        <div class="form-group">
                            <label for="port">Port</label>
                            <input type="text" class="form-control" id="port" name="port" value="${sftpPort}"/>
                        </div>
                        <div class="form-group">
                            <label for="path">Path</label>
                            <input type="text" class="form-control" id="path" name="path" value="${sftpRootPath}"/>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button form="add-filestoreSFTP-form" type="submit" class="btn btn-primary">Submit</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="add-filestoreLocal" tabindex="-1" role="dialog" aria-labelledby="addFilestoreLocalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
                <h4 class="modal-title" id="addFileStoreLocalLabel">Add filestore</h4>
            </div>
            <div class="modal-body">
                <p>Currently the default values cannot be overwritten</p>
                <form id="add-filestoreLocal-form">
                    <div class="form-filestore">
                        <div class="form-group">
                            <label for="path">Path</label>
                            <input type="text" class="form-control" id="path" name="path" value="${activeDir}" readonly />
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button form="add-filestoreLocal-form" type="submit" class="btn btn-primary">Submit</button>
            </div>
        </div>
    </div>
</div>


<div class="modal fade bs-example-modal-lg" id="copy-ssh-public-key" tabindex="-1" role="dialog" aria-labelledby="copySshPublicKey" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
                <h4 class="modal-title">Key Registration Dialogue</h4>
            </div>
            <div class="modal-body">
                <iframe src="https://registration.ecdf.ed.ac.uk/storage/public_keys/add" width="100%" height="100%" frameborder="0"></iframe>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>Home</b></a></li>
        <li class="active"><b>File Locations</b></li>
    </ol>

    <div class="panel panel-uoe-low">
        <div class="associated-image">
            <figure class="uoe-panel-image uoe-panel-image"></figure>
        </div>

        <div class="panel-body">
            <h2>Define Your File Locations</h2>
            <br/>

            <p>
            We need to know a little about where you wish to archive data from by SFTP. <br/>
            N.B. if you wish to browse to your files, please use the 'My Computer' button on the 'Create a deposit' page.
            </p>

            <div class="hidden">
                <h3>Define Your Storage Options</h3>

                <div class="table-responsive storage-table">
                    <#if filestoresLocal?has_content>
                    <table class="table table-striped">

                        <thead>
                        <tr class="tr">
                            <th>Hostname</th>
                            <th>Path</th>
                            <th></th>
                        </tr>
                        </thead>

                        <tbody id="fileStoresLocal">
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
                        </tbody>
                    </table>

                    <#else>
                        <h4>There are currently no Local Filestores configured</h4>
                    </#if>

                    <form>
                        <a class="btn btn-default" href="#" data-toggle="modal" data-target="#add-filestoreLocal">
                            <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add Filestore
                        </a>
                    </form>

                </div>
            </div>


            <h3><strong>File store locations</strong></h3>


            <#if filestoresSFTP?has_content>
                <div class="table-responsive storage-table">
                    <p>Your existing filestore paths</p>
                    <p>
                       If your filestore is on a server other than DataStore, in order to allow DataVault to authenticate (i.e. to allow DataVault to connect to the server using your identity), you will have to copy the public SSH key provided here onto your server to the appropriate place, rather than entering a username and password.
                    </p>
                    <table class="table table-bordered">
                        <thead>
                        <tr class="tr">
                            <th>Hostname</th>
                            <th>Port</th>
                            <th>Path</th>
                            <th>Public key</th>
                            <th></th>
                        </tr>
                        </thead>

                        <tbody id="fileStoresSFTP">
                            <#list filestoresSFTP as filestoreSFTP>
                            <tr class="tr">
                                <td>${filestoreSFTP.properties['host']}</td>
                                <td>${filestoreSFTP.properties['port']}</td>
                                <td>${filestoreSFTP.properties['rootPath']}</td>
                                <td class="col-md-5">
                                    <small><textarea id="filestore-sftp-${filestoreSFTP?index}" class="form-control filestore-sftp-key" rows="4" readonly>${filestoreSFTP.properties['publicKey']}</textarea></small>
                                    <br/>
                                    <button class="btn btn-sm btn-default pull-right" onclick="copyToClipboard('#filestore-sftp-${filestoreSFTP?index}')">Copy</button>
                                </td>
                                <td>
                                    <a class="btn btn-xs btn-danger pull-right" href="#" data-filestore="${filestoreSFTP.ID}" data-toggle="modal" data-target="#confirm-removal">
                                        <span class="glyphicon glyphicon-remove" aria-hidden="true"></span> Remove
                                    </a>
                                </td>
                            </tr>
                            </#list>
                        </tbody>
                    </table>
                    <a class="btn btn-default" href="#" data-toggle="modal" data-target="#add-filestoreSFTP">
                        <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> File store
                    </a>
                </div>
            <#else>
                <p>There are currently no SFTP Filestores configured</p>
                <a class="btn btn-default" href="#" data-toggle="modal" data-target="#add-filestoreSFTP">
                    <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> File store
                </a>
            </#if>

            <#if filestoresSFTP?has_content>
                <p>To register your public key you need to:</p>
                <ol>
                    <li>Copy the public key displayed above</li>
                    <li>Open 'Key Registration dialog' with the button below</li>
                    <li>Paste your key into 'Public Key' text area displayed in the dialog</li>
                    <li>Click 'Submit'</li>
                    <li>Close the dialog</li>
                </ol>
                <a class="btn btn-default" href="#" data-toggle="modal" data-target="#copy-ssh-public-key">
                    <span class="glyphicon glyphicon-modal-window" aria-hidden="true"></span> Open Key Registration Dialog
                </a>
            </#if>
        </div>
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
            },
            error: function(xhr, ajaxOptions, thrownError) {
                alert('Error: unable to add new filestore');
            }
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
            },
            error: function(xhr, ajaxOptions, thrownError) {
                alert('Error: unable to add new filestore');
            }
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
            },
            error: function(xhr, ajaxOptions, thrownError) {
                alert('Error: unable to add new filestore');
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

    function copyToClipboard(element) {
        var $temp = $("<input>");
        $("body").append($temp);
        $temp.val($(element).text()).select();
        document.execCommand("copy");
        $temp.remove();
    }

</script>

</@layout.vaultLayout>
