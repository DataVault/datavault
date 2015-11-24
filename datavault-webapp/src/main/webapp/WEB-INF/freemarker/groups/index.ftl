<#import "*/layout/groupslayout.ftl" as layout>
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
                    <h3 class="panel-title"><span class="glyphicon glyphicon-eye-open"></span> ${group.name?html} (${group.getID()?html})</h3>
                </div>
                <div class="panel-body">
                    <div class="table-responsive">
                        <table class="table table-striped">

                            <thead>
                                <tr class="tr">
                                    <th>ID</th>
                                    <th>Name</th>
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
                                        <td>${vault.getID()?html}</td>
                                        <td>${vault.name?html}</td>
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
