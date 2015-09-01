<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}"><b>My Vaults</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}"><b>Vault:</b> ${vault.name?html}</a></li>
        <li class="active"><b>Deposit:</b> ${deposit.note?html}</li>
    </ol>

    <#assign calculateSize = "progtrckr-todo">
    <#assign transferFiles = "progtrckr-todo">
    <#assign packageData = "progtrckr-todo">
    <#assign storeInArchive = "progtrckr-todo">
    <#assign depositComplete = "progtrckr-todo">

    <#if deposit.status.ordinal() gt 0>
        <#assign calculateSize = "progtrckr-done">
    </#if>
    <#if deposit.status.ordinal() gt 1>
        <#assign transferFiles = "progtrckr-done">
    </#if>
    <#if deposit.status.ordinal() gt 2>
        <#assign packageData = "progtrckr-done">
    </#if>
    <#if deposit.status.ordinal() gt 3>
        <#assign storeInArchive = "progtrckr-done">
    </#if>
    <#if deposit.status.ordinal() gt 4>
        <#assign depositComplete = "progtrckr-done">
    </#if>

    <ol class="progtrckr" data-progtrckr-steps="5">
        <li id="progress-calculateSize" class="${calculateSize}">Calculating size</li><!--
     --><li id="progress-transferFiles" class="${transferFiles}">Transferring files</li><!--
     --><li id="progress-packageData" class="${packageData}">Packaging data</li><!--
     --><li id="progress-storeInArchive" class="${storeInArchive}">Storing in archive</li><!--
     --><li id="progress-depositComplete" class="${depositComplete}">Deposit complete</li>
    </ol>

    <#if deposit.status.ordinal() == 2>
        <#assign progressTransferStyle = "">
    <#else>
        <#assign progressTransferStyle = "display:none;">
    </#if>

    <div id="progress-transfer" style="${progressTransferStyle}">
        <#if deposit.size == 0>
            <#assign percentComplete = 0>
        <#else>
            <#assign percentComplete = (deposit.bytesTransferred / deposit.size) * 100>
        </#if>

        <span id="progress-copied">Copied ${deposit.getBytesTransferredStr()} of ${deposit.getSizeStr()} at ${deposit.getBytesPerSecStr()}/sec</span>
        <div class="progress">
          <div id="progress" class="progress-bar progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="${percentComplete}" aria-valuemin="0" aria-valuemax="100" style="width: ${percentComplete}%">
            <span id="progress-label" class="sr-only">${percentComplete}% Complete</span>
          </div>
        </div>
    </div>

    <ul class="nav nav-tabs">
        <li class="active"><a data-toggle="tab" href="#deposit">Deposit</a></li>
        <li><a data-toggle="tab" href="#contents">Contents <span class="badge">${manifest?size}</span></a></li>
        <li><a data-toggle="tab" href="#events">Events <span class="badge">${events?size}</span></a></li>
    </ul>

    <div id="deposit-tab-content" class="tab-content">

        <div class="tab-pane active" id="deposit">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr class="tr">
                        <th>Note</th>
                        <th>Status</th>
                        <th>Size (bytes)</th>
                        <th>Timestamp</th>
                    </tr>
                    <thead>
                    <tbody>
                    <tr class="tr">
                        <td>${deposit.note?html}</td>
                        <td>${deposit.status}</td>
                        <td>${deposit.size}</td>
                        <td>${deposit.creationTime?datetime}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="tab-pane" id="contents">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr class="tr">
                        <th>File</th>
                        <th>Checksum</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list manifest as filefixity>
                        <tr class="tr">
                            <td>${filefixity.file?html}</td>
                            <td style="font-family:monospace;">${filefixity.fixity}&nbsp<span class="label label-primary">${filefixity.algorithm}</span></td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="tab-pane" id="events">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr class="tr">
                        <th>Event</th>
                        <th>Message</th>
                        <th>Timestamp</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list events as event>
                        <tr class="tr">
                            <td>${event.eventClass?html}</td>
                            <td>${event.message?html}</td>
                            <td>${event.timestamp?datetime}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}/restore">
        <span class="glyphicon glyphicon-open" aria-hidden="true"></span> Restore data
    </a>

</div>

<script>

    function updateProgress(deposit) {
        var percentComplete = 0
        if (deposit.size > 0 && deposit.bytesTransferred > 0) {
            percentComplete = (deposit.bytesTransferred / deposit.size) * 100
        }
        
        $('#progress').css('width', percentComplete + '%').attr('aria-valuenow', percentComplete);
        $('#progress-label').text(percentComplete + '% Complete')
        $('#progress-copied').text("Copied " + deposit.bytesTransferredStr + " of " + deposit.sizeStr + " at " + deposit.bytesPerSecStr + "/sec")

        if (deposit.status == 'COMPLETE') {
            $('#progress-transfer').hide()
            statusComplete()
        } else if (deposit.status == 'STORE_ARCHIVE_PACKAGE') {
            $('#progress-transfer').hide()
            statusStoreInArchive()
        } else if (deposit.status == 'CREATE_PACKAGE') {
            $('#progress-transfer').hide()
            statusPackageData()
        } else if (deposit.status == 'TRANSFER_FILES') {
            $('#progress-transfer').show()
            statusTransferFiles()
        } else if (deposit.status == 'CALCULATE_SIZE') {
            $('#progress-transfer').hide()
            statusCalculateSize()
        }
    }

    function statusComplete() {
        $('#progress-depositComplete').removeClass('progtrckr-todo')
        $('#progress-depositComplete').addClass('progtrckr-done')
        
        statusStoreInArchive()
    }
    
    function statusStoreInArchive() {
        $('#progress-storeInArchive').removeClass('progtrckr-todo')
        $('#progress-storeInArchive').addClass('progtrckr-done')

        statusPackageData()
    }

    function statusPackageData() {
        $('#progress-packageData').removeClass('progtrckr-todo')
        $('#progress-packageData').addClass('progtrckr-done')

        statusTransferFiles()
    }

    function statusTransferFiles() {
        $('#progress-transferFiles').removeClass('progtrckr-todo')
        $('#progress-transferFiles').addClass('progtrckr-done')

        statusCalculateSize()
    }

    function statusCalculateSize() {
        $('#progress-calculateSize').removeClass('progtrckr-todo')
        $('#progress-calculateSize').addClass('progtrckr-done')
    }

    function load() {
        setTimeout(function () {
            $.ajax({
                url: "${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}/json",
                type: "GET",
                dataType: 'json',  
                success: function (result) {
                    updateProgress(result)

                    if (result.status != 'COMPLETE') {
                        load()
                    }
                }
            });
        }, 500);
    }
    load();
</script>

</@layout.vaultLayout>
