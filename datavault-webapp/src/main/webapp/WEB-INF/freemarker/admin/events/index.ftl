<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Events</b></li>
    </ol>

    <#if events?has_content>
        <div class="table-responsive" style="overflow: scroll; font-family:monospace;">
            <table class="table table-striped text-nowrap">
                <thead>
                    <tr class="tr">
                        <th>Actor</th>
                        <th>User</th>
                        <th>Event</th>
                        <th>Timestamp</a></th>
                        <th>Vault</th>
                        <th>Deposit</th>
                        <th>Remote Address</th>
                        <th>User Agent</th>
                        <th>Event ID</th>
                    </tr>
                </thead>

                <tbody>
                    <#list events as event>
                    <tr class="tr">
                        <td>
                            <#if event.actorType == "WORKER">
                                <span class="label label-Warning">${event.actorType}</span>&nbsp${event.actor}
                            <#elseif event.actorType == "WEB_API">
                                <span class="label label-primary">${event.actorType}</span>&nbsp${event.actor}
                            <#else> 
                                <span class="label label-default">${event.actorType}</span>&nbsp${event.actor}
                            </#if>
                        </td>
                        <td>${event.userID!}</td>
                        <td>${event.eventClass?html}</a></td>
                        <td>${event.timestamp?datetime}</td>
                        <td>${event.vaultID!}</td>
                        <td>${event.depositID!}</td>
                        <td>${event.remoteAddress!?html}</a></td>
                        <td>${event.userAgent!?html}</a></td>
                        <td>${event.id}</td>
                    </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

</div>
</@layout.vaultLayout>
