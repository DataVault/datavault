<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}"><b>My Vaults</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}"><b>Vault:</b> ${vault.name?html}</a></li>
        <li class="active"><b>Deposit:</b> ${deposit.note?html}</li>
    </ol>

    <#if deposit.status.ordinal() gt 0>

        <#assign calculateSize = "progtrckr-done">
        <#assign transferFiles = "progtrckr-todo">
        <#assign packageData = "progtrckr-todo">
        <#assign storeInArchive = "progtrckr-todo">
        <#assign depositComplete = "progtrckr-todo">

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
            <li class="${calculateSize}">Calculating size</li><!--
         --><li class="${transferFiles}">Transferring files</li><!--
         --><li class="${packageData}">Packaging data</li><!--
         --><li class="${storeInArchive}">Storing in archive</li><!--
         --><li class="${depositComplete}">Deposit complete</li>
        </ol>

        <#if deposit.status.ordinal() == 2>
            <#if deposit.size == 0>
                <#assign percentComplete = 0>
            <#else>
                <#assign percentComplete = (deposit.bytesTransferred / deposit.size) * 100>
            </#if>

            <#if percentComplete != 100>
                <div class="progress">
                  <div class="progress-bar progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="${percentComplete}" aria-valuemin="0" aria-valuemax="100" style="width: ${percentComplete}%">
                    <span class="sr-only">${percentComplete}% Complete</span>
                  </div>
                </div>
            </#if>
        </#if>

    </#if>

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

</@layout.vaultLayout>
