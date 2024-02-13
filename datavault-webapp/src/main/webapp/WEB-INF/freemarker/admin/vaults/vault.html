<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/vaults"><b>Vaults</b></a></li>
        <li class="active"><b>Vault:</b> ${vault.name?html}</li>
    </ol>

    <form action="${springMacroRequestContext.getContextPath()}/admin/vaults/${vault.getID()}/checkretentionpolicy" method="post">

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
            <b>Retention Policy:</b> ${retentionPolicy.name?html}<br/>
            <b>Dataset name:</b> ${vault.datasetName?html}<br/>
            <b>Group:</b> ${group.name?html}<br/>
            <b>Created:</b> ${vault.creationTime?datetime}<br/>
            <b>Review date:</b> ${vault.policyExpiry?datetime} (Status: ${vault.policyStatusStr?html})<br/>
            <b>Last checked:</b> ${vault.policyLastChecked?datetime} <button type="submit" class="btn btn-default btn-sm"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span> Update</button><br/>
            <b>Size:</b> ${vault.getSizeStr()}
        </p>

        <#if deposits?has_content>

            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                        <tr class="tr">
                            <th>Deposit</th>
                            <th>Origin</th>
                            <th>File Path</th>
                            <th>Status</th>
                            <th>Size</th>
                            <th>Timestamp</th>
                        </tr>
                    </thead>

                    <tbody>
                        <#list deposits as deposit>
                            <tr class="tr">
                                <td>
                                    <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}">${deposit.name?html}</a>
                                </td>
                                <td>${deposit.fileOrigin?html}</td>
                                <td>${deposit.shortFilePath?html}</td>
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
                                        <#elseif deposit.status.name() == "NOT_STARTED">&nbsp;Queued
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
    </div>

        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </form>

</div>

</@layout.vaultLayout>
