<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="groups">
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li class="active"><b>Group Vaults</b></li>
    </ol>

    <#if groups?has_content>
        <#assign counter = 0 >

        <#list groups as group>
            <div class="panel panel-success">
                <div class="panel-heading">
                    <h3 class="panel-title"><i class="fa fa-users" aria-hidden="true"></i> ${group.name?html} (${group.getID()?html})</h3>
                </div>
                <div class="panel-body">

                    <#if !group.enabled>
                        <div class="alert alert-warning" role="alert">
                            <span class="glyphicon glyphicon-warning-sign" aria-hidden="true"></span>
                            This group is disabled and cannot be used for new Vaults
                        </div>
                    </#if>

                    <div class="table-responsive">
                        <table class="table table-striped">

                            <thead>
                                <tr class="tr">
                                    <th>Vault</th>
                                    <th>Description</th>
                                    <th>Owner</th>
                                    <th>Size</th>
                                    <th>Policy</th>
                                    <th>Timestamp</th>
                                </tr>
                            </thead>

                            <tbody>
                                <#list vaults[counter] as vault>
                                    <tr class="tr">
                                        <td><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/">${vault.name?html}</a></td>
                                        <td>${vault.description?html}</td>
                                        <td>${vault.userID?html}</td>
                                        <td>${vault.getSizeStr()?html}</td>
                                        <td>${vault.policyID?html}</td>
                                        <td>${vault.getCreationTime()?datetime}</td>
                                    </tr>
                                </#list>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <#assign counter = counter + 1 >

        </#list>

        <#else>
            You are not an owner of any group!
        </#if>

</div>
</@layout.vaultLayout>
