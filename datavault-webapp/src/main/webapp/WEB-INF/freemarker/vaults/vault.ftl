<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>My Vaults</b></a></li>
        <li class="active"><b>Vault:</b> ${vault.name?html}</li>
    </ol>

    <div class="bs-callout">
        <h2>
            <span class="glyphicon glyphicon-folder-close"></span> ${vault.name?html}
        </h2>
        <h2>
            <small>
                ${vault.description?html}
            </small>
        </h2>
        <hr>
        <p>
            <b>Owner:</b> ${vault.userID?html}<br/>
            <b>Retention policy:</b> ${retentionPolicy.name?html}<br/>
            <b>Dataset name:</b> ${vault.datasetName?html}<br/>
            <b>Group:</b> ${group.name?html}<br/>
            <b>Created:</b> ${vault.creationTime?datetime}<br/>
            <b>Review date:</b> ${vault.policyExpiry?datetime} (Status: ${vault.policyStatusStr?html})<br/>
            <b>Size:</b> ${vault.getSizeStr()}
        </p>

        <#if deposits?has_content>

            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                        <tr class="tr">
                            <th>Deposit</th>
                            <th>Status</th>
                            <th>Size</th>
                            <th>Timestamp</th>
                        </tr>
                    </thead>

                    <tbody>
                        <#list deposits as deposit>
                            <tr class="tr">
                                <td>
                                    <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}">${deposit.note?html}</a>
                                </td>
                                <td>
                                    <div id="deposit-status" class="job-status">
                                        <#if deposit.status.name() == "COMPLETE">
                                            <div class="text-success">
                                                <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>&nbsp;Complete
                                            </div>
                                        <#elseif deposit.status.name() == "FAILED">
                                            <div class="text-danger">
                                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>&nbsp;Failed
                                            </div>
                                        <#elseif deposit.status.name() == "NOT_STARTED">&nbsp;Not started
                                        <#elseif deposit.status.name() == "IN_PROGRESS">
                                            <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span>&nbsp;In progress
                                        <#else>
                                            ${deposit.status}
                                        </#if>
                                    </div>
                                </td>
                                <td>${deposit.getSizeStr()}</td>
                                <td>${deposit.getCreationTime()?datetime}</td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </#if>

        <form>
            <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/create">
                <i class="fa fa-download fa-rotate-180" aria-hidden="true"></i> Deposit data
            </a>
        </form>

    </div>

</div>

</@layout.vaultLayout>
