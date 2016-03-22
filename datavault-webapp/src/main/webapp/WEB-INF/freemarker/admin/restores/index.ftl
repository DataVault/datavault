<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Retrieves</b></li>
    </ol>

    <#if restores?has_content>
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                <tr class="tr">
                    <th>Retrieve</th>
                    <th>Status</th>
                    <th>Timestamp</th>
                    <th>Retrieve path</th>
                    <th>Retrieve note</th>
                </tr>
                </thead>

                <tbody>
                    <#list restores as restore>
                    <tr class="tr">
                        <td>${restore.getDeposit().getID()?html}</td>
                        <td>${restore.status?html}</td>
                        <td>${restore.timestamp?datetime}</td>
                        <td>${restore.restorePath?html}</td>
                        <td>${restore.note?html}</td>
                    </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

</div>
</@layout.vaultLayout>
